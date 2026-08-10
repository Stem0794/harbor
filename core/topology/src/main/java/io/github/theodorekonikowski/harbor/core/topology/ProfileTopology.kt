package io.github.theodorekonikowski.harbor.core.topology

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.pm.LauncherApps
import android.os.Build
import android.os.Process
import android.os.UserManager

enum class LocalProfileKind {
    FULL_USER,
    MANAGED_PROFILE,
    UNKNOWN_PROFILE,
}

enum class ProfileOwnership {
    LOCAL_HARBOR_PROFILE_OWNER,
    HARBOR_INSTALLED_OWNER_UNKNOWN,
    FOREIGN_OR_UNKNOWN,
}

data class AssociatedProfile(
    val userId: AndroidUserId,
    val isCurrent: Boolean,
    val kind: LocalProfileKind,
    val ownership: ProfileOwnership,
)

data class ProfileTopology(
    val currentUser: AndroidUserId,
    val localKind: LocalProfileKind,
    val harborIsProfileOwner: Boolean,
    val associatedProfiles: List<AssociatedProfile>,
    val supportsMultipleUsers: Boolean,
)

class ProfileTopologyDetector(private val context: Context) {
    fun detect(): ProfileTopology {
        val userManager = context.getSystemService(UserManager::class.java)
        val policyManager = context.getSystemService(DevicePolicyManager::class.java)
        val launcherApps = context.getSystemService(LauncherApps::class.java)
        val currentHandle = Process.myUserHandle()
        val isProfileOwner = policyManager.isProfileOwnerApp(context.packageName)
        val systemReportsManaged = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && userManager.isManagedProfile
        val systemReportsProfile =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && userManager.isProfile
        val localKind = when {
            isProfileOwner -> LocalProfileKind.MANAGED_PROFILE
            systemReportsManaged -> LocalProfileKind.MANAGED_PROFILE
            systemReportsProfile -> LocalProfileKind.UNKNOWN_PROFILE
            Build.VERSION.SDK_INT < Build.VERSION_CODES.R && userManager.userProfiles.size > 1 ->
                LocalProfileKind.UNKNOWN_PROFILE
            userManager.userProfiles.size == 1 -> LocalProfileKind.FULL_USER
            else -> LocalProfileKind.FULL_USER
        }
        return ProfileTopology(
            // Android does not expose UserHandle#getIdentifier to ordinary apps. Its public
            // hashCode is used only as a candidate and is revalidated against a fresh Shizuku
            // user listing before any privileged operation.
            currentUser = AndroidUserId(currentHandle.hashCode()),
            localKind = localKind,
            harborIsProfileOwner = isProfileOwner,
            associatedProfiles = userManager.userProfiles.map { handle ->
                val isCurrent = handle == currentHandle
                val harborVisible = runCatching {
                    launcherApps.getActivityList(context.packageName, handle).isNotEmpty()
                }.getOrDefault(false)
                AssociatedProfile(
                    userId = AndroidUserId(handle.hashCode()),
                    isCurrent = isCurrent,
                    kind = if (isCurrent) localKind else LocalProfileKind.UNKNOWN_PROFILE,
                    ownership = when {
                        isCurrent && isProfileOwner -> ProfileOwnership.LOCAL_HARBOR_PROFILE_OWNER
                        harborVisible -> ProfileOwnership.HARBOR_INSTALLED_OWNER_UNKNOWN
                        else -> ProfileOwnership.FOREIGN_OR_UNKNOWN
                    },
                )
            },
            supportsMultipleUsers = UserManager.supportsMultipleUsers(),
        )
    }
}
