package com.monstera.harbor.core.policy

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import com.monstera.harbor.core.topology.PackageName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed interface CrossProfilePackageAccess {
    data object Enabled : CrossProfilePackageAccess
    data object Disabled : CrossProfilePackageAccess
    data object Unsupported : CrossProfilePackageAccess
}

interface CrossProfilePackagePolicy {
    suspend fun accessFor(packageName: PackageName): PolicyResult<CrossProfilePackageAccess>

    suspend fun setAccess(
        packageName: PackageName,
        enabled: Boolean,
    ): PolicyResult<CrossProfilePackageAccess>
}

internal fun updatedCrossProfilePackages(
    current: Set<String>,
    packageName: PackageName,
    enabled: Boolean,
): Set<String> = if (enabled) {
    current + packageName.value
} else {
    current - packageName.value
}

internal fun crossProfilePackageAccess(
    apiLevel: Int,
    current: Set<String>,
    packageName: PackageName,
): CrossProfilePackageAccess = when {
    apiLevel < Build.VERSION_CODES.R -> CrossProfilePackageAccess.Unsupported
    packageName.value in current -> CrossProfilePackageAccess.Enabled
    else -> CrossProfilePackageAccess.Disabled
}

class AndroidCrossProfilePackagePolicy(
    context: Context,
    private val admin: ComponentName,
) : CrossProfilePackagePolicy {
    private val policyManager = context.getSystemService(DevicePolicyManager::class.java)
    private val packageName = context.packageName

    override suspend fun accessFor(
        packageName: PackageName,
    ): PolicyResult<CrossProfilePackageAccess> = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return@withContext PolicyResult.Success(CrossProfilePackageAccess.Unsupported)
        }
        if (!isOwner()) {
            return@withContext PolicyResult.Failure("Harbor is not the profile owner", false)
        }
        runCatching {
            crossProfilePackageAccess(
                apiLevel = Build.VERSION.SDK_INT,
                current = policyManager.getCrossProfilePackages(admin),
                packageName = packageName,
            )
        }.fold(
            onSuccess = { PolicyResult.Success(it) },
            onFailure = { PolicyResult.Failure(it.message ?: it.javaClass.simpleName) },
        )
    }

    override suspend fun setAccess(
        packageName: PackageName,
        enabled: Boolean,
    ): PolicyResult<CrossProfilePackageAccess> = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return@withContext PolicyResult.Failure(
                "Cross-profile access requires Android 11 or newer",
                false,
            )
        }
        if (!isOwner()) {
            return@withContext PolicyResult.Failure("Harbor is not the profile owner", false)
        }
        runCatching {
            val current = policyManager.getCrossProfilePackages(admin)
            val updated = updatedCrossProfilePackages(current, packageName, enabled)
            if (updated != current) {
                // This setter replaces the complete profile-owner allowlist. Always
                // derive the update from Android's latest set so unrelated entries survive.
                policyManager.setCrossProfilePackages(admin, updated)
            }
            if (enabled) CrossProfilePackageAccess.Enabled else CrossProfilePackageAccess.Disabled
        }.fold(
            onSuccess = { PolicyResult.Success(it) },
            onFailure = { PolicyResult.Failure(it.message ?: it.javaClass.simpleName) },
        )
    }

    private fun isOwner(): Boolean = policyManager.isProfileOwnerApp(packageName)
}
