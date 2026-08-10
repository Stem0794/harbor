package io.github.theodorekonikowski.harbor.privileged.shizuku

import io.github.theodorekonikowski.harbor.core.topology.AndroidUserId
import io.github.theodorekonikowski.harbor.core.topology.PackageName
import io.github.theodorekonikowski.harbor.core.topology.UserVisibleName

internal object PrivilegedCommandFactory {
    fun listUsers(): List<String> = listOf("/system/bin/cmd", "user", "list")

    fun currentUser(): List<String> = listOf("/system/bin/am", "get-current-user")

    fun maximumUsers(): List<String> = listOf("/system/bin/pm", "get-max-users")

    fun listPackages(userId: AndroidUserId): List<String> =
        listOf("/system/bin/pm", "list", "packages", "--user", userId.value.toString())

    fun installExisting(packageName: PackageName, userId: AndroidUserId): List<String> = listOf(
        "/system/bin/cmd",
        "package",
        "install-existing",
        "--user",
        userId.value.toString(),
        packageName.value,
    )

    fun createFullUser(name: UserVisibleName): List<String> =
        listOf("/system/bin/pm", "create-user", name.value)

    fun switchUser(userId: AndroidUserId): List<String> =
        listOf("/system/bin/am", "switch-user", userId.value.toString())
}
