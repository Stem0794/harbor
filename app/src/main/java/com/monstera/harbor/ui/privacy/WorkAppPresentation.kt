package com.monstera.harbor.ui.privacy

import com.monstera.harbor.core.policy.CrossProfilePackageAccess

/**
 * Pure presentation helpers for Work app interaction and copy.
 *
 * These helpers intentionally encode interaction/copy rules only. They must not own policy,
 * topology, profile, package-manager, or privileged behavior.
 */
internal enum class WorkAppRowClickIntent {
    ToggleSelection,
    OpenActions,
    NoOp,
}

internal fun workAppRowClickIntent(
    selectionMode: Boolean,
    isSystem: Boolean,
): WorkAppRowClickIntent = when {
    selectionMode && isSystem -> WorkAppRowClickIntent.NoOp
    selectionMode -> WorkAppRowClickIntent.ToggleSelection
    else -> WorkAppRowClickIntent.OpenActions
}

internal fun workAppCountLabel(
    totalCount: Int,
    visibleCount: Int,
    query: String,
): String {
    val count = if (query.isBlank()) totalCount else visibleCount
    return "$count apps"
}

internal fun launcherShortcutActionLabel(isHidden: Boolean): String =
    if (isHidden) "Add & unfreeze" else "Add to launcher"

internal data class CrossProfileAccessPresentation(
    val body: String,
    val checked: Boolean,
    val toggleEnabled: Boolean,
)

internal fun crossProfileAccessPresentation(
    access: CrossProfilePackageAccess?,
    error: String?,
    busy: Boolean,
): CrossProfileAccessPresentation = when {
    error != null -> CrossProfileAccessPresentation(
        body = "Unavailable · $error",
        checked = false,
        toggleEnabled = false,
    )
    access == null -> CrossProfileAccessPresentation(
        body = "Checking Android policy…",
        checked = false,
        toggleEnabled = false,
    )
    access == CrossProfilePackageAccess.Unsupported -> CrossProfileAccessPresentation(
        body = "Requires Android 11 or newer",
        checked = false,
        toggleEnabled = false,
    )
    access == CrossProfilePackageAccess.Enabled -> CrossProfileAccessPresentation(
        body = "Allowed by Harbor · Android consent is still required",
        checked = true,
        toggleEnabled = !busy,
    )
    else -> CrossProfileAccessPresentation(
        body = "Off · Android consent is still required",
        checked = false,
        toggleEnabled = !busy,
    )
}
