package com.monstera.harbor.core.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.monstera.harbor.core.topology.PackageName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class PackagePresentation(
    val packageName: PackageName,
    val label: String,
    val isSystem: Boolean,
)

interface PackageMetadataProvider {
    suspend fun resolve(packageName: PackageName): PackagePresentation?
}

class AndroidPackageMetadataProvider(context: Context) : PackageMetadataProvider {
    private val packageManager = context.packageManager

    override suspend fun resolve(packageName: PackageName): PackagePresentation? = withContext(Dispatchers.IO) {
        runCatching {
            val info = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                packageManager.getApplicationInfo(
                    packageName.value,
                    PackageManager.ApplicationInfoFlags.of(0),
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getApplicationInfo(packageName.value, 0)
            }
            PackagePresentation(
                packageName = packageName,
                label = info.loadLabel(packageManager).toString().ifBlank { packageName.value },
                isSystem = info.flags and ApplicationInfo.FLAG_SYSTEM != 0,
            )
        }.getOrNull()
    }
}
