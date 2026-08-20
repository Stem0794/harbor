package com.monstera.harbor.core.policy

import android.app.admin.DevicePolicyManager

enum class ManagedProfileProvisioningBlockReason {
    ANDROID_MANAGEMENT_STATE,
    CAPABILITY_CHECK_FAILED,
}

sealed interface ManagedProfileProvisioningCapability {
    data object Allowed : ManagedProfileProvisioningCapability

    data class Blocked(
        val reason: ManagedProfileProvisioningBlockReason,
    ) : ManagedProfileProvisioningCapability
}

interface ManagedProfileProvisioningPolicy {
    fun capability(): ManagedProfileProvisioningCapability
}

class AndroidManagedProfileProvisioningPolicy internal constructor(
    private val isProvisioningAllowed: (String) -> Boolean,
) : ManagedProfileProvisioningPolicy {
    constructor(policyManager: DevicePolicyManager) : this(policyManager::isProvisioningAllowed)

    override fun capability(): ManagedProfileProvisioningCapability = runCatching {
        isProvisioningAllowed(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE)
    }.fold(
        onSuccess = { allowed ->
            if (allowed) {
                ManagedProfileProvisioningCapability.Allowed
            } else {
                ManagedProfileProvisioningCapability.Blocked(
                    ManagedProfileProvisioningBlockReason.ANDROID_MANAGEMENT_STATE,
                )
            }
        },
        onFailure = {
            ManagedProfileProvisioningCapability.Blocked(
                ManagedProfileProvisioningBlockReason.CAPABILITY_CHECK_FAILED,
            )
        },
    )
}

sealed interface ManagedProfileProvisioningStartResult {
    data object Started : ManagedProfileProvisioningStartResult

    data class Blocked(
        val capability: ManagedProfileProvisioningCapability.Blocked,
    ) : ManagedProfileProvisioningStartResult
}

/** Re-checks Android's current management state immediately before launching setup. */
class ManagedProfileProvisioningPreflight(
    private val policy: ManagedProfileProvisioningPolicy,
) {
    fun start(launchProvisioning: () -> Unit): ManagedProfileProvisioningStartResult =
        when (val capability = policy.capability()) {
            ManagedProfileProvisioningCapability.Allowed -> {
                launchProvisioning()
                ManagedProfileProvisioningStartResult.Started
            }

            is ManagedProfileProvisioningCapability.Blocked ->
                ManagedProfileProvisioningStartResult.Blocked(capability)
        }
}
