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
    fun readyHasOpenSendAndAdvancedActions() {
        val result = personalHeroActions(
            harborManagedProfile = true,
            foreignProfile = false,
            provisioningAllowed = false,
        )

        assertEquals(PersonalHeroAction.OPEN_WORK, result.primary)
        assertEquals(PersonalHeroAction.SEND_FILES, result.quickLeft)
        assertEquals(PersonalHeroAction.ADVANCED, result.quickRight)
        assertEquals(0, listOf(result.primary, result.quickLeft, result.quickRight).count { it == PersonalHeroAction.PROVISION_WORK })
    }

    @Test
    fun setupAllowedHasSingleProvisionActionAndNoSendFiles() {
        val result = personalHeroActions(
            harborManagedProfile = false,
            foreignProfile = false,
            provisioningAllowed = true,
        )

        assertEquals(PersonalHeroAction.PROVISION_WORK, result.primary)
        assertNull(result.quickLeft)
        assertEquals(PersonalHeroAction.ADVANCED, result.quickRight)
        assertEquals(1, listOf(result.primary, result.quickLeft, result.quickRight).count { it == PersonalHeroAction.PROVISION_WORK })
    }

    @Test
    fun foreignProfileAllowedHasSingleProvisionActionAndNoSendFiles() {
        val result = personalHeroActions(
            harborManagedProfile = false,
            foreignProfile = true,
            provisioningAllowed = true,
        )

        assertEquals(PersonalHeroAction.PROVISION_WORK, result.primary)
        assertNull(result.quickLeft)
        assertEquals(PersonalHeroAction.ADVANCED, result.quickRight)
        assertEquals(1, listOf(result.primary, result.quickLeft, result.quickRight).count { it == PersonalHeroAction.PROVISION_WORK })
    }

    @Test
    fun blockedForeignProfileHasNoProvisionOrSendFiles() {
        val result = personalHeroActions(
            harborManagedProfile = false,
            foreignProfile = true,
            provisioningAllowed = false,
        )

        assertNull(result.primary)
        assertNull(result.quickLeft)
        assertEquals(PersonalHeroAction.ADVANCED, result.quickRight)
        assertEquals(0, listOf(result.primary, result.quickLeft, result.quickRight).count { it == PersonalHeroAction.PROVISION_WORK })
    }

    @Test
    fun blockedGenericSetupHasNoProvisionOrSendFiles() {
        val result = personalHeroActions(
            harborManagedProfile = false,
            foreignProfile = false,
            provisioningAllowed = false,
        )

        assertNull(result.primary)
        assertNull(result.quickLeft)
        assertEquals(PersonalHeroAction.ADVANCED, result.quickRight)
        assertEquals(0, listOf(result.primary, result.quickLeft, result.quickRight).count { it == PersonalHeroAction.PROVISION_WORK })
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
