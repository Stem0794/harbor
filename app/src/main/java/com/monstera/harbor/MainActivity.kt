package com.monstera.harbor

import android.app.admin.DevicePolicyManager
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.graphics.Bitmap
import android.graphics.Canvas
import android.content.pm.LauncherApps
import android.net.Uri
import android.os.Bundle
import android.os.Process
import android.os.UserManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.monstera.harbor.ui.HarborRoot
import com.monstera.harbor.ui.theme.HarborTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val provisioningLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { recreate() }
    private val personalFilePicker = registerForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments(),
    ) { uris -> importPersonalFiles(uris) }

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
                    onPickPersonalFiles = ::pickPersonalFiles,
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
        val launcherApps = getSystemService(LauncherApps::class.java)
        val userManager = getSystemService(UserManager::class.java)
        val target = userManager.userProfiles.firstNotNullOfOrNull { user ->
            if (user == Process.myUserHandle()) return@firstNotNullOfOrNull null
            launcherApps.getActivityList(packageName, user).firstOrNull()?.let { user to it.componentName }
        } ?: return false
        return runCatching {
            launcherApps.startMainActivity(target.second, target.first, null, null)
            true
        }.getOrDefault(false)
    }

    private fun openSystemSettings() {
        startActivity(Intent(Settings.ACTION_SETTINGS))
    }

    private fun pickPersonalFiles() {
        personalFilePicker.launch(arrayOf("*/*"))
    }

    private fun importPersonalFiles(uris: List<Uri>) {
        if (uris.isEmpty()) return
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                SharedFileImporter(contentResolver).copyUris(uris, null)
            }
            Toast.makeText(
                this@MainActivity,
                if (result.names.isEmpty()) {
                    "No file could be copied. Use Share from the personal profile."
                } else {
                    "Copied ${result.names.size} file(s) to work Downloads/Harbor"
                },
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
        val shortcutId = java.util.UUID.randomUUID().toString()
        val graph = (application as HarborApplication).graph
        graph.preferences.saveShortcut(
            shortcutId,
            com.monstera.harbor.core.topology.PackageName(packageName),
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
}
