package com.monstera.harbor

import com.monstera.harbor.core.policy.PolicyResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShortcutLaunchCoordinatorTest {
    @Test
    fun visibleAppResolvesLaunchTarget() = runBlocking {
        val calls = mutableListOf<String>()

        val result = resolveShortcutLaunch(
            hiddenResult = PolicyResult.Success(false),
            unfreeze = { calls += "unfreeze"; PolicyResult.Success(Unit) },
            resolveLaunchIntent = { calls += "resolve"; "launch" },
        )

        assertEquals(ShortcutLaunchResult.Launch("launch"), result)
        assertEquals(listOf("resolve"), calls)
    }

    @Test
    fun hiddenAppUnfreezesBeforeResolvingLaunchTarget() = runBlocking {
        val calls = mutableListOf<String>()

        val result = resolveShortcutLaunch(
            hiddenResult = PolicyResult.Success(true),
            unfreeze = { calls += "unfreeze"; PolicyResult.Success(Unit) },
            resolveLaunchIntent = { calls += "resolve"; "launch" },
        )

        assertEquals(ShortcutLaunchResult.Launch("launch"), result)
        assertEquals(listOf("unfreeze", "resolve"), calls)
    }

    @Test
    fun hiddenAppDoesNotResolveWhenUnfreezeFails() = runBlocking {
        var resolved = false

        val result = resolveShortcutLaunch(
            hiddenResult = PolicyResult.Success(true),
            unfreeze = { PolicyResult.Failure("unfreeze denied") },
            resolveLaunchIntent = { resolved = true; "launch" },
        )

        assertEquals(ShortcutLaunchResult.Failure("unfreeze denied", removeShortcut = false), result)
        assertTrue(!resolved)
    }

    @Test
    fun hiddenAppWithMissingLaunchTargetRemovesStaleShortcut() = runBlocking {
        val result = resolveShortcutLaunch(
            hiddenResult = PolicyResult.Success(true),
            unfreeze = { PolicyResult.Success(Unit) },
            resolveLaunchIntent = { null },
        )

        assertEquals(ShortcutLaunchResult.Failure("The target app is no longer installed", removeShortcut = true), result)
    }

    @Test
    fun hiddenStateFailureDoesNotAttemptUnfreezeOrResolution() = runBlocking {
        var resolved = false
        var unfrozen = false

        val result = resolveShortcutLaunch(
            hiddenResult = PolicyResult.Failure("profile owner unavailable"),
            unfreeze = { unfrozen = true; PolicyResult.Success(Unit) },
            resolveLaunchIntent = { resolved = true; "launch" },
        )

        assertEquals(ShortcutLaunchResult.Failure("profile owner unavailable", removeShortcut = false), result)
        assertTrue(!unfrozen)
        assertTrue(!resolved)
    }
}
