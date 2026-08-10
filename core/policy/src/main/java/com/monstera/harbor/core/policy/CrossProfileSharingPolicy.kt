package com.monstera.harbor.core.policy

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.os.UserManager

/**
 * The narrow cross-profile data flow Harbor supports: personal -> work files.
 *
 * Work -> personal sharing remains governed by Android's default work-profile
 * isolation policy. These filters mirror Android's default ACTION_SEND filters
 * and make the intended direction explicit for OEMs that omit them.
 */
object CrossProfileSharingPolicy {
    internal data class IntentRule(
        val action: String,
        val direction: Int,
        val mimeType: String = "*/*",
    )

    internal val parentToManagedFileRules = listOf(
        IntentRule(
            action = Intent.ACTION_SEND,
            direction = DevicePolicyManager.FLAG_MANAGED_CAN_ACCESS_PARENT,
        ),
        IntentRule(
            action = Intent.ACTION_SEND_MULTIPLE,
            direction = DevicePolicyManager.FLAG_MANAGED_CAN_ACCESS_PARENT,
        ),
    )

    fun apply(
        policyManager: DevicePolicyManager,
        admin: ComponentName,
    ): PolicyResult<Unit> = runCatching {
        // This is a directional policy: allow content into the work profile,
        // while leaving work -> personal sharing disabled by default.
        policyManager.clearUserRestriction(
            admin,
            UserManager.DISALLOW_SHARE_INTO_MANAGED_PROFILE,
        )
        parentToManagedFileRules.forEach { rule ->
            policyManager.addCrossProfileIntentFilter(
                admin,
                IntentFilter().apply {
                    addAction(rule.action)
                    addCategory(Intent.CATEGORY_DEFAULT)
                    addDataType(rule.mimeType)
                },
                rule.direction,
            )
        }
    }.fold(
        onSuccess = { PolicyResult.Success(Unit) },
        onFailure = { PolicyResult.Failure(it.message ?: it.javaClass.simpleName) },
    )
}
