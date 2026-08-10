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

class HarborPreferences(private val context: Context) {
    val advancedToolsEnabled: Flow<Boolean> = context.harborDataStore.data.map { preferences ->
        preferences[ADVANCED_TOOLS_ENABLED] ?: false
    }

    suspend fun setAdvancedToolsEnabled(enabled: Boolean) {
        context.harborDataStore.edit { it[ADVANCED_TOOLS_ENABLED] = enabled }
    }

    suspend fun saveShortcut(shortcutId: String, packageName: PackageName) {
        ShortcutIdValidator.normalize(shortcutId)?.let { normalized ->
            context.harborDataStore.edit { it[shortcutKey(normalized)] = packageName.value }
        }
    }

    suspend fun removeShortcut(shortcutId: String) {
        ShortcutIdValidator.normalize(shortcutId)?.let { normalized ->
            context.harborDataStore.edit { it.remove(shortcutKey(normalized)) }
        }
    }

    suspend fun shortcutPackage(shortcutId: String): PackageName? {
        val normalized = ShortcutIdValidator.normalize(shortcutId) ?: return null
        return context.harborDataStore.data.map { preferences ->
            preferences[shortcutKey(normalized)]?.let { value -> runCatching { PackageName(value) }.getOrNull() }
        }.first()
    }

    private companion object {
        val ADVANCED_TOOLS_ENABLED = booleanPreferencesKey("advanced_tools_enabled")

        fun shortcutKey(id: String) = androidx.datastore.preferences.core.stringPreferencesKey("shortcut_$id")
    }
}
