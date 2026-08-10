package io.github.theodorekonikowski.harbor

import android.app.Application
import android.content.ComponentName
import io.github.theodorekonikowski.harbor.admin.HarborDeviceAdminReceiver
import io.github.theodorekonikowski.harbor.core.data.AndroidAppCatalogRepository
import io.github.theodorekonikowski.harbor.core.data.HarborPreferences
import io.github.theodorekonikowski.harbor.core.policy.AndroidWorkProfileController
import io.github.theodorekonikowski.harbor.core.topology.ProfileTopologyDetector
import io.github.theodorekonikowski.harbor.privileged.shizuku.ShizukuPrivilegedBackend

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
