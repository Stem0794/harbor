package com.monstera.harbor.privileged.shizuku

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import com.monstera.harbor.core.topology.AndroidUserId
import com.monstera.harbor.core.topology.CloneProfileRelationship
import com.monstera.harbor.core.topology.CloneProfileRelationshipResolver
import com.monstera.harbor.core.topology.InstallExistingResult
import com.monstera.harbor.core.topology.MultiUserController
import com.monstera.harbor.core.topology.PackageName
import com.monstera.harbor.core.topology.PrivilegedAvailability
import com.monstera.harbor.core.topology.PrivilegedBackend
import com.monstera.harbor.core.topology.PrivilegedBackendState
import com.monstera.harbor.core.topology.PrivilegedResult
import com.monstera.harbor.core.topology.SystemDiagnostics
import com.monstera.harbor.core.topology.SystemUser
import com.monstera.harbor.core.topology.SystemUserParser
import com.monstera.harbor.core.topology.UserVisibleName
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import kotlin.coroutines.resume

class ShizukuPrivilegedBackend(
    private val context: Context,
) : PrivilegedBackend, MultiUserController, AutoCloseable {
    private val state = MutableStateFlow(snapshot())
    private var service: IHarborUserService? = null
    private var connection: ServiceConnection? = null

    private val binderReceived = Shizuku.OnBinderReceivedListener { state.value = snapshot() }
    private val binderDead = Shizuku.OnBinderDeadListener {
        service = null
        state.value = snapshot()
    }

    init {
        Shizuku.addBinderReceivedListenerSticky(binderReceived)
        Shizuku.addBinderDeadListener(binderDead)
    }

    override fun observeState(): Flow<PrivilegedBackendState> = state.asStateFlow()

    override fun refresh() {
        val refreshed = snapshot()
        state.value = refreshed
        if (refreshed.availability != PrivilegedAvailability.READY) release()
    }

    override suspend fun requestPermission(): PrivilegedResult<Unit> {
        if (!Shizuku.pingBinder()) return PrivilegedResult.Failure("Shizuku is not running")
        if (Shizuku.isPreV11()) return PrivilegedResult.Failure("Shizuku API 11 or newer is required", false)
        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            state.value = snapshot()
            return PrivilegedResult.Success(Unit)
        }
        if (Shizuku.shouldShowRequestPermissionRationale()) {
            state.value = snapshot(permissionDenied = true)
            return PrivilegedResult.Failure("Shizuku permission was denied")
        }
        return withTimeoutOrNull(PERMISSION_RESULT_TIMEOUT_MILLIS) {
            suspendCancellableCoroutine { continuation ->
                val listener = object : Shizuku.OnRequestPermissionResultListener {
                    override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
                        if (requestCode != PERMISSION_REQUEST_CODE) return
                        Shizuku.removeRequestPermissionResultListener(this)
                        state.value = snapshot(permissionDenied = grantResult != PackageManager.PERMISSION_GRANTED)
                        if (continuation.isActive) {
                            continuation.resume(
                                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                                    PrivilegedResult.Success(Unit)
                                } else {
                                    PrivilegedResult.Failure("Shizuku permission was denied")
                                },
                            )
                        }
                    }
                }
                Shizuku.addRequestPermissionResultListener(listener)
                continuation.invokeOnCancellation { Shizuku.removeRequestPermissionResultListener(listener) }
                runCatching { Shizuku.requestPermission(PERMISSION_REQUEST_CODE) }
                    .onFailure {
                        Shizuku.removeRequestPermissionResultListener(listener)
                        if (continuation.isActive) {
                            continuation.resume(PrivilegedResult.Failure(it.safeMessage()))
                        }
                    }
            }
        } ?: PrivilegedResult.Failure("Timed out waiting for Shizuku permission")
    }

    override suspend fun diagnostics(): PrivilegedResult<SystemDiagnostics> = call { remote ->
        val response = CommandResponse.decode(remote.diagnostics())
        if (!response.isSuccess) return@call PrivilegedResult.Failure(response.output)
        val output = response.output
        val current = Regex("(?m)^CURRENT=(\\d+)$").find(output)?.groupValues?.get(1)?.toIntOrNull()
        val maximum = Regex("(?m)^MAX=.*?(\\d+)\\s*$").find(output)?.groupValues?.get(1)?.toIntOrNull()
        val usersOutput = output.substringAfter("USERS_BEGIN", "")
        PrivilegedResult.Success(
            SystemDiagnostics(
                effectiveUid = Regex("(?m)^UID=(\\d+)$").find(output)?.groupValues?.get(1)?.toIntOrNull()
                    ?: Shizuku.getUid(),
                currentUser = current?.let(::AndroidUserId),
                maximumUsers = maximum,
                users = SystemUserParser.parse(usersOutput),
            ),
        )
    }

    override suspend fun resolveCloneProfile(): PrivilegedResult<CloneProfileRelationship> = call { remote ->
        latestCloneProfile(remote)
    }

    override suspend fun installExisting(
        packageName: PackageName,
        targetUser: AndroidUserId,
    ): PrivilegedResult<InstallExistingResult> = call { remote ->
        val relationship = latestCloneProfile(remote).getOrElseFailure { return@call it }
        val target = relationship.targetManagedProfile.takeIf { it.id == targetUser }
            ?: return@call PrivilegedResult.Failure("The target is not the current unambiguous work profile")
        CommandResponse.decode(remote.installExisting(packageName.value, targetUser.value)).toResult {
            InstallExistingResult(packageName, target.id, output)
        }
    }

    override suspend fun listPackages(user: AndroidUserId): PrivilegedResult<List<PackageName>> = call { remote ->
        val relationship = latestCloneProfile(remote).getOrElseFailure { return@call it }
        if (relationship.sourceFullUser.id != user) {
            return@call PrivilegedResult.Failure("The package source is not the current unambiguous parent user")
        }
        val response = CommandResponse.decode(remote.listPackages(user.value))
        if (!response.isSuccess) return@call PrivilegedResult.Failure(response.output)
        val packages = response.output.lineSequence().mapNotNull { line ->
            runCatching { PackageName(line.removePrefix("package:").trim()) }.getOrNull()
        }.distinct().sortedBy { it.value }.toList()
        PrivilegedResult.Success(packages)
    }

    override suspend fun listPackagesInWorkProfile(user: AndroidUserId): PrivilegedResult<List<PackageName>> = call { remote ->
        val relationship = latestCloneProfile(remote).getOrElseFailure { return@call it }
        if (relationship.targetManagedProfile.id != user) {
            return@call PrivilegedResult.Failure("The target is not the current unambiguous work profile")
        }
        val response = CommandResponse.decode(remote.listPackages(user.value))
        if (!response.isSuccess) return@call PrivilegedResult.Failure(response.output)
        PrivilegedResult.Success(
            response.output.lineSequence().mapNotNull { line ->
                runCatching { PackageName(line.removePrefix("package:").trim()) }.getOrNull()
            }.distinct().sortedBy { it.value }.toList(),
        )
    }

    override suspend fun listUsers(): PrivilegedResult<List<SystemUser>> = call { remote ->
        latestUsers(remote).mapSuccess { users -> users.filter(SystemUser::isSwitchableFullUser) }
    }

    override suspend fun createFullUser(name: UserVisibleName): PrivilegedResult<SystemUser> = call { remote ->
        val response = CommandResponse.decode(remote.createFullUser(name.value))
        if (!response.isSuccess) return@call PrivilegedResult.Failure(response.output)
        val id = Regex("(?i)user id (\\d+)").find(response.output)?.groupValues?.get(1)?.toIntOrNull()
            ?: return@call PrivilegedResult.Failure("Android did not return the new user ID")
        PrivilegedResult.Success(SystemUser(AndroidUserId(id), name.value, flags = 0, isRunning = false))
    }

    override suspend fun installHarbor(user: AndroidUserId): PrivilegedResult<Unit> = call { remote ->
        if (latestUsers(remote).getOrElseFailure { return@call it }.none {
                it.id == user && it.isSwitchableFullUser
            }
        ) {
            return@call PrivilegedResult.Failure("The target is not a current full Android user")
        }
        CommandResponse.decode(remote.installHarbor(context.packageName, user.value)).toResult { Unit }
    }

    override suspend fun switchUser(user: AndroidUserId): PrivilegedResult<Unit> = call { remote ->
        if (latestUsers(remote).getOrElseFailure { return@call it }.none {
                it.id == user && it.isSwitchableFullUser
            }
        ) {
            return@call PrivilegedResult.Failure("The target is not a switchable full Android user")
        }
        CommandResponse.decode(remote.switchUser(user.value)).toResult { Unit }
    }

    override fun release() {
        connection?.let { activeConnection ->
            runCatching { Shizuku.unbindUserService(serviceArgs(), activeConnection, true) }
        }
        connection = null
        service = null
    }

    override fun close() {
        Shizuku.removeBinderReceivedListener(binderReceived)
        Shizuku.removeBinderDeadListener(binderDead)
        release()
    }

    private suspend fun <T> call(block: (IHarborUserService) -> PrivilegedResult<T>): PrivilegedResult<T> =
        withContext(Dispatchers.IO) {
            when (val permission = ensurePermission()) {
                is PrivilegedResult.Failure -> permission
                is PrivilegedResult.Success -> {
                    val remote = withTimeoutOrNull(SERVICE_BIND_TIMEOUT_MILLIS) { bindService() }
                        ?: return@withContext PrivilegedResult.Failure("Timed out connecting to Shizuku")
                    runCatching { block(remote) }
                        .getOrElse {
                            service = null
                            state.value = snapshot()
                            PrivilegedResult.Failure(it.safeMessage())
                        }
                }
            }
        }

    private fun ensurePermission(): PrivilegedResult<Unit> = when {
        !Shizuku.pingBinder() -> PrivilegedResult.Failure("Shizuku is not running")
        Shizuku.isPreV11() -> PrivilegedResult.Failure("Shizuku API 11 or newer is required", false)
        Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED -> {
            release()
            state.value = snapshot(permissionDenied = true)
            PrivilegedResult.Failure("Grant Harbor permission in Shizuku")
        }
        else -> PrivilegedResult.Success(Unit)
    }

    private suspend fun bindService(): IHarborUserService {
        service?.let { return it }
        return suspendCancellableCoroutine { continuation ->
            val candidate = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                    val remote = IHarborUserService.Stub.asInterface(binder)
                    if (continuation.isActive) {
                        service = remote
                        connection = this
                        continuation.resume(remote)
                    } else {
                        runCatching { Shizuku.unbindUserService(serviceArgs(), this, true) }
                    }
                }

                override fun onServiceDisconnected(name: ComponentName) {
                    service = null
                    state.value = snapshot()
                }
            }
            runCatching { Shizuku.bindUserService(serviceArgs(), candidate) }
                .onFailure { if (continuation.isActive) continuation.resumeWith(Result.failure(it)) }
            continuation.invokeOnCancellation {
                runCatching { Shizuku.unbindUserService(serviceArgs(), candidate, true) }
            }
        }
    }

    private fun serviceArgs() = Shizuku.UserServiceArgs(
        ComponentName(context.packageName, HarborUserService::class.java.name),
    ).daemon(false)
        .tag("harbor-privileged-v1")
        .processNameSuffix("harbor_privileged")
        .debuggable(BuildConfig.DEBUG)
        .version(1)

    private fun snapshot(permissionDenied: Boolean = false): PrivilegedBackendState = runCatching {
        if (!Shizuku.pingBinder()) {
            val installed = runCatching {
                context.packageManager.getApplicationInfo(SHIZUKU_PACKAGE, 0)
            }.isSuccess
            return@runCatching PrivilegedBackendState(
                if (installed) PrivilegedAvailability.BINDER_UNAVAILABLE else PrivilegedAvailability.NOT_INSTALLED,
                detail = if (installed) "Start Shizuku to use Advanced tools" else "Shizuku is not installed",
            )
        }
        if (Shizuku.isPreV11()) {
            return@runCatching PrivilegedBackendState(
                PrivilegedAvailability.UNSUPPORTED,
                serverVersion = Shizuku.getVersion(),
                detail = "Shizuku API 11 or newer is required",
            )
        }
        val granted = Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        PrivilegedBackendState(
            availability = when {
                granted -> PrivilegedAvailability.READY
                permissionDenied || Shizuku.shouldShowRequestPermissionRationale() -> PrivilegedAvailability.PERMISSION_DENIED
                else -> PrivilegedAvailability.PERMISSION_REQUIRED
            },
            serverVersion = Shizuku.getVersion(),
            effectiveUid = if (granted) Shizuku.getUid() else null,
        )
    }.getOrElse {
        PrivilegedBackendState(PrivilegedAvailability.BINDER_UNAVAILABLE, detail = it.safeMessage())
    }

    private inline fun <T> CommandResponse.toResult(value: CommandResponse.() -> T): PrivilegedResult<T> =
        if (isSuccess) PrivilegedResult.Success(value()) else PrivilegedResult.Failure(output)

    private fun latestUsers(remote: IHarborUserService): PrivilegedResult<List<SystemUser>> =
        CommandResponse.decode(remote.listUsers()).toResult {
            val parsed = SystemUserParser.parseResult(output)
            when {
                !parsed.isComplete -> return PrivilegedResult.Failure(
                    "Android returned a partially unrecognized user list",
                    false,
                )
                parsed.users.isEmpty() -> return PrivilegedResult.Failure(
                    "Android returned no recognizable users",
                    false,
                )
                else -> parsed.users
            }
        }

    private fun latestCloneProfile(
        remote: IHarborUserService,
    ): PrivilegedResult<CloneProfileRelationship> {
        val currentResponse = CommandResponse.decode(remote.currentUser())
        if (!currentResponse.isSuccess) return PrivilegedResult.Failure(currentResponse.output)
        val currentUser = currentResponse.output.lineSequence()
            .map(String::trim)
            .firstNotNullOfOrNull(String::toIntOrNull)
            ?.let(::AndroidUserId)
            ?: return PrivilegedResult.Failure("Android did not report the current full user", false)
        val users = latestUsers(remote).getOrElseFailure { return it }
        return CloneProfileRelationshipResolver.resolve(currentUser, users)
    }

    private inline fun <T, R> PrivilegedResult<T>.mapSuccess(transform: (T) -> R): PrivilegedResult<R> = when (this) {
        is PrivilegedResult.Success -> PrivilegedResult.Success(transform(value))
        is PrivilegedResult.Failure -> this
    }

    private inline fun <T> PrivilegedResult<T>.getOrElseFailure(
        block: (PrivilegedResult.Failure) -> Nothing,
    ): T = when (this) {
        is PrivilegedResult.Success -> value
        is PrivilegedResult.Failure -> block(this)
    }

    private fun Throwable.safeMessage(): String = message ?: javaClass.simpleName

    private companion object {
        const val SHIZUKU_PACKAGE = "moe.shizuku.privileged.api"
        const val PERMISSION_REQUEST_CODE = 0x4842
        const val PERMISSION_RESULT_TIMEOUT_MILLIS = 30_000L
        const val SERVICE_BIND_TIMEOUT_MILLIS = 15_000L
    }
}
