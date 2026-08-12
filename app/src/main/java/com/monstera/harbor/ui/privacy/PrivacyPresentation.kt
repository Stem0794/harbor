package com.monstera.harbor.ui.privacy

import com.monstera.harbor.core.data.ManagedApp
import com.monstera.harbor.ui.designsystem.HarborIconKind
import com.monstera.harbor.ui.designsystem.PrivacyFact
import com.monstera.harbor.ui.designsystem.StatusTone

data class WorkSpacePresentation(
    val title: String,
    val body: String,
    val tone: StatusTone,
    val primaryAction: String?,
)

fun workSpacePresentation(
    harborManagedProfile: Boolean,
    foreignProfile: Boolean,
    provisioningAllowed: Boolean,
): WorkSpacePresentation = when {
    harborManagedProfile -> WorkSpacePresentation(
        title = "Work space is ready",
        body = "Your work apps and data stay separate from your personal apps.",
        tone = StatusTone.Positive,
        primaryAction = "Open Work",
    )
    foreignProfile && provisioningAllowed -> WorkSpacePresentation(
        title = "Work space setup is available",
        body = "Another profile exists, but Harbor does not manage it.",
        tone = StatusTone.Warning,
        primaryAction = "Create Work space",
    )
    foreignProfile -> WorkSpacePresentation(
        title = "Work profile setup is unavailable",
        body = "Android has another profile and does not currently allow Harbor to create one.",
        tone = StatusTone.Warning,
        primaryAction = null,
    )
    provisioningAllowed -> WorkSpacePresentation(
        title = "Set up your Work space",
        body = "Keep selected apps and their data separate from your personal apps.",
        tone = StatusTone.Neutral,
        primaryAction = "Create Work space",
    )
    else -> WorkSpacePresentation(
        title = "Work profile setup is unavailable",
        body = "Android does not currently allow another Work profile.",
        tone = StatusTone.Warning,
        primaryAction = null,
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
