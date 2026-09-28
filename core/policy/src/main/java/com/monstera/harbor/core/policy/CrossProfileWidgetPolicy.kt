package com.monstera.harbor.core.policy

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import com.monstera.harbor.core.topology.PackageName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed interface CrossProfileWidgetAccess {
    data object Enabled : CrossProfileWidgetAccess
    data object Disabled : CrossProfileWidgetAccess
}

interface CrossProfileWidgetPolicy {
    suspend fun accessFor(packageName: PackageName): PolicyResult<CrossProfileWidgetAccess>

    suspend fun setAccess(
        packageName: PackageName,
        enabled: Boolean,
    ): PolicyResult<CrossProfileWidgetAccess>
}

internal fun crossProfileWidgetAccess(
    current: Collection<String>,
    packageName: PackageName,
): CrossProfileWidgetAccess = if (packageName.value in current) {
    CrossProfileWidgetAccess.Enabled
} else {
    CrossProfileWidgetAccess.Disabled
}

/** True when Android's allowlist must change to reach the requested state. */
internal fun crossProfileWidgetUpdateNeeded(
    current: Collection<String>,
    packageName: PackageName,
    enabled: Boolean,
): Boolean = (packageName.value in current) != enabled

class AndroidCrossProfileWidgetPolicy(
    context: Context,
    private val admin: ComponentName,
) : CrossProfileWidgetPolicy {
    private val policyManager = context.getSystemService(DevicePolicyManager::class.java)
    private val packageName = context.packageName

    override suspend fun accessFor(
        packageName: PackageName,
    ): PolicyResult<CrossProfileWidgetAccess> = withContext(Dispatchers.IO) {
        if (!isOwner()) {
            return@withContext PolicyResult.Failure("Harbor is not the profile owner", false)
        }
        runCatching {
            crossProfileWidgetAccess(policyManager.getCrossProfileWidgetProviders(admin), packageName)
        }.fold(
            onSuccess = { PolicyResult.Success(it) },
            onFailure = { PolicyResult.Failure(it.message ?: it.javaClass.simpleName) },
        )
    }

    override suspend fun setAccess(
        packageName: PackageName,
        enabled: Boolean,
    ): PolicyResult<CrossProfileWidgetAccess> = withContext(Dispatchers.IO) {
        if (!isOwner()) {
            return@withContext PolicyResult.Failure("Harbor is not the profile owner", false)
        }
        runCatching {
            val current = policyManager.getCrossProfileWidgetProviders(admin)
            if (crossProfileWidgetUpdateNeeded(current, packageName, enabled)) {
                // add/remove edit a single entry, so unrelated providers are untouched.
                if (enabled) {
                    policyManager.addCrossProfileWidgetProvider(admin, packageName.value)
                } else {
                    policyManager.removeCrossProfileWidgetProvider(admin, packageName.value)
                }
            }
            if (enabled) CrossProfileWidgetAccess.Enabled else CrossProfileWidgetAccess.Disabled
        }.fold(
            onSuccess = { PolicyResult.Success(it) },
            onFailure = { PolicyResult.Failure(it.message ?: it.javaClass.simpleName) },
        )
    }

    private fun isOwner(): Boolean = policyManager.isProfileOwnerApp(packageName)
}
