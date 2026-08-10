package com.monstera.harbor.core.policy

import com.monstera.harbor.core.topology.PackageName
import kotlinx.coroutines.flow.Flow

enum class WorkProfileStatus {
    NOT_PROFILE_OWNER,
    ACTIVE,
    UNAVAILABLE,
}

data class WorkProfileState(
    val status: WorkProfileStatus,
    val detail: String? = null,
)

sealed interface PolicyResult<out T> {
    data class Success<T>(val value: T) : PolicyResult<T>
    data class Failure(val reason: String, val recoverable: Boolean = true) : PolicyResult<Nothing>
}

interface WorkProfileController {
    fun observeState(): Flow<WorkProfileState>
    suspend fun setApplicationHidden(packageName: PackageName, hidden: Boolean): PolicyResult<Boolean>
    suspend fun isApplicationHidden(packageName: PackageName): PolicyResult<Boolean>
}
