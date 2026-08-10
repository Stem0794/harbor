package com.monstera.harbor.core.topology

object SystemUserParser {
    data class ParseResult(
        val users: List<SystemUser>,
        val malformedUserLines: List<String>,
    ) {
        val isComplete: Boolean get() = malformedUserLines.isEmpty()
    }

    private val userLine = Regex("^UserInfo\\{(\\d+):([^:}]*)[:]([0-9a-fA-F]+)}(?:\\s+(.*))?$")

    fun parse(output: String): List<SystemUser> = parseResult(output).users

    fun parseResult(output: String): ParseResult {
        val users = mutableListOf<SystemUser>()
        val malformed = mutableListOf<String>()
        output.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            val match = userLine.matchEntire(line)
            if (match == null) {
                if (line.contains("UserInfo{")) malformed += line
                return@forEach
            }
            val id = match.groupValues[1].toIntOrNull()
            val flags = match.groupValues[3].toIntOrNull(16)
            if (id == null || flags == null) {
                malformed += line
                return@forEach
            }
            users += SystemUser(
                id = AndroidUserId(id),
                name = match.groupValues[2].ifBlank { "User $id" },
                flags = flags,
                isRunning = match.groupValues[4].contains("running", ignoreCase = true),
            )
        }
        return ParseResult(users, malformed)
    }
}
