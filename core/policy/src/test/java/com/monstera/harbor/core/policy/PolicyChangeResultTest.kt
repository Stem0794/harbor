package com.monstera.harbor.core.policy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PolicyChangeResultTest {
    @Test
    fun truePlatformResultIsSuccessful() {
        val result = applyBooleanPolicyChange { true }

        assertEquals(PolicyResult.Success(Unit), result)
    }

    @Test
    fun falsePlatformResultIsFailure() {
        val result = applyBooleanPolicyChange { false }

        assertTrue(result is PolicyResult.Failure)
        assertEquals(
            "Android did not apply the requested policy change",
            (result as PolicyResult.Failure).reason,
        )
    }

    @Test
    fun platformExceptionIsFailure() {
        val result = applyBooleanPolicyChange { throw SecurityException("denied") }

        assertTrue(result is PolicyResult.Failure)
        assertEquals("denied", (result as PolicyResult.Failure).reason)
    }
}
