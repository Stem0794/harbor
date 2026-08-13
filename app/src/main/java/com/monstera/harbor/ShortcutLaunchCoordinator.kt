package com.monstera.harbor

import com.monstera.harbor.core.policy.PolicyResult

internal sealed interface ShortcutLaunchResult<out T> {
    data class Launch<T>(val target: T) : ShortcutLaunchResult<T>
    data class Failure(val message: String, val removeShortcut: Boolean) : ShortcutLaunchResult<Nothing>
}

/** Resolves policy state before asking PackageManager for a launch target. */
internal suspend fun <T> resolveShortcutLaunch(
    hiddenResult: PolicyResult<Boolean>,
    unfreeze: suspend () -> PolicyResult<Unit>,
    resolveLaunchIntent: () -> T?,
): ShortcutLaunchResult<T> {
    when (hiddenResult) {
        is PolicyResult.Failure -> return ShortcutLaunchResult.Failure(hiddenResult.reason, removeShortcut = false)
        is PolicyResult.Success -> if (hiddenResult.value) {
            when (val result = unfreeze()) {
                is PolicyResult.Failure -> return ShortcutLaunchResult.Failure(result.reason, removeShortcut = false)
                is PolicyResult.Success -> Unit
            }
        }
    }

    return resolveLaunchIntent()?.let { ShortcutLaunchResult.Launch(it) }
        ?: ShortcutLaunchResult.Failure("The target app is no longer installed", removeShortcut = true)
}
