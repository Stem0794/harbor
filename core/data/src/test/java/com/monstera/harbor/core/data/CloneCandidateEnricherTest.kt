package com.monstera.harbor.core.data

import com.monstera.harbor.core.topology.PackageName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CloneCandidateEnricherTest {
    @Test
    fun usesLocalLabelsAndTargetInstalledStateWithFallback() {
        val known = PackageName("com.example.known")
        val unknown = PackageName("org.example.unknown")
        val result = CloneCandidateEnricher.enrich(
            packages = listOf(unknown, known),
            metadata = mapOf(known to PackagePresentation(known, "Known app", isSystem = false)),
            installedInTarget = setOf(known),
        )

        assertEquals("Known app", result.first().label)
        assertTrue(result.first().alreadyInstalledInTarget)
        assertEquals(unknown.value, result.last().label)
    }
}
