package com.monstera.harbor.ui

import com.monstera.harbor.launcherShortcutId
import com.monstera.harbor.core.data.ShortcutIdValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShortcutRequestPresentationTest {
    @Test
    fun retriesReuseThePackageShortcutId() {
        val first = launcherShortcutId("com.example.app")

        assertEquals(first, launcherShortcutId("com.example.app"))
        assertEquals(first, ShortcutIdValidator.normalize(first))
        assertFalse(first.contains("com.example.app"))
        assertFalse(first == launcherShortcutId("com.example.other"))
    }

    @Test
    fun acceptedRequestExplainsThatTheLauncherStillControlsPinning() {
        val result = shortcutRequestPresentation(requestAccepted = true)

        assertEquals("Pin request sent", result.title)
        assertEquals("Try again", result.actionLabel)
        assertTrue(result.body.contains("accepted the request"))
        assertTrue(result.body.contains("no confirmation"))
        assertFalse(result.body.contains("shortcut was pinned"))
    }

    @Test
    fun rejectedRequestDoesNotClaimThatShortcutWasPinned() {
        val result = shortcutRequestPresentation(requestAccepted = false)

        assertEquals("Shortcut request unavailable", result.title)
        assertEquals("Try again", result.actionLabel)
        assertTrue(result.body.contains("could not ask"))
    }
}
