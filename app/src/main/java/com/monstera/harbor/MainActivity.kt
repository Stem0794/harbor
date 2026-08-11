package com.monstera.harbor

import android.app.admin.DevicePolicyManager
import android.content.ClipData
import android.content.ComponentName
import android.content.Intent
import android.content.pm.CrossProfileApps
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import com.monstera.harbor.ui.HarborRoot
import com.monstera.harbor.ui.theme.HarborTheme

class MainActivity : ComponentActivity() {
    private val provisioningLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { recreate() }
    private val filesToWorkPicker = registerForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments(),
    ) { uris -> sendFilesToWork(uris) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val graph = (application as HarborApplication).graph
        setContent {
            HarborTheme {
                HarborRoot(
                    graph = graph,
                    onProvision = { beginProvisioning(graph) },
                    onOpenWorkHarbor = ::openWorkHarbor,
                    onOpenSystemSettings = ::openSystemSettings,
                    onLaunchPackage = ::launchPackage,
                    onOpenPackageDetails = ::openPackageDetails,
                    onUninstallPackage = ::uninstallPackage,
                    onAddShortcut = ::addShortcut,
                    onOpenPersonalHarbor = ::openPersonalHarbor,
                    onSendFilesToWork = ::pickFilesToSend,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (application as HarborApplication).graph.privilegedBackend.refresh()
    }

    private fun beginProvisioning(graph: HarborGraph) {
        val intent = Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE)
            .putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME, graph.admin)
        provisioningLauncher.launch(intent)
    }

    private fun openWorkHarbor(): Boolean {
        // CrossProfileApps exposes the profiles that this app can reach, rather than
        // every full Android user. This avoids accidentally opening Harbor in a
        // secondary user when the work-profile button is pressed.
        val crossProfileApps = getSystemService(CrossProfileApps::class.java)
        val target = runCatching { crossProfileApps.targetUserProfiles.singleOrNull() }
            .getOrNull()
            ?: return false
        return runCatching {
            crossProfileApps.startMainActivity(ComponentName(this, MainActivity::class.java), target)
            true
        }.getOrDefault(false)
    }

    private fun openSystemSettings() {
        startActivity(Intent(Settings.ACTION_SETTINGS))
    }

    private fun openPersonalHarbor(): Boolean {
        val crossProfileApps = getSystemService(CrossProfileApps::class.java)
        val target = crossProfileApps.targetUserProfiles.singleOrNull() ?: return false
        return runCatching {
            crossProfileApps.startMainActivity(
                ComponentName(this, MainActivity::class.java),
                target,
            )
            true
        }.getOrDefault(false)
    }

    private fun pickFilesToSend() {
        filesToWorkPicker.launch(arrayOf("*/*"))
    }

    private fun sendFilesToWork(uris: List<Uri>) {
        if (uris.isEmpty()) return
        val selected = uris.take(MAX_SHARED_FILES)
        val shareIntent = Intent(
            if (selected.size == 1) Intent.ACTION_SEND else Intent.ACTION_SEND_MULTIPLE,
        ).apply {
            setPackage(packageName)
            type = selected.mapNotNull(contentResolver::getType).distinct().singleOrNull() ?: "*/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = ClipData.newUri(contentResolver, "Harbor file", selected.first()).apply {
                selected.drop(1).forEach { addItem(ClipData.Item(it)) }
            }
            if (selected.size == 1) {
                putExtra(Intent.EXTRA_STREAM, selected.first())
            } else {
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(selected))
            }
        }
        runCatching {
            startActivity(Intent.createChooser(shareIntent, "Send to Harbor work profile"))
        }.onFailure {
            Toast.makeText(
                this@MainActivity,
                "Work Harbor is not available as a Share target. Open work Harbor, enable Share, then retry.",
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    private fun launchPackage(packageName: String): Boolean {
        val intent = packageManager.getLaunchIntentForPackage(packageName) ?: return false
        return runCatching { startActivity(intent); true }.getOrDefault(false)
    }

    private fun openPackageDetails(packageName: String) {
        startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData("package:$packageName".toUri()),
        )
    }

    private fun uninstallPackage(packageName: String) {
        startActivity(Intent(Intent.ACTION_DELETE, "package:$packageName".toUri()))
    }

    private suspend fun addShortcut(packageName: String): Boolean {
        val shortcutManager = getSystemService(ShortcutManager::class.java)
        if (!shortcutManager.isRequestPinShortcutSupported) return false
        val applicationInfo = runCatching { packageManager.getApplicationInfo(packageName, 0) }.getOrNull()
            ?: return false
        val signerDigests = PackageSigner.fingerprints(packageManager, packageName)
        if (signerDigests.isEmpty()) return false
        val shortcutId = java.util.UUID.randomUUID().toString()
        val graph = (application as HarborApplication).graph
        graph.preferences.saveShortcut(
            shortcutId,
            com.monstera.harbor.core.topology.PackageName(packageName),
            signerDigests,
        )
        val drawable = applicationInfo.loadIcon(packageManager)
        val iconSize = maxOf(drawable.intrinsicWidth, drawable.intrinsicHeight, 1)
        val bitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).also { canvas ->
            drawable.setBounds(0, 0, iconSize, iconSize)
            drawable.draw(canvas)
        }
        val shortcut = ShortcutInfo.Builder(this, shortcutId)
            .setShortLabel(applicationInfo.loadLabel(packageManager).toString().ifBlank { packageName })
            .setLongLabel("Launch ${applicationInfo.loadLabel(packageManager)}")
            .setIcon(Icon.createWithBitmap(bitmap))
            .setIntent(
                Intent(this, ShortcutEntryActivity::class.java)
                    .putExtra(Intent.EXTRA_SHORTCUT_ID, shortcutId),
            )
            .build()
        val requested = runCatching { shortcutManager.requestPinShortcut(shortcut, null) }.getOrDefault(false)
        if (!requested) {
            graph.preferences.removeShortcut(shortcutId)
        } else {
            Toast.makeText(this, "Choose where to add the Harbor shortcut", Toast.LENGTH_SHORT).show()
        }
        return requested
    }

    private companion object {
        const val MAX_SHARED_FILES = 50
    }
}
