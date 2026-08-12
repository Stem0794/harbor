package com.monstera.harbor.ui.privacy

import com.monstera.harbor.core.data.ManagedApp
import com.monstera.harbor.core.topology.PackageName
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkAppPresentationTest {
    @Test
    fun availableUserAppKeepsAllCurrentActions() {
        val app = app(isLaunchable = true)

        assertEquals(WorkAppStatus.AVAILABLE, workAppStatus(app))
        assertEquals(
            listOf(
                WorkAppAction.OPEN,
                WorkAppAction.FREEZE,
                WorkAppAction.ADD_TO_LAUNCHER,
                WorkAppAction.DETAILS,
                WorkAppAction.UNINSTALL,
            ),
            workAppActions(app),
        )
    }

    @Test
    fun frozenUserAppCannotOpenUntilUnfrozen() {
        val app = app(isHidden = true, isLaunchable = true)

        assertEquals(WorkAppStatus.FROZEN, workAppStatus(app))
        assertEquals(
            listOf(
                WorkAppAction.UNFREEZE,
                WorkAppAction.ADD_TO_LAUNCHER,
                WorkAppAction.DETAILS,
                WorkAppAction.UNINSTALL,
            ),
            workAppActions(app),
        )
    }

    @Test
    fun systemAppKeepsReadOnlyActionSet() {
        val app = app(isSystem = true, isLaunchable = true)

        assertEquals(WorkAppStatus.READ_ONLY, workAppStatus(app))
        assertEquals(
            listOf(
                WorkAppAction.OPEN,
                WorkAppAction.ADD_TO_LAUNCHER,
                WorkAppAction.DETAILS,
            ),
            workAppActions(app),
        )
    }

    @Test
    fun nonLaunchableUserAppStillSupportsPolicyAndDetailsActions() {
        val app = app(isLaunchable = false)

        assertEquals(
            listOf(
                WorkAppAction.FREEZE,
                WorkAppAction.DETAILS,
                WorkAppAction.UNINSTALL,
            ),
            workAppActions(app),
        )
    }

    @Test
    fun hiddenStatusTakesPrecedenceOverSystemAndDisabled() {
        val app = app(
            isSystem = true,
            isEnabled = false,
            isHidden = true,
            isLaunchable = false,
        )

        assertEquals(WorkAppStatus.FROZEN, workAppStatus(app))
        assertEquals(listOf(WorkAppAction.DETAILS), workAppActions(app))
    }

    private fun app(
        isSystem: Boolean = false,
        isEnabled: Boolean = true,
        isHidden: Boolean = false,
        isLaunchable: Boolean = false,
    ) = ManagedApp(
        packageName = PackageName("org.example.app"),
        label = "Example",
        isSystem = isSystem,
        isEnabled = isEnabled,
        isHidden = isHidden,
        isLaunchable = isLaunchable,
    )
}
