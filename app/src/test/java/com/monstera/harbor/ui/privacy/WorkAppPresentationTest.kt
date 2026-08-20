package com.monstera.harbor.ui.privacy

import com.monstera.harbor.core.policy.CrossProfilePackageAccess
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkAppPresentationTest {
    @Test
    fun systemAppTapIsIgnoredDuringSelection() {
        assertEquals(
            WorkAppRowClickIntent.NoOp,
            workAppRowClickIntent(selectionMode = true, isSystem = true),
        )
    }

    @Test
    fun userAppTapTogglesDuringSelection() {
        assertEquals(
            WorkAppRowClickIntent.ToggleSelection,
            workAppRowClickIntent(selectionMode = true, isSystem = false),
        )
    }

    @Test
    fun normalModeOpensActions() {
        assertEquals(
            WorkAppRowClickIntent.OpenActions,
            workAppRowClickIntent(selectionMode = false, isSystem = true),
        )
    }

    @Test
    fun blankQueryUsesTotalAppCount() {
        assertEquals(
            "18 apps",
            workAppCountLabel(totalCount = 18, visibleCount = 2, query = ""),
        )
    }

    @Test
    fun activeQueryUsesVisibleAppCount() {
        assertEquals(
            "2 apps",
            workAppCountLabel(totalCount = 18, visibleCount = 2, query = "fire"),
        )
    }

    @Test
    fun frozenShortcutCopyExplainsUnfreeze() {
        assertEquals("Add & unfreeze", launcherShortcutActionLabel(isHidden = true))
    }

    @Test
    fun normalShortcutCopyUsesStandardLabel() {
        assertEquals("Add to launcher", launcherShortcutActionLabel(isHidden = false))
    }

    @Test
    fun crossProfileAccessDefaultsToExplicitlyOff() {
        val result = crossProfileAccessPresentation(
            access = CrossProfilePackageAccess.Disabled,
            error = null,
            busy = false,
        )

        assertEquals("Off · Android consent is still required", result.body)
        assertFalse(result.checked)
        assertTrue(result.toggleEnabled)
    }

    @Test
    fun crossProfileAccessExplainsAndroidConsentWhenEnabled() {
        val result = crossProfileAccessPresentation(
            access = CrossProfilePackageAccess.Enabled,
            error = null,
            busy = false,
        )

        assertEquals("Allowed by Harbor · Android consent is still required", result.body)
        assertTrue(result.checked)
        assertTrue(result.toggleEnabled)
    }

    @Test
    fun unsupportedCrossProfileAccessCannotBeToggled() {
        val result = crossProfileAccessPresentation(
            access = CrossProfilePackageAccess.Unsupported,
            error = null,
            busy = false,
        )

        assertEquals("Requires Android 11 or newer", result.body)
        assertFalse(result.toggleEnabled)
    }

    @Test
    fun crossProfileAccessCannotBeToggledDuringAnotherPolicyOperation() {
        val result = crossProfileAccessPresentation(
            access = CrossProfilePackageAccess.Disabled,
            error = null,
            busy = true,
        )

        assertFalse(result.checked)
        assertFalse(result.toggleEnabled)
    }
}
