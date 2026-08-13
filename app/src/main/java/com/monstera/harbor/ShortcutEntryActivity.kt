package com.monstera.harbor

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.monstera.harbor.core.data.ShortcutIdValidator
import kotlinx.coroutines.launch

/**
 * Work-profile-local entry point for Harbor-created pinned shortcuts.
 * The only accepted input is an opaque UUID previously persisted by Harbor.
 */
class ShortcutEntryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val shortcutId = ShortcutIdValidator.normalize(
            intent.getStringExtra(Intent.EXTRA_SHORTCUT_ID),
        )
        if (shortcutId == null) {
            finishWithMessage("This Harbor shortcut is invalid")
            return
        }
        val graph = (application as HarborApplication).graph
        lifecycleScope.launch {
            val target = graph.preferences.shortcutTarget(shortcutId)
            if (target == null) {
                finishWithMessage("This Harbor shortcut is no longer available")
                return@launch
            }
            val packageName = target.packageName
            val currentSigners = PackageSigner.fingerprints(packageManager, packageName.value)
            if (currentSigners.isEmpty() || currentSigners.intersect(target.signerDigests).isEmpty()) {
                graph.preferences.removeShortcut(shortcutId)
                finishWithMessage("The target app identity changed; create a new shortcut")
                return@launch
            }
            when (val result = resolveShortcutLaunch(
                hiddenResult = graph.policyController.isApplicationHidden(packageName),
                unfreeze = { graph.policyController.setApplicationHidden(packageName, false) },
                resolveLaunchIntent = { packageManager.getLaunchIntentForPackage(packageName.value) },
            )) {
                is ShortcutLaunchResult.Launch -> launchTarget(result.target)
                is ShortcutLaunchResult.Failure -> {
                    if (result.removeShortcut) graph.preferences.removeShortcut(shortcutId)
                    finishWithMessage(result.message)
                }
            }
        }
    }

    private fun launchTarget(intent: Intent) {
        runCatching { startActivity(intent) }
            .onFailure { finishWithMessage(it.message ?: "Unable to launch the app") }
            .onSuccess { finish() }
    }

    private fun finishWithMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }
}
