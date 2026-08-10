package com.monstera.harbor.core.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ShortcutIdValidatorTest {
    @Test
    fun acceptsOnlyUuidIds() {
        val id = "123e4567-e89b-12d3-a456-426614174000"
        assertEquals(id, ShortcutIdValidator.normalize(id))
        assertNull(ShortcutIdValidator.normalize("package=com.example.app"))
        assertNull(ShortcutIdValidator.normalize(null))
    }
}
