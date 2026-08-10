package com.monstera.harbor.privileged.shizuku

import java.io.Reader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BoundedOutputTest {
    @Test
    fun drainsReaderAfterStoredOutputReachesLimit() {
        val reader = CountingReader("x".repeat(100_000))

        val output = drainBoundedOutput(reader, 128)

        assertEquals(100_000, reader.charactersRead)
        assertTrue(output.length <= 128)
        assertTrue(output.endsWith("[output truncated]"))
    }

    @Test
    fun preservesSmallOutput() {
        val output = drainBoundedOutput("package:com.example.app\n".reader(), 128)

        assertEquals("package:com.example.app", output)
    }

    private class CountingReader(private val value: String) : Reader() {
        var charactersRead = 0
            private set

        override fun read(buffer: CharArray, offset: Int, length: Int): Int {
            if (charactersRead == value.length) return -1
            val count = minOf(length, value.length - charactersRead)
            value.toCharArray(buffer, offset, charactersRead, charactersRead + count)
            charactersRead += count
            return count
        }

        override fun close() = Unit
    }
}
