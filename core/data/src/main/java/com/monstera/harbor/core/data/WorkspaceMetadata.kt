package com.monstera.harbor.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.monstera.harbor.core.topology.AndroidUserId
import com.monstera.harbor.core.topology.SystemUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class WorkspaceIconKey {
    PERSONAL,
    WORK,
    GAMING,
    SOCIAL,
    PRIVACY,
    TESTING,
    FINANCE,
    TRAVEL,
    GENERIC,
}

data class WorkspaceMetadata(
    val androidUserId: AndroidUserId,
    val lastKnownSystemName: String,
    val alias: String? = null,
    val iconKey: WorkspaceIconKey = WorkspaceIconKey.GENERIC,
    val stale: Boolean = false,
)

object WorkspaceMetadataReconciler {
    fun reconcile(existing: List<WorkspaceMetadata>, users: List<SystemUser>): List<WorkspaceMetadata> {
        val freshByIdentity = users.associateBy { it.id to it.name }
        val result = existing.map { metadata ->
            metadata.copy(stale = (metadata.androidUserId to metadata.lastKnownSystemName) !in freshByIdentity)
        }.toMutableList()
        users.forEach { user ->
            if (result.none { it.androidUserId == user.id && it.lastKnownSystemName == user.name }) {
                result += WorkspaceMetadata(user.id, user.name)
            }
        }
        return result.sortedWith(compareBy({ it.stale }, { it.androidUserId.value }, { it.lastKnownSystemName }))
    }
}

private val Context.workspaceDataStore by preferencesDataStore("harbor_workspace_metadata")

class WorkspaceMetadataStore(private val context: Context) {
    val metadata: Flow<List<WorkspaceMetadata>> = context.workspaceDataStore.data.map { preferences ->
        decode(preferences[METADATA_KEY].orEmpty())
    }

    suspend fun reconcile(users: List<SystemUser>) {
        context.workspaceDataStore.edit { preferences ->
            val updated = WorkspaceMetadataReconciler.reconcile(
                decode(preferences[METADATA_KEY].orEmpty()),
                users,
            )
            preferences[METADATA_KEY] = encode(updated)
        }
    }

    suspend fun setAlias(user: SystemUser, alias: String?) = update(user) { metadata ->
        metadata.copy(
            alias = alias?.trim()?.filter { it != '\t' && it != '\n' && it != '\r' }
                ?.take(64)
                ?.takeIf(String::isNotEmpty),
        )
    }

    suspend fun setIcon(user: SystemUser, icon: WorkspaceIconKey) = update(user) { metadata ->
        metadata.copy(iconKey = icon)
    }

    private suspend fun update(user: SystemUser, transform: (WorkspaceMetadata) -> WorkspaceMetadata) {
        context.workspaceDataStore.edit { preferences ->
            val current = decode(preferences[METADATA_KEY].orEmpty())
            val index = current.indexOfFirst {
                it.androidUserId == user.id && it.lastKnownSystemName == user.name
            }
            val updated = if (index >= 0) {
                current.toMutableList().also { values ->
                    values[index] = transform(values[index].copy(stale = false))
                }
            } else {
                current + transform(WorkspaceMetadata(user.id, user.name))
            }
            preferences[METADATA_KEY] = encode(updated)
        }
    }

    private companion object {
        val METADATA_KEY = stringPreferencesKey("workspace_metadata_v1")

        fun encode(values: List<WorkspaceMetadata>): String = values.joinToString("\n") { metadata ->
            listOf(
                metadata.androidUserId.value.toString(),
                metadata.lastKnownSystemName,
                metadata.alias.orEmpty(),
                metadata.iconKey.name,
                metadata.stale.toString(),
            ).joinToString("\t")
        }

        fun decode(value: String): List<WorkspaceMetadata> = value.lineSequence().mapNotNull { line ->
            val fields = line.split('\t')
            if (fields.size != 5) return@mapNotNull null
            val id = fields[0].toIntOrNull() ?: return@mapNotNull null
            val name = fields[1].takeIf(String::isNotBlank) ?: return@mapNotNull null
            val icon = runCatching { WorkspaceIconKey.valueOf(fields[3]) }
                .getOrDefault(WorkspaceIconKey.GENERIC)
            WorkspaceMetadata(
                androidUserId = AndroidUserId(id),
                lastKnownSystemName = name,
                alias = fields[2].takeIf(String::isNotBlank),
                iconKey = icon,
                stale = fields[4].toBooleanStrictOrNull() ?: false,
            )
        }.toList()
    }
}
