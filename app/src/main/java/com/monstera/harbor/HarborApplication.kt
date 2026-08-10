package com.monstera.harbor

import android.app.Application
import android.content.ComponentName
import com.monstera.harbor.admin.HarborDeviceAdminReceiver
import com.monstera.harbor.core.data.AndroidAppCatalogRepository
import com.monstera.harbor.core.data.HarborPreferences
import com.monstera.harbor.core.policy.AndroidWorkProfileController
import com.monstera.harbor.core.topology.ProfileTopologyDetector
import com.monstera.harbor.privileged.shizuku.ShizukuPrivilegedBackend

class HarborApplication : Application() {
    val graph: HarborGraph by lazy { HarborGraph(this) }
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
    val preferences = HarborPreferences(application)
    val privilegedBackend = ShizukuPrivilegedBackend(application)
}
