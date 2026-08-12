package com.monstera.harbor.ui.privacy

import com.monstera.harbor.core.data.ManagedApp
import com.monstera.harbor.core.topology.PackageName
import com.monstera.harbor.ui.designsystem.StatusTone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PrivacyPresentationTest {
    @Test
    fun readyWorkSpaceUsesOpenAction() {
        val result = workSpacePresentation(
            harborManagedProfile = true,
            foreignProfile = false,
            provisioningAllowed = false,
        )

        assertEquals("Work space is ready", result.title)
        assertEquals("Open Work", result.primaryAction)
        assertEquals(StatusTone.Positive, result.tone)
    }

    @Test
    fun blockedSetupDoesNotOfferUnsafeCreateAction() {
        val result = workSpacePresentation(
            harborManagedProfile = false,
            foreignProfile = true,
            provisioningAllowed = false,
        )

        assertNull(result.primaryAction)
        assertEquals(StatusTone.Warning, result.tone)
    }

    @Test
    fun appStatusMappingIsExplicitAndNotColorOnly() {
        assertEquals("Frozen", appStatusLabel(app(hidden = true)))
        assertEquals("Read-only", appStatusLabel(app(system = true)))
        assertEquals("Disabled", appStatusLabel(app(enabled = false)))
        assertEquals("Available", appStatusLabel(app()))
    }

    private fun app(
        system: Boolean = false,
        hidden: Boolean = false,
        enabled: Boolean = true,
    ) = ManagedApp(
        packageName = PackageName("org.example.app"),
        label = "Example",
        isSystem = system,
        isEnabled = enabled,
        isHidden = hidden,
        isLaunchable = true,
    )
}
