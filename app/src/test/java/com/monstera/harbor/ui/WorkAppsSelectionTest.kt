package com.monstera.harbor.ui

import com.monstera.harbor.core.data.ManagedApp
import com.monstera.harbor.core.topology.PackageName
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkAppsSelectionTest {
    private val apps = listOf(
        ManagedApp(PackageName("com.example.alpha"), "Alpha", false, true, false, true),
        ManagedApp(PackageName("com.example.beta"), "Beta", false, true, false, true),
        ManagedApp(PackageName("com.example.system"), "System", true, true, false, true),
    )

    @Test
    fun selectAllUsesOnlyFilteredNonSystemApps() {
        assertEquals(setOf(PackageName("com.example.beta")), WorkAppsSelection.selectAllVisible(apps, "beta"))
    }
}
