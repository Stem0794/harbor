package com.monstera.harbor.admin

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.monstera.harbor.R
import com.monstera.harbor.FileImportReceiverAvailability
import com.monstera.harbor.core.policy.CrossProfileSharingPolicy

class HarborDeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        val policyManager = context.getSystemService(DevicePolicyManager::class.java)
        val admin = ComponentName(context, HarborDeviceAdminReceiver::class.java)
        if (!policyManager.isProfileOwnerApp(context.packageName)) return
        FileImportReceiverAvailability.update(context)

        // APK installation remains restricted until the user explicitly enables it from
        // the work-profile UI. Provisioning must not widen the install policy implicitly.
        // Allow the user to send files from personal apps into the work
        // profile. The reverse direction remains disabled by default.
        CrossProfileSharingPolicy.apply(policyManager, admin)
        policyManager.setProfileName(admin, context.getString(R.string.work_profile_name))
        policyManager.setShortSupportMessage(admin, context.getString(R.string.support_message))
        policyManager.setLongSupportMessage(admin, context.getString(R.string.support_message))
        policyManager.setProfileEnabled(admin)
    }
}
