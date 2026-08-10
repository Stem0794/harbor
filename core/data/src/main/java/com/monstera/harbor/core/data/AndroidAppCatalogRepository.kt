package com.monstera.harbor.core.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.monstera.harbor.core.policy.PolicyResult
import com.monstera.harbor.core.policy.WorkProfileController
import com.monstera.harbor.core.topology.PackageName
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AndroidAppCatalogRepository(
    private val context: Context,
    private val controller: WorkProfileController,
) : AppCatalogRepository {
    private val packageManager = context.packageManager

    @OptIn(FlowPreview::class)
    override fun observeApps(): Flow<List<ManagedApp>> = callbackFlow {
        val reloads = MutableSharedFlow<Unit>(
            replay = 1,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
        val reloadJob = launch {
            reloads
                .debounce(PACKAGE_REFRESH_DEBOUNCE_MILLIS)
                .collectLatest { trySend(refresh()) }
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                reloads.tryEmit(Unit)
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        reloads.tryEmit(Unit)
        awaitClose {
            reloadJob.cancel()
            context.unregisterReceiver(receiver)
        }
    }

    override suspend fun refresh(): List<ManagedApp> = withContext(Dispatchers.IO) {
        val installedApplications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledApplications(
                PackageManager.ApplicationInfoFlags.of(
                    (PackageManager.MATCH_DISABLED_COMPONENTS or
                        PackageManager.MATCH_UNINSTALLED_PACKAGES).toLong(),
                ),
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getInstalledApplications(
                PackageManager.MATCH_DISABLED_COMPONENTS or PackageManager.MATCH_UNINSTALLED_PACKAGES,
            )
        }
        val launchablePackages = packageManager.queryIntentActivities(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
            PackageManager.MATCH_ALL,
        ).mapTo(hashSetOf()) { it.activityInfo.packageName }
        installedApplications.filter { info ->
            // System components without a launcher entry are implementation details,
            // not applications the user can actually open from the work profile.
            info.flags and ApplicationInfo.FLAG_INSTALLED != 0 &&
                (info.flags and ApplicationInfo.FLAG_SYSTEM == 0 || info.packageName in launchablePackages)
        }.mapNotNull { info ->
            val packageName = runCatching { PackageName(info.packageName) }.getOrNull()
                ?: return@mapNotNull null
            val isSystem = info.flags and ApplicationInfo.FLAG_SYSTEM != 0
            val hidden = if (isSystem) {
                false
            } else {
                when (val result = controller.isApplicationHidden(packageName)) {
                    is PolicyResult.Success -> result.value
                    is PolicyResult.Failure -> false
                }
            }
            ManagedApp(
                packageName = packageName,
                label = info.loadLabel(packageManager).toString().ifBlank { info.packageName },
                isSystem = isSystem,
                isEnabled = info.enabled,
                isHidden = hidden,
                isLaunchable = info.packageName in launchablePackages,
            )
        }.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
    }

    private companion object {
        const val PACKAGE_REFRESH_DEBOUNCE_MILLIS = 250L
    }
}
