package com.monstera.harbor.core.topology

object SystemUserParser {
    private val userLine = Regex("UserInfo\\{(\\d+):([^:}]*)[:]([0-9a-fA-F]+)}(.*)")

    fun parse(output: String): List<SystemUser> = output.lineSequence().mapNotNull { line ->
        val match = userLine.find(line.trim()) ?: return@mapNotNull null
        val id = match.groupValues[1].toIntOrNull() ?: return@mapNotNull null
        val flags = match.groupValues[3].toIntOrNull(16) ?: return@mapNotNull null
        SystemUser(
            id = AndroidUserId(id),
            name = match.groupValues[2].ifBlank { "User $id" },
            flags = flags,
            isRunning = match.groupValues[4].contains("running", ignoreCase = true),
        )
    }.toList()
}
