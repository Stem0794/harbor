package com.monstera.harbor.core.topology

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CloneProfileRelationshipResolverTest {
    @Test
    fun resolvesSimpleCurrentFullUserAndManagedProfilePair() {
        val owner = fullUser(0, running = true)
        val work = managedProfile(10)

        val result = CloneProfileRelationshipResolver.resolve(
            currentFullUser = owner.id,
            users = listOf(owner, work),
        )

        assertTrue(result is PrivilegedResult.Success)
        val relationship = (result as PrivilegedResult.Success).value
        assertEquals(owner, relationship.sourceFullUser)
        assertEquals(work, relationship.targetManagedProfile)
    }

    @Test
    fun rejectsPersonalWorkAndSiblingProfileAsAmbiguous() {
        val result = CloneProfileRelationshipResolver.resolve(
            currentFullUser = AndroidUserId(0),
            users = listOf(
                fullUser(0, running = true),
                managedProfile(10),
                SystemUser(
                    id = AndroidUserId(11),
                    name = "Private",
                    flags = 0x00001000,
                    isRunning = true,
                ),
            ),
        )

        assertTrue(result is PrivilegedResult.Failure)
    }

    @Test
    fun rejectsUnknownCurrentUser() {
        val result = CloneProfileRelationshipResolver.resolve(
            currentFullUser = AndroidUserId(12),
            users = listOf(fullUser(0, running = true), managedProfile(10)),
        )

        assertTrue(result is PrivilegedResult.Failure)
    }

    private fun fullUser(id: Int, running: Boolean) = SystemUser(
        id = AndroidUserId(id),
        name = if (id == 0) "Owner" else "User $id",
        flags = 0x00000400,
        isRunning = running,
    )

    private fun managedProfile(id: Int) = SystemUser(
        id = AndroidUserId(id),
        name = "Work",
        flags = 0x00000020 or 0x00001000,
        isRunning = true,
    )
}
