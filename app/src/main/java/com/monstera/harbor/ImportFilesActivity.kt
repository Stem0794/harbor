package com.monstera.harbor

import android.content.ClipData
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import android.provider.OpenableColumns
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
import java.util.Locale

/** Receives a user-selected personal file through Android's supported share flow. */
class ImportFilesActivity : ComponentActivity() {
    private var state by mutableStateOf<ImportState>(ImportState.Loading)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HarborTheme {
                ImportFilesScreen(
                    state = state,
                    onDone = ::finish,
                    onOpenInstaller = ::openInstaller,
                )
            }
        }
        lifecycleScope.launch {
            val imported = withContext(Dispatchers.IO) {
                SharedFileImporter(contentResolver).import(intent)
            }
            state = imported
            if (imported is ImportState.Success) {
                imported.installerIntent?.let(::openInstaller)
            }
        }
    }

    private fun openInstaller(installerIntent: Intent) {
        runCatching { startActivity(installerIntent) }
            .onFailure { error ->
                state = (state as? ImportState.Success)?.copy(
                    installerError = error.message ?: "Android could not open the package installer",
                ) ?: state
            }
    }
}

internal sealed interface ImportState {
    data object Loading : ImportState

    data class Success(
        val names: List<String>,
        val installerIntent: Intent? = null,
        val installerError: String? = null,
    ) : ImportState

    data class Failure(val message: String) : ImportState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImportFilesScreen(
    state: ImportState,
    onDone: () -> Unit,
    onOpenInstaller: (Intent) -> Unit,
) {
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
                        if (state.installerIntent != null) {
                            "The APK is in Downloads/Harbor. Android's installer opens in Work and keeps the final confirmation with you."
                        } else {
                            "The personal original was not deleted. Open the work-profile Files app and look in Downloads/Harbor."
                        },
                    )
                    state.installerError?.let { error ->
                        Text("The APK was copied, but Android could not open its package installer: $error")
                    }
                    state.names.forEach { name ->
                        Text(name, style = MaterialTheme.typography.bodySmall)
                    }
                    state.installerIntent?.let { installerIntent ->
                        Button(
                            onClick = { onOpenInstaller(installerIntent) },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Open package installer") }
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
            result.files.isNotEmpty() -> ImportState.Success(
                names = result.files.map(CopiedFile::name),
                installerIntent = ApkInstallIntentFactory.create(
                    sharedUriCount = uris.size,
                    copiedFiles = result.files,
                ),
            )
            else -> ImportState.Failure(
                "This source did not provide a readable Android content URI. Try Share instead of the OEM Move action.",
            )
        }
    }

    internal fun copyUris(uris: List<Uri>, sharedMimeType: String?): CopyResult {
        val files = mutableListOf<CopiedFile>()
        uris.take(MAX_SHARED_FILES).forEachIndexed { index, uri ->
            if (uri.scheme != ContentResolver.SCHEME_CONTENT) return@forEachIndexed
            runCatching { copyToWorkDownloads(uri, sharedMimeType, index) }
                .onSuccess(files::add)
        }
        return CopyResult(files)
    }

    private fun copyToWorkDownloads(uri: Uri, sharedMimeType: String?, index: Int): CopiedFile {
        val sourceName = queryDisplayName(uri)
        val displayName = uniqueDisplayName(sourceName, index)
        val mimeType = resolver.getType(uri) ?: sharedMimeType ?: "application/octet-stream"
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
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
            return CopiedFile(name = displayName, uri = destination, mimeType = mimeType)
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

internal data class CopyResult(val files: List<CopiedFile>)

internal data class CopiedFile(
    val name: String,
    val uri: Uri,
    val mimeType: String,
)

internal object ApkInstallIntentFactory {
    const val APK_MIME_TYPE = "application/vnd.android.package-archive"

    fun create(sharedUriCount: Int, copiedFiles: List<CopiedFile>): Intent? {
        val file = copiedFiles.singleOrNull() ?: return null
        if (
            sharedUriCount != 1 ||
            file.uri.scheme != ContentResolver.SCHEME_CONTENT ||
            !isApk(file)
        ) return null
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(file.uri, APK_MIME_TYPE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = ClipData.newRawUri("Harbor APK", file.uri)
        }
    }

    internal fun isApk(file: CopiedFile): Boolean {
        return isApk(file.name, file.mimeType)
    }

    internal fun isApk(fileName: String, mimeType: String): Boolean {
        return mimeType.equals(APK_MIME_TYPE, ignoreCase = true) ||
            fileName.lowercase(Locale.ROOT).endsWith(".apk")
    }
}
