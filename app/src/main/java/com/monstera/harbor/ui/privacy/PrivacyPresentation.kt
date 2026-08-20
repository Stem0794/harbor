package com.monstera.harbor.ui.privacy

import com.monstera.harbor.core.data.ManagedApp
import com.monstera.harbor.core.policy.ManagedProfileProvisioningCapability
import com.monstera.harbor.ui.designsystem.HarborIconKind
import com.monstera.harbor.ui.designsystem.PrivacyFact
import com.monstera.harbor.ui.designsystem.StatusTone

data class WorkSpacePresentation(
    val title: String,
    val body: String,
    val tone: StatusTone,
)

internal enum class PersonalHeroAction {
    OPEN_WORK,
    PROVISION_WORK,
    SEND_FILES,
    ADVANCED,
}

internal data class PersonalHeroActions(
    val primary: PersonalHeroAction?,
    val quickLeft: PersonalHeroAction?,
    val quickRight: PersonalHeroAction?,
)

internal fun personalHeroActions(
    harborManagedProfile: Boolean,
    foreignProfile: Boolean,
    provisioningCapability: ManagedProfileProvisioningCapability,
): PersonalHeroActions = when {
    harborManagedProfile -> PersonalHeroActions(
        primary = PersonalHeroAction.OPEN_WORK,
        quickLeft = PersonalHeroAction.SEND_FILES,
        quickRight = PersonalHeroAction.ADVANCED,
    )
    foreignProfile && provisioningCapability is ManagedProfileProvisioningCapability.Allowed -> PersonalHeroActions(
        primary = PersonalHeroAction.PROVISION_WORK,
        quickLeft = null,
        quickRight = PersonalHeroAction.ADVANCED,
    )
    !foreignProfile && provisioningCapability is ManagedProfileProvisioningCapability.Allowed -> PersonalHeroActions(
        primary = PersonalHeroAction.PROVISION_WORK,
        quickLeft = null,
        quickRight = PersonalHeroAction.ADVANCED,
    )
    else -> PersonalHeroActions(
        primary = null,
        quickLeft = null,
        quickRight = PersonalHeroAction.ADVANCED,
    )
}

fun workSpacePresentation(
    harborManagedProfile: Boolean,
    foreignProfile: Boolean,
    provisioningCapability: ManagedProfileProvisioningCapability,
): WorkSpacePresentation = when {
    harborManagedProfile -> WorkSpacePresentation(
        title = "Work space is ready",
        body = "Your work apps and data stay separate from your personal apps.",
        tone = StatusTone.Positive,
    )
    foreignProfile && provisioningCapability is ManagedProfileProvisioningCapability.Allowed -> WorkSpacePresentation(
        title = "Work space setup is available",
        body = "Another profile exists, but Harbor does not manage it.",
        tone = StatusTone.Warning,
    )
    foreignProfile -> WorkSpacePresentation(
        title = "Work profile setup is unavailable",
        body = "Android has another profile and does not currently allow Harbor to create one.",
        tone = StatusTone.Warning,
    )
    provisioningCapability is ManagedProfileProvisioningCapability.Allowed -> WorkSpacePresentation(
        title = "Set up your Work space",
        body = "Keep selected apps and their data separate from your personal apps.",
        tone = StatusTone.Neutral,
    )
    else -> WorkSpacePresentation(
        title = "Work profile setup is unavailable",
        body = "Android's current device-management state prevents Harbor from creating its normal Work profile.",
        tone = StatusTone.Warning,
    )
}

fun privacyFacts(): List<PrivacyFact> = listOf(
    PrivacyFact("No network permission", "Harbor itself cannot access the internet.", HarborIconKind.Network),
    PrivacyFact("No analytics", "Harbor does not collect usage or telemetry.", HarborIconKind.Analytics),
    PrivacyFact("Local only", "Harbor's app catalog and diagnostics stay on this device.", HarborIconKind.Device),
)

fun appStatusLabel(app: ManagedApp): String = when {
    app.isHidden -> "Frozen"
    app.isSystem -> "Read-only"
    !app.isEnabled -> "Disabled"
    else -> "Available"
}

fun appStatusTone(app: ManagedApp): StatusTone = when {
    app.isHidden -> StatusTone.Neutral
    app.isSystem -> StatusTone.Warning
    !app.isEnabled -> StatusTone.Warning
    else -> StatusTone.Positive
}
