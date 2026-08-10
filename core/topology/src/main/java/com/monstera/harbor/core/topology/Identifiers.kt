package com.monstera.harbor.core.topology

private val PACKAGE_NAME = Regex("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)+")

@JvmInline
value class AndroidUserId(val value: Int) {
    init {
        require(value >= 0) { "Android user ID must be non-negative" }
    }
}

@JvmInline
value class PackageName(val value: String) {
    init {
        require(PACKAGE_NAME.matches(value)) { "Invalid Android package name" }
    }
}

@JvmInline
value class UserVisibleName(val value: String) {
    init {
        require(value.isNotBlank()) { "User name must not be blank" }
        require(value.length <= 64) { "User name is too long" }
        require(value == value.trim() && value.first().isLetterOrDigit()) {
            "User name must start with a letter or number and have no surrounding spaces"
        }
        require(value.all { character ->
            character.isLetterOrDigit() || character == ' ' || character in "-_.\u0027"
        }) {
            "User name contains unsupported characters"
        }
    }
}
