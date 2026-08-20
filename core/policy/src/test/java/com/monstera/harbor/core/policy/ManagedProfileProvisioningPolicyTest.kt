package com.monstera.harbor.core.policy

import android.app.admin.DevicePolicyManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ManagedProfileProvisioningPolicyTest {
    @Test
    fun allowedCapabilityUsesManagedProfileProvisioningActionOnMinSdk29Path() {
        var checkedAction: String? = null
        val policy = AndroidManagedProfileProvisioningPolicy { action ->
            checkedAction = action
            true
        }

        assertEquals(ManagedProfileProvisioningCapability.Allowed, policy.capability())
        assertEquals(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE, checkedAction)
    }

    @Test
    fun blockedCapabilityReportsAndroidManagementState() {
        val policy = AndroidManagedProfileProvisioningPolicy { false }

        assertEquals(
            ManagedProfileProvisioningCapability.Blocked(
                ManagedProfileProvisioningBlockReason.ANDROID_MANAGEMENT_STATE,
            ),
            policy.capability(),
        )
    }

    @Test
    fun failedCapabilityCheckFailsClosed() {
        val policy = AndroidManagedProfileProvisioningPolicy {
            error("Device policy service unavailable")
        }

        assertEquals(
            ManagedProfileProvisioningCapability.Blocked(
                ManagedProfileProvisioningBlockReason.CAPABILITY_CHECK_FAILED,
            ),
            policy.capability(),
        )
    }

    @Test
    fun allowedPreflightLaunchesProvisioning() {
        var launched = false
        val preflight = ManagedProfileProvisioningPreflight(
            policy = FixedProvisioningPolicy(ManagedProfileProvisioningCapability.Allowed),
        )

        val result = preflight.start { launched = true }

        assertTrue(launched)
        assertEquals(ManagedProfileProvisioningStartResult.Started, result)
    }

    @Test
    fun blockedPreflightDoesNotLaunchProvisioning() {
        var launched = false
        val capability = ManagedProfileProvisioningCapability.Blocked(
            ManagedProfileProvisioningBlockReason.ANDROID_MANAGEMENT_STATE,
        )
        val preflight = ManagedProfileProvisioningPreflight(FixedProvisioningPolicy(capability))

        val result = preflight.start { launched = true }

        assertFalse(launched)
        assertEquals(ManagedProfileProvisioningStartResult.Blocked(capability), result)
    }

    private class FixedProvisioningPolicy(
        private val value: ManagedProfileProvisioningCapability,
    ) : ManagedProfileProvisioningPolicy {
        override fun capability(): ManagedProfileProvisioningCapability = value
    }
}
