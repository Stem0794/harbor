package com.monstera.harbor

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApkInstallIntentFactoryTest {
    @Test
    fun apkMimeTypeIsInstallableForSingleShare() {
        assertTrue(
            ApkInstallIntentFactory.isApk(
                fileName = "Harbor-123-download.bin",
                mimeType = ApkInstallIntentFactory.APK_MIME_TYPE,
            ),
        )
    }

    @Test
    fun apkExtensionIsInstallableWhenSourceMimeTypeIsGeneric() {
        assertTrue(
            ApkInstallIntentFactory.isApk(
                fileName = "Harbor-123-app.apk",
                mimeType = "application/octet-stream",
            ),
        )
    }

    @Test
    fun nonApkFilesAreNotInstallable() {
        assertFalse(
            ApkInstallIntentFactory.isApk(
                fileName = "Harbor-123-document.pdf",
                mimeType = "application/pdf",
            ),
        )
    }
}
