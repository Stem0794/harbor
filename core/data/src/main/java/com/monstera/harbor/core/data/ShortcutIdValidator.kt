package com.monstera.harbor.core.data

import java.util.UUID

object ShortcutIdValidator {
    fun normalize(value: String?): String? = value?.let {
        runCatching { UUID.fromString(it).toString() }.getOrNull()
    }
}
