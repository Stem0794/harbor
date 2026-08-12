package com.monstera.harbor.ui.privacy

import com.monstera.harbor.core.data.ManagedApp

internal enum class WorkAppStatus {
    FROZEN,
    READ_ONLY,
    DISABLED,
    AVAILABLE,
}

internal enum class WorkAppAction {
    OPEN,
    FREEZE,
    UNFREEZE,
    ADD_TO_LAUNCHER,
    DETAILS,
    UNINSTALL,
}

/** Matches the status precedence used by the current ManagedAppCard. */
internal fun workAppStatus(app: ManagedApp): WorkAppStatus = when {
    app.isHidden -> WorkAppStatus.FROZEN
    app.isSystem -> WorkAppStatus.READ_ONLY
    !app.isEnabled -> WorkAppStatus.DISABLED
    else -> WorkAppStatus.AVAILABLE
}

/**
 * Presentation mapping for the redesigned action sheet.
 *
 * Keep this aligned with the action availability in the current WorkProfileScreen. The UI may
 * become denser, but it must not quietly gain or lose an operation during the refactor.
 */
internal fun workAppActions(app: ManagedApp): List<WorkAppAction> = buildList {
    if (app.isLaunchable && !app.isHidden) add(WorkAppAction.OPEN)

    if (!app.isSystem) {
        add(if (app.isHidden) WorkAppAction.UNFREEZE else WorkAppAction.FREEZE)
    }

    if (app.isLaunchable) add(WorkAppAction.ADD_TO_LAUNCHER)

    add(WorkAppAction.DETAILS)

    if (!app.isSystem) add(WorkAppAction.UNINSTALL)
}
