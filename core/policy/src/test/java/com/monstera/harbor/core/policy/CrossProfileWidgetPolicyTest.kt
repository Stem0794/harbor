package com.monstera.harbor.core.policy

import com.monstera.harbor.core.topology.PackageName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CrossProfileWidgetPolicyTest {
    private val target = PackageName("com.example.target")

    @Test
    fun defaultsToDisabledWhenPackageIsAbsent() {
        assertEquals(
            CrossProfileWidgetAccess.Disabled,
            crossProfileWidgetAccess(current = listOf("com.example.other"), packageName = target),
        )
    }

    @Test
    fun reportsEnabledWhenPackageIsPresent() {
        assertEquals(
            CrossProfileWidgetAccess.Enabled,
            crossProfileWidgetAccess(current = listOf("com.example.other", target.value), packageName = target),
        )
    }

    @Test
    fun enablingAnAbsentPackageRequiresAnUpdate() {
        assertTrue(crossProfileWidgetUpdateNeeded(current = emptyList(), packageName = target, enabled = true))
    }

    @Test
    fun enablingAPresentPackageIsANoOp() {
        assertFalse(crossProfileWidgetUpdateNeeded(current = listOf(target.value), packageName = target, enabled = true))
    }

    @Test
    fun disablingAnAbsentPackageIsANoOp() {
        assertFalse(crossProfileWidgetUpdateNeeded(current = listOf("com.example.other"), packageName = target, enabled = false))
    }

    @Test
    fun disablingAPresentPackageRequiresAnUpdate() {
        assertTrue(crossProfileWidgetUpdateNeeded(current = listOf(target.value), packageName = target, enabled = false))
    }
}
