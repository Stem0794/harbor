package com.monstera.harbor.core.data

import com.monstera.harbor.core.topology.AndroidUserId
import com.monstera.harbor.core.topology.SystemUser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkspaceMetadataReconcilerTest {
    @Test
    fun identityChangeMarksOldMetadataStaleAndCreatesNewRecord() {
        val existing = listOf(
            WorkspaceMetadata(AndroidUserId(10), "Gaming", alias = "Games", iconKey = WorkspaceIconKey.GAMING),
        )
        val users = listOf(SystemUser(AndroidUserId(10), "Testing", 0, isRunning = false))

        val result = WorkspaceMetadataReconciler.reconcile(existing, users)

        assertEquals(2, result.size)
        assertTrue(result.first { it.lastKnownSystemName == "Gaming" }.stale)
        assertEquals(null, result.first { it.lastKnownSystemName == "Testing" }.alias)
    }
}
