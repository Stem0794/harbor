package com.monstera.harbor.privileged.shizuku

import java.io.Reader

internal fun drainBoundedOutput(
    reader: Reader,
    maximumCharacters: Int,
): String {
    require(maximumCharacters > 0)
    val stored = StringBuilder(minOf(maximumCharacters, OUTPUT_BUFFER_SIZE))
    val buffer = CharArray(OUTPUT_BUFFER_SIZE)
    var truncated = false
    while (true) {
        val count = reader.read(buffer)
        if (count < 0) break
        val remaining = maximumCharacters - stored.length
        if (remaining > 0) stored.append(buffer, 0, minOf(count, remaining))
        if (count > remaining) truncated = true
    }
    if (!truncated) return stored.toString().trim()
    val marker = "\n[output truncated]"
    val prefixLength = (maximumCharacters - marker.length).coerceAtLeast(0)
    return buildString(maximumCharacters) {
        append(stored.take(prefixLength))
        append(marker.take(maximumCharacters - length))
    }.trim()
}

private const val OUTPUT_BUFFER_SIZE = 4_096
