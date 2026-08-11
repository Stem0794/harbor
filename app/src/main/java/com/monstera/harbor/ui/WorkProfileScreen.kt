package com.monstera.harbor.ui

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.monstera.harbor.core.data.AppCatalogRepository
import com.monstera.harbor.core.data.AppIconProvider
import com.monstera.harbor.core.data.ManagedApp
import com.monstera.harbor.core.policy.PolicyResult
import com.monstera.harbor.core.policy.WorkProfileController
import com.monstera.harbor.core.topology.HarborPrivilegeState
import com.monstera.harbor.feature.advanced.HarborAppIcon
import com.monstera.harbor.feature.advanced.PrivilegeBadge
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkProfileScreen(
    catalog: AppCatalogRepository,
    controller: WorkProfileController,
    ownPackage: String,
    privilegeState: HarborPrivilegeState,
    iconProvider: AppIconProvider,
    onAdvanced: () -> Unit,
    onOpenSystemSettings: () -> Unit,
    onLaunchPackage: (String) -> Boolean,
    onOpenPackageDetails: (String) -> Unit,
    onUninstallPackage: (String) -> Unit,
    onAddShortcut: suspend (String) -> Boolean,
    onOpenPersonalHarbor: () -> Boolean,
) {
    val viewModel: WorkAppsViewModel = viewModel(
        factory = WorkAppsViewModel.Factory(catalog, controller, ownPackage),
    )
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var selectionMode by remember { mutableStateOf(false) }
    var sharingBusy by remember { mutableStateOf(false) }
    var sharingRequested by remember { mutableStateOf(false) }

    val visibleApps = remember(uiState.apps, uiState.query) {
        WorkAppsSelection.filter(uiState.apps, uiState.query)
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
    }
    LaunchedEffect(uiState.operationInProgress, uiState.selectedPackages) {
        if (!uiState.operationInProgress && uiState.selectedPackages.isEmpty()) selectionMode = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Work profile") },
                actions = { TextButton(onClick = onAdvanced) { Text("Advanced") } },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { PrivilegeBadge(privilegeState) }

            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("Work apps", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${uiState.apps.size} available in this profile",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            TextButton(onClick = onOpenSystemSettings) { Text("Settings") }
                        }
                        OutlinedTextField(
                            value = uiState.query,
                            onValueChange = viewModel::setQuery,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Search apps") },
                            singleLine = true,
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = {
                                selectionMode = !selectionMode
                                viewModel.clearSelection()
                            }) { Text(if (selectionMode) "Done" else "Select apps") }
                        }
                    }
                }
            }

            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Move files to Work", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Choose files in Personal Harbor, or share from a personal app to Harbor's briefcase target.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Column(
                            Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                if (!onOpenPersonalHarbor()) {
                                    scope.launch {
                                        snackbar.showSnackbar("Open Personal Harbor from the personal profile")
                                    }
                                }
                            },
                            ) { Text("Open Personal Harbor") }
                            OutlinedButton(
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !sharingBusy,
                                onClick = {
                                    sharingBusy = true
                                    scope.launch {
                                        when (val result = controller.allowPersonalFileSharing()) {
                                            is PolicyResult.Success -> {
                                                sharingRequested = true
                                                snackbar.showSnackbar("Sharing to Harbor is enabled")
                                            }
                                            is PolicyResult.Failure -> snackbar.showSnackbar(result.reason)
                                        }
                                        sharingBusy = false
                                    }
                                },
                            ) { Text(if (sharingBusy) "Enabling…" else "Enable sharing") }
                        }
                        if (sharingRequested) {
                            Text(
                                "Use Share → Harbor with the briefcase badge. The Android “Move to work” command may still be blocked by the device manufacturer.",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }

            item {
                Card(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Install APKs", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Android will ask each source app for permission when you open an APK.",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        OutlinedButton(onClick = {
                            scope.launch {
                                when (val result = controller.allowApkInstalls()) {
                                    is PolicyResult.Success -> snackbar.showSnackbar(
                                        "APK installs are allowed; retry the APK.",
                                    )
                                    is PolicyResult.Failure -> snackbar.showSnackbar(result.reason)
                                }
                            }
                        }) { Text("Allow") }
                    }
                }
            }

            if (selectionMode) {
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            TextButton(onClick = viewModel::selectAllVisible) { Text("Select all") }
                            Text(
                                "${uiState.selectedPackages.size} selected",
                                modifier = Modifier.weight(1f).padding(top = 12.dp),
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Button(
                                enabled = !uiState.operationInProgress && uiState.selectedPackages.isNotEmpty(),
                                onClick = { viewModel.setSelectedHidden(true) },
                            ) { Text("Freeze") }
                            OutlinedButton(
                                enabled = !uiState.operationInProgress && uiState.selectedPackages.isNotEmpty(),
                                onClick = { viewModel.setSelectedHidden(false) },
                            ) { Text("Unfreeze") }
                        }
                    }
                }
            }

            item {
                Text(
                    if (uiState.query.isBlank()) "Apps in this work profile" else "Matching apps",
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            if (visibleApps.isEmpty()) {
                item {
                    Text(
                        if (uiState.query.isBlank()) {
                            "No user-facing apps are installed in this work profile yet."
                        } else {
                            "No matching apps"
                        },
                        modifier = Modifier.padding(horizontal = 4.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            } else {
                items(visibleApps, key = { it.packageName.value }) { app ->
                    ManagedAppCard(
                        app = app,
                        iconProvider = iconProvider,
                        busy = uiState.operationInProgress,
                        selected = app.packageName in uiState.selectedPackages,
                        selectionMode = selectionMode,
                        onToggleSelected = { viewModel.toggleSelection(app.packageName) },
                        onLongPress = {
                            if (!app.isSystem) {
                                selectionMode = true
                                viewModel.toggleSelection(app.packageName)
                            }
                        },
                        onToggleHidden = {
                            viewModel.setApplicationHidden(app.packageName, !app.isHidden)
                        },
                        onLaunch = {
                            if (!onLaunchPackage(app.packageName.value)) {
                                scope.launch { snackbar.showSnackbar("No launchable activity is available") }
                            }
                        },
                        onDetails = { onOpenPackageDetails(app.packageName.value) },
                        onUninstall = { onUninstallPackage(app.packageName.value) },
                        onAddShortcut = {
                            scope.launch {
                                if (!onAddShortcut(app.packageName.value)) {
                                    snackbar.showSnackbar("This launcher cannot pin Harbor shortcuts")
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ManagedAppCard(
    app: ManagedApp,
    iconProvider: AppIconProvider,
    busy: Boolean,
    selected: Boolean,
    selectionMode: Boolean,
    onToggleSelected: () -> Unit,
    onLongPress: () -> Unit,
    onToggleHidden: () -> Unit,
    onLaunch: () -> Unit,
    onDetails: () -> Unit,
    onUninstall: () -> Unit,
    onAddShortcut: () -> Unit,
) {
    Card(
        Modifier
            .fillMaxWidth()
            .combinedClickable(
                enabled = !busy,
                onClick = { if (selectionMode && !app.isSystem) onToggleSelected() },
                onLongClick = onLongPress,
            ),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HarborAppIcon(iconProvider, app.packageName, contentDescription = app.label)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(app.label, style = MaterialTheme.typography.titleMedium)
                    Text(
                        when {
                            app.isHidden -> "Frozen"
                            app.isSystem -> "System app · read-only"
                            !app.isEnabled -> "Disabled"
                            else -> "Available"
                        },
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (selectionMode) {
                    Checkbox(
                        checked = selected,
                        enabled = !app.isSystem && !busy,
                        onCheckedChange = { onToggleSelected() },
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = !busy && !app.isSystem,
                    onClick = onToggleHidden,
                ) { Text(if (app.isHidden) "Unfreeze" else "Freeze") }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    enabled = app.isLaunchable && !app.isHidden,
                    onClick = onLaunch,
                ) { Text("Open") }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                TextButton(onClick = onDetails) { Text("Details") }
                if (app.isLaunchable) {
                    TextButton(onClick = onAddShortcut) {
                        Text(if (app.isHidden) "Shortcut + unfreeze" else "Add shortcut")
                    }
                }
                if (!app.isSystem) {
                    TextButton(onClick = onUninstall) { Text("Uninstall") }
                }
            }
        }
    }
}
