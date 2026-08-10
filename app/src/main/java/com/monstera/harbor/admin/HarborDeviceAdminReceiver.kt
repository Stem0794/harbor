package com.monstera.harbor.admin

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.UserManager
import com.monstera.harbor.R
import com.monstera.harbor.core.policy.CrossProfileSharingPolicy

class HarborDeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        val policyManager = context.getSystemService(DevicePolicyManager::class.java)
        val admin = ComponentName(context, HarborDeviceAdminReceiver::class.java)
        if (!policyManager.isProfileOwnerApp(context.packageName)) return

        // Let Android ask the actual source app for per-source consent when an APK is opened.
        // Harbor never grants that consent itself and does not enable installs globally.
        runCatching {
            policyManager.clearUserRestriction(admin, UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES)
        }
        // Allow the user to send files from personal apps into the work
        // profile. The reverse direction remains disabled by default.
        CrossProfileSharingPolicy.apply(policyManager, admin)
        policyManager.setProfileName(admin, context.getString(R.string.work_profile_name))
        policyManager.setShortSupportMessage(admin, context.getString(R.string.support_message))
        policyManager.setLongSupportMessage(admin, context.getString(R.string.support_message))
        policyManager.setProfileEnabled(admin)
    }
}
