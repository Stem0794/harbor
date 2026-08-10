package com.monstera.harbor.privileged.shizuku

import com.monstera.harbor.core.topology.AndroidUserId
import com.monstera.harbor.core.topology.PackageName
import com.monstera.harbor.core.topology.UserVisibleName
import org.junit.Assert.assertEquals
import org.junit.Test

class PrivilegedCommandFactoryTest {
    @Test fun installExistingUsesDiscreteArguments() {
        assertEquals(
            listOf("/system/bin/cmd", "package", "install-existing", "--user", "10", "org.example.app"),
            PrivilegedCommandFactory.installExisting(PackageName("org.example.app"), AndroidUserId(10)),
        )
    }

    @Test fun userNameRemainsOneArgument() {
        assertEquals(
            listOf("/system/bin/pm", "create-user", "Harbor Lab"),
            PrivilegedCommandFactory.createFullUser(UserVisibleName("Harbor Lab")),
        )
    }


    @Test fun packageListingIsScopedToOneUser() {
        assertEquals(
            listOf("/system/bin/pm", "list", "packages", "--user", "12"),
            PrivilegedCommandFactory.listPackages(AndroidUserId(12)),
        )
    }
}
