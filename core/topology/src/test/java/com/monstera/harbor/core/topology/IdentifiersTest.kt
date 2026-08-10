package com.monstera.harbor.core.topology

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class IdentifiersTest {
    @Test fun validPackageName() {
        assertEquals("org.example.app", PackageName("org.example.app").value)
    }

    @Test fun packageNameRejectsShellCharacters() {
        assertThrows(IllegalArgumentException::class.java) { PackageName("org.example.app;id") }
    }

    @Test fun userNameRejectsNewlines() {
        assertThrows(IllegalArgumentException::class.java) { UserVisibleName("Lab\nUser") }
    }

    @Test fun userNameRejectsOptionLikeInput() {
        assertThrows(IllegalArgumentException::class.java) { UserVisibleName("--profileOf 0") }
    }

    @Test fun userNameAllowsInternationalLetters() {
        assertEquals("Équipe Démo", UserVisibleName("Équipe Démo").value)
    }
}
