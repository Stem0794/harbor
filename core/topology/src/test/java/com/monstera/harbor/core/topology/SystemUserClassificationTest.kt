package com.monstera.harbor.core.topology

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Regression coverage for Android user classification and switchability. */
class SystemUserClassificationTest {
    @Test
    fun normalSystemOwnerFullUserRemainsSwitchable() {
        val owner = SystemUser(
            id = AndroidUserId(0),
            name = "Owner",
            // FLAG_FULL | FLAG_SYSTEM
            flags = 0x00000400 or 0x00000800,
            isRunning = true,
        )

        assertTrue(owner.isSwitchableFullUser)
    }

    @Test
    fun secondaryFullUserIsSwitchable() {
        val secondary = SystemUser(
            id = AndroidUserId(10),
            name = "Harbor Lab",
            // FLAG_FULL
            flags = 0x00000400,
            isRunning = false,
        )

        assertTrue(secondary.isSwitchableFullUser)
    }

    @Test
    fun managedProfileIsNotSwitchableFullUser() {
        val profile = SystemUser(
            id = AndroidUserId(11),
            name = "Work profile",
            // FLAG_FULL | FLAG_MANAGED_PROFILE | FLAG_PROFILE
            flags = 0x00000400 or 0x00000020 or 0x00001000,
            isRunning = true,
        )

        assertTrue(profile.isProfile)
        assertFalse(profile.isSwitchableFullUser)
    }

    @Test
    fun headlessSystemUserIsNotSwitchable() {
        val headlessSystem = SystemUser(
            id = AndroidUserId(0),
            name = "System",
            // FLAG_SYSTEM without FLAG_FULL
            flags = 0x00000800,
            isRunning = true,
        )

        assertFalse(headlessSystem.isSwitchableFullUser)
    }

    @Test
    fun disabledOrRestrictedUsersAreNotSwitchable() {
        val restricted = SystemUser(
            id = AndroidUserId(12),
            name = "Restricted",
            // FLAG_FULL | FLAG_RESTRICTED
            flags = 0x00000400 or 0x00000008,
            isRunning = false,
        )
        val disabled = SystemUser(
            id = AndroidUserId(13),
            name = "Disabled",
            // FLAG_FULL | FLAG_DISABLED
            flags = 0x00000400 or 0x00000040,
            isRunning = false,
        )

        assertFalse(restricted.isSwitchableFullUser)
        assertFalse(disabled.isSwitchableFullUser)
    }
}
