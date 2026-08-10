package com.monstera.harbor.core.policy

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import com.monstera.harbor.core.topology.PackageName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

class AndroidWorkProfileController(
    private val context: Context,
    private val admin: ComponentName,
    private val protectedPackages: Set<String>,
) : WorkProfileController {
    private val policyManager = context.getSystemService(DevicePolicyManager::class.java)

    override fun observeState(): Flow<WorkProfileState> = callbackFlow {
        fun emitState() {
            trySend(snapshot())
        }
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) = emitState()
        }
        val filter = IntentFilter().apply {
            addAction(DevicePolicyManager.ACTION_PROFILE_OWNER_CHANGED)
            addAction(Intent.ACTION_MANAGED_PROFILE_AVAILABLE)
            addAction(Intent.ACTION_MANAGED_PROFILE_UNAVAILABLE)
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        emitState()
        awaitClose { context.unregisterReceiver(receiver) }
    }

    override suspend fun setApplicationHidden(
        packageName: PackageName,
        hidden: Boolean,
    ): PolicyResult<Boolean> = withContext(Dispatchers.IO) {
        if (!isOwner()) return@withContext PolicyResult.Failure("Harbor is not the profile owner")
        if (packageName.value in protectedPackages) {
            return@withContext PolicyResult.Failure("This package is required for profile recovery", false)
        }
        runCatching { policyManager.setApplicationHidden(admin, packageName.value, hidden) }
            .fold(
                onSuccess = { PolicyResult.Success(it) },
                onFailure = { PolicyResult.Failure(it.message ?: it.javaClass.simpleName) },
            )
    }

    override suspend fun isApplicationHidden(packageName: PackageName): PolicyResult<Boolean> =
        withContext(Dispatchers.IO) {
            if (!isOwner()) return@withContext PolicyResult.Failure("Harbor is not the profile owner")
            runCatching { policyManager.isApplicationHidden(admin, packageName.value) }
                .fold(
                    onSuccess = { PolicyResult.Success(it) },
                    onFailure = { PolicyResult.Failure(it.message ?: it.javaClass.simpleName) },
                )
        }

    private fun snapshot() = if (isOwner()) {
        WorkProfileState(WorkProfileStatus.ACTIVE)
    } else {
        WorkProfileState(WorkProfileStatus.NOT_PROFILE_OWNER)
    }

    private fun isOwner() = policyManager.isProfileOwnerApp(context.packageName)
}
