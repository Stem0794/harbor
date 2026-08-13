package com.monstera.harbor.ui.privacy

/**
 * Pure presentation helpers for the PR #6 UI-review follow-up.
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
