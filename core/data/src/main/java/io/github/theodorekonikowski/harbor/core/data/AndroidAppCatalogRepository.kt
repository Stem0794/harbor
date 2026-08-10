package io.github.theodorekonikowski.harbor.core.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import io.github.theodorekonikowski.harbor.core.policy.PolicyResult
import io.github.theodorekonikowski.harbor.core.policy.WorkProfileController
import io.github.theodorekonikowski.harbor.core.topology.PackageName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AndroidAppCatalogRepository(
    private val context: Context,
    private val controller: WorkProfileController,
) : AppCatalogRepository {
    private val packageManager = context.packageManager

    override fun observeApps(): Flow<List<ManagedApp>> = callbackFlow {
        fun reload() {
            launch { trySend(refresh()) }
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) = reload()
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
        reload()
        awaitClose { context.unregisterReceiver(receiver) }
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
        installedApplications.filter { info ->
            info.flags and ApplicationInfo.FLAG_INSTALLED != 0
        }.mapNotNull { info ->
            val packageName = runCatching { PackageName(info.packageName) }.getOrNull()
                ?: return@mapNotNull null
            val hidden = when (val result = controller.isApplicationHidden(packageName)) {
                is PolicyResult.Success -> result.value
                is PolicyResult.Failure -> false
            }
            ManagedApp(
                packageName = packageName,
                label = info.loadLabel(packageManager).toString().ifBlank { info.packageName },
                isSystem = info.flags and ApplicationInfo.FLAG_SYSTEM != 0,
                isEnabled = info.enabled,
                isHidden = hidden,
                isLaunchable = packageManager.getLaunchIntentForPackage(info.packageName) != null,
            )
        }.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
    }
}
