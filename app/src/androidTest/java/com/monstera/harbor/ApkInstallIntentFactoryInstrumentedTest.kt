package com.monstera.harbor

import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class ApkInstallIntentFactoryInstrumentedTest {
    @Test
    fun importerCopiesSingleApkAndResolvesSystemInstaller() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val resolver = context.contentResolver
        val sourceName = "Harbor-instrumented-${UUID.randomUUID()}.apk"
        val sourceBytes = "Harbor instrumentation APK placeholder".encodeToByteArray()
        var sourceUri: Uri? = null

        try {
            sourceUri = createMediaStoreSource(resolver, sourceName, sourceBytes)
            val result = SharedFileImporter(resolver).import(
                Intent(Intent.ACTION_SEND).apply {
                    type = ApkInstallIntentFactory.APK_MIME_TYPE
                    putExtra(Intent.EXTRA_STREAM, sourceUri)
                },
            )

            assertTrue("Expected a successful import, got $result", result is ImportState.Success)
            val success = result as ImportState.Success
            val installerIntent = success.installerIntent
            assertNotNull("A single APK import should offer the system installer", installerIntent)
            assertEquals(Intent.ACTION_VIEW, installerIntent?.action)
            assertEquals(ApkInstallIntentFactory.APK_MIME_TYPE, installerIntent?.type)

            val copiedUri = installerIntent?.data
            assertNotNull("The installer intent should point at the copied MediaStore row", copiedUri)
            resolver.openInputStream(copiedUri!!).use { input ->
                assertNotNull("The copied APK should remain readable", input)
                assertArrayEquals(sourceBytes, input!!.readBytes())
            }

            val resolved = context.packageManager.resolveActivity(
                installerIntent,
                0,
            )
            assertNotNull("Android should resolve the APK intent to a package installer", resolved)
            val packageName = resolved?.activityInfo?.packageName.orEmpty()
            assertTrue(
                "Resolved APK handler was $packageName, not a package installer",
                packageName.contains("packageinstaller", ignoreCase = true),
            )
        } finally {
            findCopiedRows(resolver, sourceName).forEach { uri ->
                runCatching { resolver.delete(uri, null, null) }
            }
            sourceUri?.let { uri ->
                runCatching { resolver.delete(uri, null, null) }
            }
        }
    }

    @Test
    fun singleApkUsesSystemInstallerAndReadGrant() {
        val uri = Uri.parse("content://com.monstera.harbor.files/downloads/harbor.apk")
        val intent = ApkInstallIntentFactory.create(
            sharedUriCount = 1,
            copiedFiles = listOf(
                CopiedFile(
                    name = "Harbor-123-harbor.apk",
                    uri = uri,
                    mimeType = "application/octet-stream",
                ),
            ),
        )

        assertNotNull(intent)
        assertEquals(Intent.ACTION_VIEW, intent?.action)
        assertEquals(uri, intent?.data)
        assertEquals(ApkInstallIntentFactory.APK_MIME_TYPE, intent?.type)
        assertTrue((intent?.flags ?: 0).and(Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0)
        assertEquals(uri, intent?.clipData?.getItemAt(0)?.uri)
    }

    @Test
    fun multipleSharedFilesStayInTheCopyOnlyFlow() {
        val file = CopiedFile(
            name = "Harbor-123-harbor.apk",
            uri = Uri.parse("content://media/downloads/harbor.apk"),
            mimeType = ApkInstallIntentFactory.APK_MIME_TYPE,
        )

        assertNull(
            ApkInstallIntentFactory.create(
                sharedUriCount = 2,
                copiedFiles = listOf(file),
            ),
        )
    }

    @Test
    fun nonContentUriIsNeverGrantedToAnInstaller() {
        val file = CopiedFile(
            name = "Harbor-123-harbor.apk",
            uri = Uri.parse("file:///sdcard/Download/harbor.apk"),
            mimeType = ApkInstallIntentFactory.APK_MIME_TYPE,
        )

        assertNull(
            ApkInstallIntentFactory.create(
                sharedUriCount = 1,
                copiedFiles = listOf(file),
            ),
        )
    }

    private fun createMediaStoreSource(
        resolver: android.content.ContentResolver,
        name: String,
        bytes: ByteArray,
    ): Uri {
        val uri = resolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, ApkInstallIntentFactory.APK_MIME_TYPE)
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    "${Environment.DIRECTORY_DOWNLOADS}/HarborInstrumentation",
                )
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            },
        ) ?: error("Could not create the MediaStore source row")

        try {
            resolver.openOutputStream(uri)?.use { output -> output.write(bytes) }
                ?: error("Could not open the MediaStore source row")
            resolver.update(
                uri,
                ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) },
                null,
                null,
            )
            return uri
        } catch (error: Throwable) {
            resolver.delete(uri, null, null)
            throw error
        }
    }

    private fun findCopiedRows(
        resolver: android.content.ContentResolver,
        sourceName: String,
    ): List<Uri> {
        val suffix = "-$sourceName"
        return resolver.query(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            buildList {
                while (cursor.moveToNext()) {
                    val name = cursor.getString(nameColumn)
                    if (name?.endsWith(suffix) == true) {
                        add(ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cursor.getLong(idColumn)))
                    }
                }
            }
        }.orEmpty()
    }
}
