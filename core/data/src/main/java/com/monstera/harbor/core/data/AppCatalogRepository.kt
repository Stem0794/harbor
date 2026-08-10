package com.monstera.harbor.core.data

import com.monstera.harbor.core.topology.PackageName
import kotlinx.coroutines.flow.Flow

data class ManagedApp(
    val packageName: PackageName,
    val label: String,
    val isSystem: Boolean,
    val isEnabled: Boolean,
    val isHidden: Boolean,
    val isLaunchable: Boolean,
)

interface AppCatalogRepository {
    fun observeApps(): Flow<List<ManagedApp>>
    suspend fun refresh(): List<ManagedApp>
}
