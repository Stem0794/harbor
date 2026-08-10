package com.monstera.harbor.core.topology

import kotlinx.coroutines.flow.Flow

enum class PrivilegedAvailability {
    NOT_INSTALLED,
    BINDER_UNAVAILABLE,
    PERMISSION_REQUIRED,
    PERMISSION_DENIED,
    READY,
    UNSUPPORTED,
}

enum class HarborPrivilegeLevel {
    STANDARD,
    SHIZUKU_ADB,
    SHIZUKU_ROOT,
}

enum class HarborPrivilegeSecondaryState {
    ADVANCED_DISABLED,
    SHIZUKU_NOT_INSTALLED,
    SHIZUKU_STOPPED,
    SHIZUKU_PERMISSION_REQUIRED,
    SHIZUKU_PERMISSION_DENIED,
    ADVANCED_WORKSPACE_TOOLS_AVAILABLE,
    ROOT_ALLOWLIST_ONLY,
    UNSUPPORTED,
}

data class HarborPrivilegeState(
    val level: HarborPrivilegeLevel,
    val secondary: HarborPrivilegeSecondaryState,
)

data class PrivilegedBackendState(
    val availability: PrivilegedAvailability,
    val serverVersion: Int? = null,
    val effectiveUid: Int? = null,
    val detail: String? = null,
)

object HarborPrivilegeResolver {
    private const val UID_ROOT = 0
    private const val UID_SHELL = 2_000

    fun resolve(
        advancedEnabled: Boolean,
        backend: PrivilegedBackendState,
    ): HarborPrivilegeState {
        if (!advancedEnabled) {
            return HarborPrivilegeState(
                HarborPrivilegeLevel.STANDARD,
                HarborPrivilegeSecondaryState.ADVANCED_DISABLED,
            )
        }
        return when (backend.availability) {
            PrivilegedAvailability.NOT_INSTALLED -> standard(HarborPrivilegeSecondaryState.SHIZUKU_NOT_INSTALLED)
            PrivilegedAvailability.BINDER_UNAVAILABLE -> standard(HarborPrivilegeSecondaryState.SHIZUKU_STOPPED)
            PrivilegedAvailability.PERMISSION_REQUIRED -> standard(HarborPrivilegeSecondaryState.SHIZUKU_PERMISSION_REQUIRED)
            PrivilegedAvailability.PERMISSION_DENIED -> standard(HarborPrivilegeSecondaryState.SHIZUKU_PERMISSION_DENIED)
            PrivilegedAvailability.UNSUPPORTED -> standard(HarborPrivilegeSecondaryState.UNSUPPORTED)
            PrivilegedAvailability.READY -> when (backend.effectiveUid) {
                UID_SHELL -> HarborPrivilegeState(
                    HarborPrivilegeLevel.SHIZUKU_ADB,
                    HarborPrivilegeSecondaryState.ADVANCED_WORKSPACE_TOOLS_AVAILABLE,
                )
                UID_ROOT -> HarborPrivilegeState(
                    HarborPrivilegeLevel.SHIZUKU_ROOT,
                    HarborPrivilegeSecondaryState.ROOT_ALLOWLIST_ONLY,
                )
                else -> standard(HarborPrivilegeSecondaryState.UNSUPPORTED)
            }
        }
    }

    private fun standard(secondary: HarborPrivilegeSecondaryState) = HarborPrivilegeState(
        HarborPrivilegeLevel.STANDARD,
        secondary,
    )
}

sealed interface PrivilegedResult<out T> {
    data class Success<T>(val value: T) : PrivilegedResult<T>
    data class Failure(val reason: String, val recoverable: Boolean = true) : PrivilegedResult<Nothing>
}

data class SystemUser(
    val id: AndroidUserId,
    val name: String,
    val flags: Int,
    val isRunning: Boolean,
) {
    val isManagedProfile: Boolean
        get() = flags and FLAG_MANAGED_PROFILE != 0

    val isProfile: Boolean
        get() = flags and (FLAG_MANAGED_PROFILE or FLAG_PROFILE) != 0

    val isFullUser: Boolean
        get() = flags and FLAG_FULL != 0 || !isProfile && flags and FLAG_SYSTEM == 0

    val isSwitchableFullUser: Boolean
        get() = isFullUser && !isProfile && flags and (FLAG_RESTRICTED or FLAG_DISABLED) == 0

    private companion object {
        const val FLAG_RESTRICTED = 0x00000008
        const val FLAG_MANAGED_PROFILE = 0x00000020
        const val FLAG_DISABLED = 0x00000040
        const val FLAG_FULL = 0x00000400
        const val FLAG_SYSTEM = 0x00000800
        const val FLAG_PROFILE = 0x00001000
    }
}

data class CloneProfileRelationship(
    val sourceFullUser: SystemUser,
    val targetManagedProfile: SystemUser,
)

object CloneProfileRelationshipResolver {
    fun resolve(
        currentFullUser: AndroidUserId,
        users: List<SystemUser>,
    ): PrivilegedResult<CloneProfileRelationship> {
        val source = users.singleOrNull {
            it.id == currentFullUser && it.isSwitchableFullUser
        } ?: return PrivilegedResult.Failure(
            "Harbor cannot safely identify the current full Android user",
            false,
        )
        val profiles = users.filter(SystemUser::isProfile)
        if (profiles.size != 1) {
            return PrivilegedResult.Failure(
                "Harbor cannot safely identify one work profile in this user topology",
                false,
            )
        }
        val target = profiles.single()
        if (!target.isManagedProfile || !target.isRunning) {
            return PrivilegedResult.Failure(
                "The only visible profile is not an active managed work profile",
                false,
            )
        }
        return PrivilegedResult.Success(CloneProfileRelationship(source, target))
    }
}

data class SystemDiagnostics(
    val effectiveUid: Int,
    val currentUser: AndroidUserId?,
    val maximumUsers: Int?,
    val users: List<SystemUser>,
)

data class InstallExistingResult(
    val packageName: PackageName,
    val targetUser: AndroidUserId,
    val message: String,
)

data class CloneCandidate(
    val packageName: PackageName,
    val label: String,
    val isSystem: Boolean,
    val alreadyInstalledInTarget: Boolean,
)

interface PrivilegedBackend {
    fun observeState(): Flow<PrivilegedBackendState>
    fun refresh()
    suspend fun requestPermission(): PrivilegedResult<Unit>
    suspend fun diagnostics(): PrivilegedResult<SystemDiagnostics>
    suspend fun resolveCloneProfile(): PrivilegedResult<CloneProfileRelationship>
    suspend fun listPackages(user: AndroidUserId): PrivilegedResult<List<PackageName>>
    suspend fun listPackagesInWorkProfile(user: AndroidUserId): PrivilegedResult<List<PackageName>>
    suspend fun installExisting(
        packageName: PackageName,
        targetUser: AndroidUserId,
    ): PrivilegedResult<InstallExistingResult>
    fun release()
}

class NoOpPrivilegedBackend : PrivilegedBackend {
    override fun observeState(): Flow<PrivilegedBackendState> = kotlinx.coroutines.flow.flowOf(
        PrivilegedBackendState(
            availability = PrivilegedAvailability.NOT_INSTALLED,
            detail = "Advanced tools are unavailable",
        ),
    )

    override suspend fun requestPermission(): PrivilegedResult<Unit> = unavailable()
    override fun refresh() = Unit
    override suspend fun diagnostics(): PrivilegedResult<SystemDiagnostics> = unavailable()
    override suspend fun resolveCloneProfile(): PrivilegedResult<CloneProfileRelationship> = unavailable()
    override suspend fun listPackages(user: AndroidUserId): PrivilegedResult<List<PackageName>> = unavailable()
    override suspend fun listPackagesInWorkProfile(user: AndroidUserId): PrivilegedResult<List<PackageName>> = unavailable()
    override suspend fun installExisting(
        packageName: PackageName,
        targetUser: AndroidUserId,
    ): PrivilegedResult<InstallExistingResult> = unavailable()

    override fun release() = Unit

    private fun unavailable() = PrivilegedResult.Failure("Advanced tools are unavailable", false)
}

interface MultiUserController {
    suspend fun listUsers(): PrivilegedResult<List<SystemUser>>
    suspend fun createFullUser(name: UserVisibleName): PrivilegedResult<SystemUser>
    suspend fun installHarbor(user: AndroidUserId): PrivilegedResult<Unit>
    suspend fun switchUser(user: AndroidUserId): PrivilegedResult<Unit>
}
