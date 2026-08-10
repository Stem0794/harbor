package com.monstera.harbor.core.topology

import org.junit.Assert.assertEquals
import org.junit.Test

class HarborPrivilegeResolverTest {
    @Test
    fun disabledAdvancedAlwaysUsesStandard() {
        val result = HarborPrivilegeResolver.resolve(
            advancedEnabled = false,
            backend = PrivilegedBackendState(PrivilegedAvailability.READY, effectiveUid = 0),
        )

        assertEquals(HarborPrivilegeLevel.STANDARD, result.level)
        assertEquals(HarborPrivilegeSecondaryState.ADVANCED_DISABLED, result.secondary)
    }

    @Test
    fun shellAndRootHaveDistinctLevels() {
        val adb = HarborPrivilegeResolver.resolve(
            advancedEnabled = true,
            backend = PrivilegedBackendState(PrivilegedAvailability.READY, effectiveUid = 2_000),
        )
        val root = HarborPrivilegeResolver.resolve(
            advancedEnabled = true,
            backend = PrivilegedBackendState(PrivilegedAvailability.READY, effectiveUid = 0),
        )

        assertEquals(HarborPrivilegeLevel.SHIZUKU_ADB, adb.level)
        assertEquals(HarborPrivilegeLevel.SHIZUKU_ROOT, root.level)
        assertEquals(HarborPrivilegeSecondaryState.ROOT_ALLOWLIST_ONLY, root.secondary)
    }

    @Test
    fun unexpectedUidFailsClosed() {
        val result = HarborPrivilegeResolver.resolve(
            advancedEnabled = true,
            backend = PrivilegedBackendState(PrivilegedAvailability.READY, effectiveUid = 10_000),
        )

        assertEquals(HarborPrivilegeLevel.STANDARD, result.level)
        assertEquals(HarborPrivilegeSecondaryState.UNSUPPORTED, result.secondary)
    }
}
