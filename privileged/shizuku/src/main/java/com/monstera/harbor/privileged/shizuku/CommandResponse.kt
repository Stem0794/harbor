package com.monstera.harbor.privileged.shizuku

internal data class CommandResponse(val exitCode: Int, val output: String) {
    val isSuccess: Boolean get() = exitCode == 0

    fun encode(): String = "$exitCode\n$output"

    companion object {
        fun decode(encoded: String): CommandResponse {
            val separator = encoded.indexOf('\n')
            if (separator < 0) return CommandResponse(-1, encoded)
            return CommandResponse(
                exitCode = encoded.substring(0, separator).toIntOrNull() ?: -1,
                output = encoded.substring(separator + 1).trim(),
            )
        }
    }
}
