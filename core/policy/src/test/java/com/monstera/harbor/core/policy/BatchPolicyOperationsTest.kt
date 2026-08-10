package com.monstera.harbor.core.policy

import com.monstera.harbor.core.topology.PackageName
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BatchPolicyOperationsTest {
    private val packages = listOf(
        PackageName("com.example.one"),
        PackageName("com.example.two"),
        PackageName("com.example.three"),
    )

    @Test
    fun preservesPartialFailuresAndOrder() = runBlocking {
        val controller = object : WorkProfileController {
            override fun observeState() = kotlinx.coroutines.flow.emptyFlow<WorkProfileState>()
            override suspend fun setApplicationHidden(packageName: PackageName, hidden: Boolean): PolicyResult<Unit> =
                if (packageName == packages[1]) PolicyResult.Failure("package disappeared") else PolicyResult.Success(Unit)
            override suspend fun isApplicationHidden(packageName: PackageName) = PolicyResult.Success(false)
            override suspend fun allowApkInstalls() = PolicyResult.Success(Unit)
        }

        val results = controller.setApplicationHiddenSequentially(packages, hidden = true)

        assertEquals(packages, results.map { it.packageName })
        assertTrue(results[0].success)
        assertFalse(results[1].success)
        assertEquals("package disappeared", results[1].reason)
        assertTrue(results[2].success)
    }
}
