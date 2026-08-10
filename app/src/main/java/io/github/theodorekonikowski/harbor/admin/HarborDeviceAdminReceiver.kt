package io.github.theodorekonikowski.harbor.admin

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import io.github.theodorekonikowski.harbor.R

class HarborDeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        val policyManager = context.getSystemService(DevicePolicyManager::class.java)
        val admin = ComponentName(context, HarborDeviceAdminReceiver::class.java)
        if (!policyManager.isProfileOwnerApp(context.packageName)) return

        policyManager.setProfileName(admin, context.getString(R.string.work_profile_name))
        policyManager.setShortSupportMessage(admin, context.getString(R.string.support_message))
        policyManager.setLongSupportMessage(admin, context.getString(R.string.support_message))
        policyManager.setProfileEnabled(admin)
    }
}
