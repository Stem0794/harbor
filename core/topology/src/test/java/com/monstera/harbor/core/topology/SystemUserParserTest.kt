package com.monstera.harbor.core.topology

import org.junit.Assert.assertEquals
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
}
