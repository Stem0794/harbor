package com.monstera.harbor.core.policy

import android.app.admin.DevicePolicyManager
import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Test

class CrossProfileSharingPolicyTest {
    @Test
    fun rulesAllowOnlyPersonalToWorkFileIntents() {
        assertEquals(
            listOf(
                Intent.ACTION_SEND,
                Intent.ACTION_SEND_MULTIPLE,
                Intent.ACTION_SEND,
                Intent.ACTION_SEND_MULTIPLE,
            ),
            CrossProfileSharingPolicy.parentToManagedFileRules.map { it.action },
        )
        assertEquals(
            listOf(
                DevicePolicyManager.FLAG_MANAGED_CAN_ACCESS_PARENT,
                DevicePolicyManager.FLAG_MANAGED_CAN_ACCESS_PARENT,
                DevicePolicyManager.FLAG_MANAGED_CAN_ACCESS_PARENT,
                DevicePolicyManager.FLAG_MANAGED_CAN_ACCESS_PARENT,
            ),
            CrossProfileSharingPolicy.parentToManagedFileRules.map { it.direction },
        )
        assertEquals(
            listOf("*/*", "*/*", null, null),
            CrossProfileSharingPolicy.parentToManagedFileRules.map { it.mimeType },
        )
    }
}
