package com.monstera.harbor.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.monstera.harbor.core.data.AppCatalogRepository
import com.monstera.harbor.core.data.ManagedApp
import com.monstera.harbor.core.policy.PackageOperationResult
import com.monstera.harbor.core.policy.PolicyResult
import com.monstera.harbor.core.policy.WorkProfileController
import com.monstera.harbor.core.policy.setApplicationHiddenSequentially
import com.monstera.harbor.core.topology.PackageName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class WorkAppsUiState(
    val apps: List<ManagedApp> = emptyList(),
    val query: String = "",
    val selectedPackages: Set<PackageName> = emptySet(),
    val operationInProgress: Boolean = false,
    val message: String? = null,
)

object WorkAppsSelection {
    fun filter(apps: List<ManagedApp>, query: String): List<ManagedApp> = apps.filter {
        query.isBlank() || it.label.contains(query, true) || it.packageName.value.contains(query, true)
    }

    fun selectAllVisible(apps: List<ManagedApp>, query: String): Set<PackageName> =
        filter(apps, query).filterNot(ManagedApp::isSystem).mapTo(linkedSetOf(), ManagedApp::packageName)
}

class WorkAppsViewModel(
    private val catalog: AppCatalogRepository,
    private val controller: WorkProfileController,
    private val ownPackage: String,
) : ViewModel() {
    private val _state = MutableStateFlow(WorkAppsUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            catalog.observeApps().collectLatest { apps ->
                val filtered = apps.filterNot { it.packageName.value == ownPackage }
                val validSelection = _state.value.selectedPackages.intersect(filtered.map { it.packageName }.toSet())
                _state.value = _state.value.copy(apps = filtered, selectedPackages = validSelection)
            }
        }
    }

    fun setQuery(query: String) {
        _state.value = _state.value.copy(query = query)
    }

    fun toggleSelection(packageName: PackageName) {
        val current = _state.value.selectedPackages
        _state.value = _state.value.copy(
            selectedPackages = if (packageName in current) current - packageName else current + packageName,
        )
    }

    fun selectAllVisible() {
        val visible = WorkAppsSelection.selectAllVisible(_state.value.apps, _state.value.query)
        _state.value = _state.value.copy(selectedPackages = visible)
    }

    fun clearSelection() {
        _state.value = _state.value.copy(selectedPackages = emptySet())
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }

    fun setApplicationHidden(packageName: PackageName, hidden: Boolean) {
        if (_state.value.operationInProgress) return
        val app = _state.value.apps.firstOrNull { it.packageName == packageName }
        if (app == null || app.isSystem || packageName.value == ownPackage) {
            _state.value = _state.value.copy(message = "This app cannot be changed from Harbor")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(operationInProgress = true, message = null)
            val message = when (val result = controller.setApplicationHidden(packageName, hidden)) {
                is PolicyResult.Success -> {
                    updateHiddenState(packageName, hidden)
                    null
                }
                is PolicyResult.Failure -> result.reason
            }
            _state.value = _state.value.copy(operationInProgress = false, message = message)
        }
    }

    fun setSelectedHidden(hidden: Boolean) {
        if (_state.value.operationInProgress) return
        val selected = _state.value.selectedPackages
            .mapNotNull { packageName -> _state.value.apps.find { it.packageName == packageName } }
            .filter { !it.isSystem && it.isHidden != hidden }
            .map { it.packageName }
        if (selected.isEmpty()) {
            _state.value = _state.value.copy(message = "No selected apps need this action")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(operationInProgress = true, message = null)
            val results = controller.setApplicationHiddenSequentially(selected, hidden)
            results.filter(PackageOperationResult::success).forEach { updateHiddenState(it.packageName, hidden) }
            _state.value = _state.value.copy(
                operationInProgress = false,
                selectedPackages = emptySet(),
                message = resultsMessage(results, hidden),
            )
        }
    }

    fun visibleApps(): List<ManagedApp> = WorkAppsSelection.filter(_state.value.apps, _state.value.query)

    private fun updateHiddenState(packageName: PackageName, hidden: Boolean) {
        _state.value = _state.value.copy(
            apps = _state.value.apps.map { app ->
                if (app.packageName == packageName) app.copy(isHidden = hidden) else app
            },
        )
    }

    private fun resultsMessage(results: List<PackageOperationResult>, hidden: Boolean): String {
        val successCount = results.count(PackageOperationResult::success)
        val failures = results.filterNot(PackageOperationResult::success)
        val action = if (hidden) "Frozen" else "Unfrozen"
        return if (failures.isEmpty()) {
            "$action $successCount apps"
        } else {
            "$action $successCount; failed ${failures.size}: " +
                failures.joinToString { "${it.packageName.value} — ${it.reason ?: "operation failed"}" }
        }
    }

    class Factory(
        private val catalog: AppCatalogRepository,
        private val controller: WorkProfileController,
        private val ownPackage: String,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(WorkAppsViewModel::class.java))
            return WorkAppsViewModel(catalog, controller, ownPackage) as T
        }
    }
}
