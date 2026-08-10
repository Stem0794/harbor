package io.github.theodorekonikowski.harbor.privileged.shizuku

import android.content.Context
import android.system.Os
import androidx.annotation.Keep
import io.github.theodorekonikowski.harbor.core.topology.AndroidUserId
import io.github.theodorekonikowski.harbor.core.topology.PackageName
import io.github.theodorekonikowski.harbor.core.topology.UserVisibleName
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

@Keep
class HarborUserService() : IHarborUserService.Stub() {
    @Keep
    constructor(@Suppress("UNUSED_PARAMETER") context: Context) : this()

    override fun destroy() {
        System.exit(0)
    }

    override fun diagnostics(): String {
        val current = run(PrivilegedCommandFactory.currentUser())
        val maximum = run(PrivilegedCommandFactory.maximumUsers())
        val users = run(PrivilegedCommandFactory.listUsers())
        val output = buildString {
            appendLine("UID=${Os.getuid()}")
            appendLine("CURRENT_EXIT=${current.exitCode}")
            appendLine("CURRENT=${current.output.lineSequence().firstOrNull().orEmpty()}")
            appendLine("MAX_EXIT=${maximum.exitCode}")
            appendLine("MAX=${maximum.output}")
            appendLine("USERS_EXIT=${users.exitCode}")
            appendLine("USERS_BEGIN")
            append(users.output)
        }
        return CommandResponse(0, output.trim()).encode()
    }

    override fun listUsers(): String = run(PrivilegedCommandFactory.listUsers()).encode()

    override fun installExisting(packageName: String, userId: Int): String = validated {
        PrivilegedCommandFactory.installExisting(PackageName(packageName), AndroidUserId(userId))
    }

    override fun createFullUser(name: String): String = validated {
        PrivilegedCommandFactory.createFullUser(UserVisibleName(name))
    }

    override fun installHarbor(packageName: String, userId: Int): String =
        installExisting(packageName, userId)

    override fun switchUser(userId: Int): String = validated {
        PrivilegedCommandFactory.switchUser(AndroidUserId(userId))
    }

    override fun listPackages(userId: Int): String = validated {
        PrivilegedCommandFactory.listPackages(AndroidUserId(userId))
    }

    private inline fun validated(command: () -> List<String>): String = runCatching { run(command()) }
        .fold(
            onSuccess = CommandResponse::encode,
            onFailure = { CommandResponse(2, it.message ?: "Invalid command arguments").encode() },
        )

    private fun run(arguments: List<String>): CommandResponse {
        require(arguments.isNotEmpty())
        val process = ProcessBuilder(arguments)
            .redirectErrorStream(true)
            .start()
        val output = StringBuffer()
        val outputReader = thread(
            start = true,
            isDaemon = true,
            name = "harbor-command-output",
        ) {
            process.inputStream.bufferedReader().use { reader ->
                var total = 0
                while (total < MAX_OUTPUT_CHARS) {
                    val line = reader.readLine() ?: break
                    val remaining = MAX_OUTPUT_CHARS - total
                    val accepted = line.take(remaining)
                    output.append(accepted).append('\n')
                    total += accepted.length + 1
                }
            }
        }
        if (!process.waitFor(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            process.destroyForcibly()
            outputReader.join(OUTPUT_READER_JOIN_MILLIS)
            return CommandResponse(124, "Operation timed out")
        }
        outputReader.join(OUTPUT_READER_JOIN_MILLIS)
        return CommandResponse(process.exitValue(), output.toString().trim())
    }

    private companion object {
        const val COMMAND_TIMEOUT_SECONDS = 30L
        const val MAX_OUTPUT_CHARS = 16_384
        const val OUTPUT_READER_JOIN_MILLIS = 1_000L
    }
}
