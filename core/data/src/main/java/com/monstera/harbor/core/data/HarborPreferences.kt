package com.monstera.harbor.core.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import com.monstera.harbor.core.topology.PackageName

private val Context.harborDataStore by preferencesDataStore("harbor_preferences")

data class ShortcutTarget(
    val packageName: PackageName,
    val signerDigests: Set<String>,
)

class HarborPreferences(private val context: Context) {
    val advancedToolsEnabled: Flow<Boolean> = context.harborDataStore.data.map { preferences ->
        preferences[ADVANCED_TOOLS_ENABLED] ?: false
    }

    suspend fun setAdvancedToolsEnabled(enabled: Boolean) {
        context.harborDataStore.edit { it[ADVANCED_TOOLS_ENABLED] = enabled }
    }

    suspend fun saveShortcut(
        shortcutId: String,
        packageName: PackageName,
        signerDigests: Set<String>,
    ) {
        require(signerDigests.isNotEmpty()) { "A shortcut must have a signer identity" }
        ShortcutIdValidator.normalize(shortcutId)?.let { normalized ->
            context.harborDataStore.edit {
                it[shortcutKey(normalized)] = packageName.value
                it[shortcutSignerKey(normalized)] = signerDigests.sorted().joinToString(",")
            }
        }
    }

    suspend fun removeShortcut(shortcutId: String) {
        ShortcutIdValidator.normalize(shortcutId)?.let { normalized ->
            context.harborDataStore.edit {
                it.remove(shortcutKey(normalized))
                it.remove(shortcutSignerKey(normalized))
            }
        }
    }

    suspend fun shortcutTarget(shortcutId: String): ShortcutTarget? {
        val normalized = ShortcutIdValidator.normalize(shortcutId) ?: return null
        return context.harborDataStore.data.map { preferences ->
            val packageName = preferences[shortcutKey(normalized)]
                ?.let { value -> runCatching { PackageName(value) }.getOrNull() }
            val signerDigests = preferences[shortcutSignerKey(normalized)]
                ?.split(',')
                ?.map(String::trim)
                ?.filter { it.matches(SIGNER_DIGEST) }
                ?.toSet()
                .orEmpty()
            if (packageName == null || signerDigests.isEmpty()) null
            else ShortcutTarget(packageName, signerDigests)
        }.first()
    }

    private companion object {
        val ADVANCED_TOOLS_ENABLED = booleanPreferencesKey("advanced_tools_enabled")
        val SIGNER_DIGEST = Regex("[0-9a-fA-F]{64}")

        fun shortcutKey(id: String) = androidx.datastore.preferences.core.stringPreferencesKey("shortcut_$id")

        fun shortcutSignerKey(id: String) =
            androidx.datastore.preferences.core.stringPreferencesKey("shortcut_signers_$id")
    }
}
