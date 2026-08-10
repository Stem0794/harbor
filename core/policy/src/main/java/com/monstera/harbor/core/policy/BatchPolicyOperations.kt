package com.monstera.harbor.core.policy

import com.monstera.harbor.core.topology.PackageName

data class PackageOperationResult(
    val packageName: PackageName,
    val success: Boolean,
    val reason: String? = null,
)

suspend fun WorkProfileController.setApplicationHiddenSequentially(
    packages: Iterable<PackageName>,
    hidden: Boolean,
): List<PackageOperationResult> {
    val results = mutableListOf<PackageOperationResult>()
    for (packageName in packages) {
        val result = try {
            setApplicationHidden(packageName, hidden)
        } catch (error: Throwable) {
            PolicyResult.Failure(error.message ?: error.javaClass.simpleName)
        }
        results += when (result) {
            is PolicyResult.Success -> PackageOperationResult(packageName, success = true)
            is PolicyResult.Failure -> PackageOperationResult(packageName, success = false, reason = result.reason)
        }
    }
    return results
}
