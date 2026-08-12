package com.monstera.harbor.ui.privacy

import com.monstera.harbor.core.topology.ProfileOwnership
import com.monstera.harbor.core.topology.ProfileTopology

/**
 * Presentation-only states for the Personal-side work-space card.
 *
 * This intentionally mirrors the existing PersonalProfileScreen provisioning logic so the UI
 * refactor cannot accidentally change ownership or provisioning semantics.
 */
internal enum class WorkSpaceSetupState {
    HARBOR_READY,
    FOREIGN_PROFILE_PRESENT_PROVISIONING_ALLOWED,
    BLOCKED_BY_EXISTING_PROFILE,
    READY_TO_CREATE,
    BLOCKED_BY_ANDROID,
}

internal fun resolveWorkSpaceSetupState(
    topology: ProfileTopology,
    provisioningAllowed: Boolean,
): WorkSpaceSetupState {
    val associatedHarbor = topology.associatedProfiles.any {
        !it.isCurrent && it.ownership == ProfileOwnership.HARBOR_INSTALLED_OWNER_UNKNOWN
    }
    val hasForeignOrUnknownProfile = topology.associatedProfiles.any {
        !it.isCurrent && it.ownership == ProfileOwnership.FOREIGN_OR_UNKNOWN
    }

    return when {
        associatedHarbor -> WorkSpaceSetupState.HARBOR_READY
        hasForeignOrUnknownProfile && provisioningAllowed ->
            WorkSpaceSetupState.FOREIGN_PROFILE_PRESENT_PROVISIONING_ALLOWED
        hasForeignOrUnknownProfile -> WorkSpaceSetupState.BLOCKED_BY_EXISTING_PROFILE
        provisioningAllowed -> WorkSpaceSetupState.READY_TO_CREATE
        else -> WorkSpaceSetupState.BLOCKED_BY_ANDROID
    }
}

internal data class HarborPrivacyFact(
    val title: String,
    val detail: String,
)

/**
 * Claims shown in the Direction 2 privacy card must describe Harbor itself, not every app inside
 * the work profile. In particular, Harbor does not provide a profile-wide network firewall.
 */
internal val harborPrivacyFacts = listOf(
    HarborPrivacyFact(
        title = "No network permission",
        detail = "Harbor itself cannot access the internet.",
    ),
    HarborPrivacyFact(
        title = "No analytics",
        detail = "Harbor does not collect usage or telemetry.",
    ),
    HarborPrivacyFact(
        title = "Local only",
        detail = "Harbor's app catalog and diagnostics stay on this device.",
    ),
)
