package com.monstera.harbor.ui.privacy

import com.monstera.harbor.core.topology.AssociatedProfile
import com.monstera.harbor.core.topology.LocalProfileKind
import com.monstera.harbor.core.topology.ProfileOwnership
import com.monstera.harbor.core.topology.ProfileTopology
import org.junit.Assert.assertEquals
import org.junit.Test

class PrivacyDashboardPresentationTest {
    @Test
    fun harborAssociatedProfileIsPresentedAsReady() {
        val topology = topologyWith(
            AssociatedProfile(
                isCurrent = false,
                kind = LocalProfileKind.UNKNOWN_PROFILE,
                ownership = ProfileOwnership.HARBOR_INSTALLED_OWNER_UNKNOWN,
            ),
        )

        assertEquals(
            WorkSpaceSetupState.HARBOR_READY,
            resolveWorkSpaceSetupState(topology, provisioningAllowed = false),
        )
    }

    @Test
    fun foreignProfileDoesNotHideThatAndroidStillAllowsProvisioning() {
        val topology = topologyWith(
            AssociatedProfile(
                isCurrent = false,
                kind = LocalProfileKind.UNKNOWN_PROFILE,
                ownership = ProfileOwnership.FOREIGN_OR_UNKNOWN,
            ),
        )

        assertEquals(
            WorkSpaceSetupState.FOREIGN_PROFILE_PRESENT_PROVISIONING_ALLOWED,
            resolveWorkSpaceSetupState(topology, provisioningAllowed = true),
        )
    }

    @Test
    fun foreignProfileIsPresentedAsBlockingWhenProvisioningIsUnavailable() {
        val topology = topologyWith(
            AssociatedProfile(
                isCurrent = false,
                kind = LocalProfileKind.UNKNOWN_PROFILE,
                ownership = ProfileOwnership.FOREIGN_OR_UNKNOWN,
            ),
        )

        assertEquals(
            WorkSpaceSetupState.BLOCKED_BY_EXISTING_PROFILE,
            resolveWorkSpaceSetupState(topology, provisioningAllowed = false),
        )
    }

    @Test
    fun noAssociatedProfileAndProvisioningAllowedIsReadyToCreate() {
        assertEquals(
            WorkSpaceSetupState.READY_TO_CREATE,
            resolveWorkSpaceSetupState(topologyWith(), provisioningAllowed = true),
        )
    }

    @Test
    fun noAssociatedProfileAndProvisioningUnavailableIsAndroidBlocked() {
        assertEquals(
            WorkSpaceSetupState.BLOCKED_BY_ANDROID,
            resolveWorkSpaceSetupState(topologyWith(), provisioningAllowed = false),
        )
    }

    private fun topologyWith(vararg profiles: AssociatedProfile) = ProfileTopology(
        localKind = LocalProfileKind.FULL_USER,
        harborIsProfileOwner = false,
        associatedProfiles = profiles.toList(),
        supportsMultipleUsers = true,
    )
}
