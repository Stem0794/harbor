package com.monstera.harbor

import android.app.Application
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.monstera.harbor.admin.HarborDeviceAdminReceiver
import com.monstera.harbor.core.data.AndroidAppCatalogRepository
import com.monstera.harbor.core.data.AndroidAppIconProvider
import com.monstera.harbor.core.data.AndroidPackageMetadataProvider
import com.monstera.harbor.core.data.HarborPreferences
import com.monstera.harbor.core.data.WorkspaceMetadataStore
import com.monstera.harbor.core.policy.AndroidWorkProfileController
import com.monstera.harbor.core.topology.ProfileTopologyDetector
import com.monstera.harbor.privileged.shizuku.ShizukuPrivilegedBackend

class HarborApplication : Application() {
    val graph: HarborGraph by lazy { HarborGraph(this) }

    override fun onCreate() {
        super.onCreate()
        FileImportReceiverAvailability.update(this)
    }
}

/** Keeps Harbor's exported share receiver available only inside its owned work profile. */
internal object FileImportReceiverAvailability {
    fun update(context: Context) {
        val policyManager = context.getSystemService(DevicePolicyManager::class.java)
        val desiredState = if (policyManager.isProfileOwnerApp(context.packageName)) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }
        val component = ComponentName(context, ImportFilesActivity::class.java)
        if (context.packageManager.getComponentEnabledSetting(component) != desiredState) {
            context.packageManager.setComponentEnabledSetting(
                component,
                desiredState,
                PackageManager.DONT_KILL_APP,
            )
        }
    }
}

class HarborGraph(application: Application) {
    val admin = ComponentName(application, HarborDeviceAdminReceiver::class.java)
    val topologyDetector = ProfileTopologyDetector(application)
    val policyController = AndroidWorkProfileController(
        context = application,
        admin = admin,
        protectedPackages = setOf(
            application.packageName,
            "com.android.settings",
            "com.android.packageinstaller",
            "com.google.android.packageinstaller",
            "com.android.permissioncontroller",
        ),
    )
    val appCatalog = AndroidAppCatalogRepository(application, policyController)
    val iconProvider = AndroidAppIconProvider(application)
    val packageMetadataProvider = AndroidPackageMetadataProvider(application)
    val preferences = HarborPreferences(application)
    val workspaceMetadataStore = WorkspaceMetadataStore(application)
    val privilegedBackend = ShizukuPrivilegedBackend(application)
}
