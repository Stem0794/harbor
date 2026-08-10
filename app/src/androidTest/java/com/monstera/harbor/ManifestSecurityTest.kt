package com.monstera.harbor

import android.content.pm.PackageManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ManifestSecurityTest {
    @Test fun prohibitedPermissionsAreAbsent() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        @Suppress("DEPRECATION")
        val info = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
        val requested = info.requestedPermissions.orEmpty().toSet()
        assertFalse("Network access must remain absent", "android.permission.INTERNET" in requested)
        assertFalse("Secure settings must remain absent", "android.permission.WRITE_SECURE_SETTINGS" in requested)
        assertFalse("Harbor must not request MANAGE_USERS", "android.permission.MANAGE_USERS" in requested)
        assertFalse(
            "Harbor must not request direct cross-user access",
            "android.permission.INTERACT_ACROSS_USERS" in requested,
        )
        assertFalse(
            "Harbor must not request full direct cross-user access",
            "android.permission.INTERACT_ACROSS_USERS_FULL" in requested,
        )
    }
}
