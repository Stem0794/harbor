package com.monstera.harbor.core.policy

import com.monstera.harbor.core.topology.PackageName
import org.junit.Assert.assertEquals
import org.junit.Test

class CrossProfilePackagePolicyTest {
    private val target = PackageName("com.example.target")

    @Test
    fun enablingMergesTargetIntoCurrentAllowlist() {
        assertEquals(
            setOf("com.example.existing", target.value),
            updatedCrossProfilePackages(
                current = setOf("com.example.existing"),
                packageName = target,
                enabled = true,
            ),
        )
    }

    @Test
    fun disablingRemovesOnlyTargetFromCurrentAllowlist() {
        assertEquals(
            setOf("com.example.existing", "com.example.other"),
            updatedCrossProfilePackages(
                current = setOf("com.example.existing", target.value, "com.example.other"),
                packageName = target,
                enabled = false,
            ),
        )
    }

    @Test
    fun api29ReportsUnsupportedWithoutInspectingMembership() {
        assertEquals(
            CrossProfilePackageAccess.Unsupported,
            crossProfilePackageAccess(
                apiLevel = 29,
                current = setOf(target.value),
                packageName = target,
            ),
        )
    }

    @Test
    fun api30DefaultsToDisabledWhenPackageIsAbsent() {
        assertEquals(
            CrossProfilePackageAccess.Disabled,
            crossProfilePackageAccess(
                apiLevel = 30,
                current = emptySet(),
                packageName = target,
            ),
        )
    }

    @Test
    fun api30ReportsEnabledWhenPackageIsPresent() {
        assertEquals(
            CrossProfilePackageAccess.Enabled,
            crossProfilePackageAccess(
                apiLevel = 30,
                current = setOf(target.value),
                packageName = target,
            ),
        )
    }
}
