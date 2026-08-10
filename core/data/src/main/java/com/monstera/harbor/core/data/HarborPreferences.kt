package com.monstera.harbor.core.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.harborDataStore by preferencesDataStore("harbor_preferences")

class HarborPreferences(private val context: Context) {
    val advancedToolsEnabled: Flow<Boolean> = context.harborDataStore.data.map { preferences ->
        preferences[ADVANCED_TOOLS_ENABLED] ?: false
    }

    suspend fun setAdvancedToolsEnabled(enabled: Boolean) {
        context.harborDataStore.edit { it[ADVANCED_TOOLS_ENABLED] = enabled }
    }

    private companion object {
        val ADVANCED_TOOLS_ENABLED = booleanPreferencesKey("advanced_tools_enabled")
    }
}
