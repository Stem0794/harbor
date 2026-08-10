package com.monstera.harbor.core.data

import com.monstera.harbor.core.topology.CloneCandidate
import com.monstera.harbor.core.topology.PackageName

object CloneCandidateEnricher {
    fun enrich(
        packages: Iterable<PackageName>,
        metadata: Map<PackageName, PackagePresentation>,
        installedInTarget: Set<PackageName>,
    ): List<CloneCandidate> = packages.map { packageName ->
        val presentation = metadata[packageName]
        CloneCandidate(
            packageName = packageName,
            label = presentation?.label ?: packageName.value,
            isSystem = presentation?.isSystem ?: false,
            alreadyInstalledInTarget = packageName in installedInTarget,
        )
    }.distinctBy(CloneCandidate::packageName)
        .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
}
