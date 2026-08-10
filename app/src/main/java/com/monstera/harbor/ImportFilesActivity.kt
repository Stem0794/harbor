package com.monstera.harbor

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.monstera.harbor.ui.theme.HarborTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Receives a user-selected personal file through Android's supported share flow. */
class ImportFilesActivity : ComponentActivity() {
    private var state by mutableStateOf<ImportState>(ImportState.Loading)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HarborTheme {
                ImportFilesScreen(state = state, onDone = ::finish)
            }
        }
        lifecycleScope.launch {
            state = withContext(Dispatchers.IO) {
                SharedFileImporter(contentResolver).import(intent)
            }
        }
    }
}

internal sealed interface ImportState {
    data object Loading : ImportState

    data class Success(val names: List<String>) : ImportState

    data class Failure(val message: String) : ImportState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImportFilesScreen(state: ImportState, onDone: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Send to Harbor work profile") }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when (state) {
                ImportState.Loading -> {
                    CircularProgressIndicator()
                    Text("Copying the selected file into this work profile…")
                }

                is ImportState.Success -> {
                    Text("File copied to work Downloads", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "The personal original was not deleted. Open the work-profile Files app and look in Downloads/Harbor.",
                    )
                    state.names.forEach { name ->
                        Text(name, style = MaterialTheme.typography.bodySmall)
                    }
                    Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Done") }
                }

                is ImportState.Failure -> {
                    Text("File was not copied", style = MaterialTheme.typography.headlineSmall)
                    Text(state.message)
                    Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Close") }
                }
            }
        }
    }
}

internal class SharedFileImporter(private val resolver: ContentResolver) {
    fun import(intent: Intent): ImportState {
        val uris = sharedUris(intent)
        if (uris.isEmpty()) {
            return ImportState.Failure(
                "No content URI was received. From the personal profile, use Share and select Harbor with the work badge.",
            )
        }

        val result = copyUris(uris, intent.type)
        return when {
            result.names.isNotEmpty() -> ImportState.Success(result.names)
            else -> ImportState.Failure(
                "This source did not provide a readable Android content URI. Try Share instead of the OEM Move action.",
            )
        }
    }

    internal fun copyUris(uris: List<Uri>, sharedMimeType: String?): CopyResult {
        val names = mutableListOf<String>()
        uris.take(MAX_SHARED_FILES).forEachIndexed { index, uri ->
            if (uri.scheme != ContentResolver.SCHEME_CONTENT) return@forEachIndexed
            runCatching { copyToWorkDownloads(uri, sharedMimeType, index) }
                .onSuccess(names::add)
        }
        return CopyResult(names)
    }

    private fun copyToWorkDownloads(uri: Uri, sharedMimeType: String?, index: Int): String {
        val sourceName = queryDisplayName(uri)
        val displayName = uniqueDisplayName(sourceName, index)
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, resolver.getType(uri) ?: sharedMimeType ?: "application/octet-stream")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/Harbor")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val destination = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: error("Android could not create a work-profile Downloads entry")
        try {
            resolver.openInputStream(uri)?.use { input ->
                resolver.openOutputStream(destination)?.use { output -> input.copyTo(output) }
                    ?: error("Android could not open the work-profile destination")
            } ?: error("The source app did not grant Harbor access to the file")
            resolver.update(
                destination,
                ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) },
                null,
                null,
            )
            return displayName
        } catch (error: Throwable) {
            resolver.delete(destination, null, null)
            throw error
        }
    }

    private fun queryDisplayName(uri: Uri): String? = runCatching {
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor: Cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    }.getOrNull()

    private fun uniqueDisplayName(sourceName: String?, index: Int): String {
        val safe = sourceName.orEmpty()
            .substringAfterLast('/')
            .replace(Regex("[^A-Za-z0-9._ -]"), "_")
            .trim()
            .take(MAX_NAME_LENGTH)
            .ifBlank { "shared-file-${index + 1}" }
        return "Harbor-${System.currentTimeMillis()}-$safe"
    }

    private fun sharedUris(intent: Intent): List<Uri> {
        val result = LinkedHashSet<Uri>()
        intent.clipData?.let { clipData ->
            for (index in 0 until clipData.itemCount) {
                clipData.getItemAt(index).uri?.let(result::add)
            }
        }
        when (intent.action) {
            Intent.ACTION_SEND -> intent.getParcelableExtraCompat<Uri>(Intent.EXTRA_STREAM)?.let(result::add)
            Intent.ACTION_SEND_MULTIPLE -> intent.getParcelableArrayListExtraCompat<Uri>(Intent.EXTRA_STREAM)
                ?.forEach { result.add(it) }
        }
        return result.toList()
    }

    private inline fun <reified T> Intent.getParcelableExtraCompat(key: String): T? =
        if (Build.VERSION.SDK_INT >= 33) getParcelableExtra(key, T::class.java)
        else @Suppress("DEPRECATION") getParcelableExtra(key)

    private inline fun <reified T : Parcelable> Intent.getParcelableArrayListExtraCompat(key: String): ArrayList<T>? =
        if (Build.VERSION.SDK_INT >= 33) getParcelableArrayListExtra(key, T::class.java)
        else @Suppress("DEPRECATION") getParcelableArrayListExtra(key)

    private companion object {
        const val MAX_SHARED_FILES = 50
        const val MAX_NAME_LENGTH = 96
    }
}

internal data class CopyResult(val names: List<String>)
