package com.monstera.harbor

import android.app.admin.DevicePolicyManager
import android.content.Intent
import android.content.pm.LauncherApps
import android.os.Bundle
import android.os.Process
import android.os.UserManager
import android.provider.Settings
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
                    onOpenWorkSettings = ::openWorkSettings,
                    onLaunchPackage = ::launchPackage,
                    onOpenPackageDetails = ::openPackageDetails,
                    onUninstallPackage = ::uninstallPackage,
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

    private fun openWorkSettings() {
        val intent = Intent(Settings.ACTION_SYNC_SETTINGS)
        startActivity(if (intent.resolveActivity(packageManager) != null) intent else Intent(Settings.ACTION_SETTINGS))
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
}
