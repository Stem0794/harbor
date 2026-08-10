package com.monstera.harbor.core.topology

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SystemUserParserTest {
    @Test fun parsesAospUserList() {
        val users = SystemUserParser.parse(
            """
            Users:
              UserInfo{0:Owner:13} running
              UserInfo{10:Harbor Lab:10}
            """.trimIndent(),
        )
        assertEquals(listOf(0, 10), users.map { it.id.value })
        assertEquals("Harbor Lab", users[1].name)
        assertTrue(users[0].isRunning)
        assertTrue(users.all { it.isSwitchableFullUser })
    }

    @Test fun identifiesProfilesAndNonSwitchableSystemUsers() {
        val users = SystemUserParser.parse(
            """
            Users:
              UserInfo{0:System:800} running
              UserInfo{10:Owner:413} running
              UserInfo{11:Work profile:1030}
            """.trimIndent(),
        )
        assertTrue(!users[0].isSwitchableFullUser)
        assertTrue(users[1].isSwitchableFullUser)
        assertTrue(users[2].isProfile)
        assertTrue(!users[2].isSwitchableFullUser)
    }

    @Test fun parsesModernAospHexFlagsAndStatusSuffixes() {
        val result = SystemUserParser.parseResult(
            """
            Users:
              UserInfo{0:Owner:c13} running
              UserInfo{10:Harbor Work:1030} running unlocked
            """.trimIndent(),
        )

        assertTrue(result.isComplete)
        assertEquals(listOf(0xC13, 0x1030), result.users.map(SystemUser::flags))
        assertTrue(result.users.all(SystemUser::isRunning))
    }

    @Test fun reportsMalformedUserLinesInsteadOfPartiallyTrustingThem() {
        val result = SystemUserParser.parseResult(
            """
            Users:
              UserInfo{0:Owner:413} running
              UserInfo{10:Broken:not-hex} running
            """.trimIndent(),
        )

        assertFalse(result.isComplete)
        assertEquals(listOf(0), result.users.map { it.id.value })
        assertEquals(1, result.malformedUserLines.size)
    }

    @Test fun ignoresUnrelatedOemHeadersWithoutInventingUsers() {
        val result = SystemUserParser.parseResult(
            """
            OEM user service dump
            Users:
              UserInfo{0:Phone owner:413} running
            Device policy state follows
            """.trimIndent(),
        )

        assertTrue(result.isComplete)
        assertEquals(listOf(0), result.users.map { it.id.value })
    }
}
