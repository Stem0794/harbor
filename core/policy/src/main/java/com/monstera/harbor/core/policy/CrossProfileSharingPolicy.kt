package com.monstera.harbor.core.policy

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.os.UserManager

/**
 * The narrow cross-profile data flow Harbor supports: personal -> work files.
 *
 * Personal apps may explicitly send selected files to a matching activity in
 * the work profile. Harbor does not register work-to-personal export targets.
 * These filters mirror Android's default sharing filters and make the intended
 * direction explicit for OEMs that omit them.
 */
object CrossProfileSharingPolicy {
    internal data class IntentRule(
        val action: String,
        val direction: Int,
        val mimeType: String? = "*/*",
        val categories: Set<String> = setOf(Intent.CATEGORY_DEFAULT),
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
        // Some OEM file managers omit the MIME type when implementing their
        // own "move to work" or share action. Keep an action-only rule as a
        // fallback; the receiving activity still validates the URI scheme.
        IntentRule(
            action = Intent.ACTION_SEND,
            direction = DevicePolicyManager.FLAG_MANAGED_CAN_ACCESS_PARENT,
            mimeType = null,
        ),
        IntentRule(
            action = Intent.ACTION_SEND_MULTIPLE,
            direction = DevicePolicyManager.FLAG_MANAGED_CAN_ACCESS_PARENT,
            mimeType = null,
        ),
    )

    fun apply(
        policyManager: DevicePolicyManager,
        admin: ComponentName,
    ): PolicyResult<Unit> = runCatching {
        // This is a directional policy: allow explicit personal content imports
        // into the work profile, without adding work-to-personal export targets.
        // Replacing Harbor-owned filters keeps repeated setup clicks idempotent
        // and removes filters from older Harbor builds before applying the
        // current allowlist. System defaults are not removed by this call.
        policyManager.clearCrossProfileIntentFilters(admin)
        policyManager.clearUserRestriction(
            admin,
            UserManager.DISALLOW_SHARE_INTO_MANAGED_PROFILE,
        )
        parentToManagedFileRules.forEach { rule ->
            policyManager.addCrossProfileIntentFilter(
                admin,
                IntentFilter().apply {
                    addAction(rule.action)
                    rule.categories.forEach(::addCategory)
                    rule.mimeType?.let(::addDataType)
                },
                rule.direction,
            )
        }
    }.fold(
        onSuccess = { PolicyResult.Success(Unit) },
        onFailure = { PolicyResult.Failure(it.message ?: it.javaClass.simpleName) },
    )
}
