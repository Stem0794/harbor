package com.monstera.harbor.core.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
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

data class ShortcutRequestNoticeState(
    val packageName: PackageName,
    val appLabel: String,
    val requestAccepted: Boolean,
)

class HarborPreferences(private val context: Context) {
    val advancedToolsEnabled: Flow<Boolean> = context.harborDataStore.data.map { preferences ->
        preferences[ADVANCED_TOOLS_ENABLED] ?: false
    }

    val shortcutRequestNotice: Flow<ShortcutRequestNoticeState?> =
        context.harborDataStore.data.map { preferences ->
            val packageName = preferences[SHORTCUT_REQUEST_PACKAGE]
                ?.let { value -> runCatching { PackageName(value) }.getOrNull() }
            val appLabel = preferences[SHORTCUT_REQUEST_LABEL]
            val requestAccepted = preferences[SHORTCUT_REQUEST_ACCEPTED]
            if (packageName == null || appLabel == null || requestAccepted == null) null
            else ShortcutRequestNoticeState(packageName, appLabel, requestAccepted)
        }

    suspend fun setAdvancedToolsEnabled(enabled: Boolean) {
        context.harborDataStore.edit { it[ADVANCED_TOOLS_ENABLED] = enabled }
    }

    suspend fun saveShortcutRequestNotice(
        packageName: PackageName,
        appLabel: String,
        requestAccepted: Boolean,
    ) {
        context.harborDataStore.edit {
            it[SHORTCUT_REQUEST_PACKAGE] = packageName.value
            it[SHORTCUT_REQUEST_LABEL] = appLabel.ifBlank { packageName.value }
            it[SHORTCUT_REQUEST_ACCEPTED] = requestAccepted
        }
    }

    suspend fun clearShortcutRequestNotice() {
        context.harborDataStore.edit {
            it.remove(SHORTCUT_REQUEST_PACKAGE)
            it.remove(SHORTCUT_REQUEST_LABEL)
            it.remove(SHORTCUT_REQUEST_ACCEPTED)
        }
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
        val SHORTCUT_REQUEST_PACKAGE = stringPreferencesKey("shortcut_request_package")
        val SHORTCUT_REQUEST_LABEL = stringPreferencesKey("shortcut_request_label")
        val SHORTCUT_REQUEST_ACCEPTED = booleanPreferencesKey("shortcut_request_accepted")
        val SIGNER_DIGEST = Regex("[0-9a-fA-F]{64}")

        fun shortcutKey(id: String) = androidx.datastore.preferences.core.stringPreferencesKey("shortcut_$id")

        fun shortcutSignerKey(id: String) =
            androidx.datastore.preferences.core.stringPreferencesKey("shortcut_signers_$id")
    }
}
