package com.monstera.harbor.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.monstera.harbor.core.data.WorkspaceMetadata
import com.monstera.harbor.core.data.WorkspaceMetadataStore
import com.monstera.harbor.core.data.WorkspaceIconKey
import com.monstera.harbor.core.topology.AndroidUserId
import com.monstera.harbor.core.topology.MultiUserController
import com.monstera.harbor.core.topology.PrivilegedBackend
import com.monstera.harbor.core.topology.PrivilegedResult
import com.monstera.harbor.core.topology.SystemUser
import com.monstera.harbor.core.topology.UserVisibleName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class WorkspaceUiState(
    val users: List<SystemUser> = emptyList(),
    val currentUserId: AndroidUserId? = null,
    val metadata: List<WorkspaceMetadata> = emptyList(),
    val loading: Boolean = false,
    val message: String? = null,
)

class WorkspaceViewModel(
    private val backend: PrivilegedBackend,
    private val multiUserController: MultiUserController,
    private val metadataStore: WorkspaceMetadataStore,
) : ViewModel() {
    private val _state = MutableStateFlow(WorkspaceUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            metadataStore.metadata.collectLatest { metadata ->
                _state.value = _state.value.copy(metadata = metadata)
            }
        }
    }

    fun refresh() {
        if (_state.value.loading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, message = null)
            val diagnostics = try {
                backend.diagnostics()
            } catch (error: Throwable) {
                PrivilegedResult.Failure(error.message ?: error.javaClass.simpleName)
            }
            val usersResult = try {
                multiUserController.listUsers()
            } catch (error: Throwable) {
                PrivilegedResult.Failure(error.message ?: error.javaClass.simpleName)
            }
            val currentUserId = when (diagnostics) {
                is PrivilegedResult.Success -> diagnostics.value.currentUser
                is PrivilegedResult.Failure -> null
            }
            when (usersResult) {
                is PrivilegedResult.Success -> {
                    metadataStore.reconcile(usersResult.value)
                    _state.value = _state.value.copy(
                        users = usersResult.value,
                        currentUserId = currentUserId,
                        loading = false,
                        message = (diagnostics as? PrivilegedResult.Failure)?.reason,
                    )
                }
                is PrivilegedResult.Failure -> {
                    _state.value = _state.value.copy(
                        loading = false,
                        message = usersResult.reason,
                    )
                }
            }
        }
    }

    fun clear() {
        _state.value = WorkspaceUiState(metadata = _state.value.metadata)
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }

    fun setMessage(message: String) {
        _state.value = _state.value.copy(message = message)
    }

    fun switchUser(user: SystemUser) = runAction("Switch requested") {
        switchUser(user.id)
    }

    fun installHarbor(user: SystemUser) = runAction("Harbor installation requested") {
        installHarbor(user.id)
    }

    fun createFullUser(name: UserVisibleName) {
        if (_state.value.loading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, message = null)
            val result = try {
                multiUserController.createFullUser(name)
            } catch (error: Throwable) {
                PrivilegedResult.Failure(error.message ?: error.javaClass.simpleName)
            }
            _state.value = _state.value.copy(
                loading = false,
                message = when (result) {
                    is PrivilegedResult.Success -> "Created workspace ${result.value.name}"
                    is PrivilegedResult.Failure -> result.reason
                },
            )
            if (result is PrivilegedResult.Success) refresh()
        }
    }

    fun rename(user: SystemUser, alias: String?) {
        viewModelScope.launch { metadataStore.setAlias(user, alias) }
    }

    fun setIcon(user: SystemUser, icon: WorkspaceIconKey) {
        viewModelScope.launch { metadataStore.setIcon(user, icon) }
    }

    private fun runAction(successMessage: String, action: suspend MultiUserController.() -> PrivilegedResult<Unit>) {
        if (_state.value.loading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, message = null)
            val result = try {
                action(multiUserController)
            } catch (error: Throwable) {
                PrivilegedResult.Failure(error.message ?: error.javaClass.simpleName)
            }
            _state.value = _state.value.copy(
                loading = false,
                message = when (result) {
                    is PrivilegedResult.Success -> successMessage
                    is PrivilegedResult.Failure -> result.reason
                },
            )
        }
    }

    class Factory(
        private val backend: PrivilegedBackend,
        private val multiUserController: MultiUserController,
        private val metadataStore: WorkspaceMetadataStore,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(WorkspaceViewModel::class.java))
            return WorkspaceViewModel(backend, multiUserController, metadataStore) as T
        }
    }
}
