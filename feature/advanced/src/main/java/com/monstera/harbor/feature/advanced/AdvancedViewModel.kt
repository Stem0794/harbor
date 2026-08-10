package com.monstera.harbor.feature.advanced

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.monstera.harbor.core.topology.PrivilegedBackend
import com.monstera.harbor.core.topology.PrivilegedResult
import com.monstera.harbor.core.topology.SystemDiagnostics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdvancedUiState(
    val diagnostics: SystemDiagnostics? = null,
    val status: String? = null,
    val busy: Boolean = false,
)

class AdvancedViewModel(private val backend: PrivilegedBackend) : ViewModel() {
    private val _state = MutableStateFlow(AdvancedUiState())
    val state = _state.asStateFlow()

    fun setBusy(value: Boolean) { _state.value = _state.value.copy(busy = value) }
    fun setStatus(value: String?) { _state.value = _state.value.copy(status = value) }

    fun refreshDiagnostics() {
        if (_state.value.busy) return
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true, status = null)
            val result = try {
                backend.diagnostics()
            } catch (error: Throwable) {
                PrivilegedResult.Failure(error.message ?: error.javaClass.simpleName)
            }
            _state.value = when (result) {
                is PrivilegedResult.Success -> _state.value.copy(
                    diagnostics = result.value,
                    status = "Diagnostics refreshed",
                    busy = false,
                )
                is PrivilegedResult.Failure -> _state.value.copy(status = result.reason, busy = false)
            }
        }
    }

    fun runOperation(operation: suspend () -> PrivilegedResult<*>) {
        if (_state.value.busy) return
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true, status = null)
            val result = try {
                operation()
            } catch (error: Throwable) {
                PrivilegedResult.Failure(error.message ?: error.javaClass.simpleName)
            }
            _state.value = _state.value.copy(
                status = when (result) {
                    is PrivilegedResult.Success -> "Operation completed"
                    is PrivilegedResult.Failure -> result.reason
                },
                busy = false,
            )
        }
    }

    class Factory(private val backend: PrivilegedBackend) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(AdvancedViewModel::class.java))
            return AdvancedViewModel(backend) as T
        }
    }
}
