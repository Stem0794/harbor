package com.monstera.harbor

import android.content.pm.PackageManager
import java.security.MessageDigest

/** Returns the current package signer identities as stable SHA-256 fingerprints. */
internal object PackageSigner {
    fun fingerprints(packageManager: PackageManager, packageName: String): Set<String> = runCatching {
        val packageInfo = packageManager.getPackageInfo(
            packageName,
            PackageManager.GET_SIGNING_CERTIFICATES,
        )
        val signingInfo = packageInfo.signingInfo ?: return@runCatching emptySet()
        // Pin the certificates currently signing the installed APK. Deliberately do
        // not accept signing history: a shortcut must not authorize a replacement
        // package merely because it shares an older certificate lineage.
        val signatures = signingInfo.apkContentsSigners
        signatures.map { signature ->
            MessageDigest.getInstance("SHA-256")
                .digest(signature.toByteArray())
                .toHex()
        }.toSet()
    }.getOrDefault(emptySet())

    private fun ByteArray.toHex(): String = buildString(size * 2) {
        for (byte in this@toHex) {
            val value = byte.toInt() and 0xff
            append(HEX[value ushr 4])
            append(HEX[value and 0x0f])
        }
    }

    private const val HEX = "0123456789abcdef"
}
