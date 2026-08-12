package com.monstera.harbor.ui

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import com.monstera.harbor.ui.designsystem.HarborAppIconSize
import com.monstera.harbor.ui.designsystem.HarborEmptyState
import com.monstera.harbor.ui.designsystem.HarborSectionTitle
import com.monstera.harbor.ui.designsystem.HarborSettingsRow
import com.monstera.harbor.ui.designsystem.HarborSpacing
import com.monstera.harbor.ui.designsystem.HarborStatusPill
import com.monstera.harbor.ui.privacy.appStatusLabel
import com.monstera.harbor.ui.privacy.appStatusTone
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
    var selectedApp by remember { mutableStateOf<ManagedApp?>(null) }
    var showControls by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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

    if (selectedApp != null) {
        AppActionSheet(
            app = selectedApp!!,
            sheetState = sheetState,
            busy = uiState.operationInProgress,
            onDismiss = { selectedApp = null },
            onLaunch = {
                if (!onLaunchPackage(selectedApp!!.packageName.value)) {
                    scope.launch { snackbar.showSnackbar("No launchable activity is available") }
                }
                selectedApp = null
            },
            onToggleHidden = {
                viewModel.setApplicationHidden(selectedApp!!.packageName, !selectedApp!!.isHidden)
                selectedApp = null
            },
            onDetails = {
                onOpenPackageDetails(selectedApp!!.packageName.value)
                selectedApp = null
            },
            onUninstall = {
                onUninstallPackage(selectedApp!!.packageName.value)
                selectedApp = null
            },
            onAddShortcut = {
                val packageName = selectedApp!!.packageName.value
                scope.launch {
                    if (!onAddShortcut(packageName)) snackbar.showSnackbar("This launcher cannot pin Harbor shortcuts")
                }
                selectedApp = null
            },
        )
    }

    if (showControls) {
        WorkControlsSheet(
            sheetState = sheetState,
            sharingBusy = sharingBusy,
            sharingRequested = sharingRequested,
            onDismiss = { showControls = false },
            onOpenPersonalHarbor = {
                if (!onOpenPersonalHarbor()) scope.launch { snackbar.showSnackbar("Open Personal Harbor from the personal profile") }
                showControls = false
            },
            onEnableSharing = {
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
            onAllowApkInstalls = {
                scope.launch {
                    when (val result = controller.allowApkInstalls()) {
                        is PolicyResult.Success -> snackbar.showSnackbar("APK installs are allowed; retry the APK")
                        is PolicyResult.Failure -> snackbar.showSnackbar(result.reason)
                    }
                }
                showControls = false
            },
            onOpenSettings = {
                onOpenSystemSettings()
                showControls = false
            },
        )
    }

    Scaffold(
        topBar = {
            if (selectionMode) {
                TopAppBar(
                    title = { Text("${uiState.selectedPackages.size} selected") },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                selectionMode = false
                                viewModel.clearSelection()
                            },
                            modifier = Modifier.semantics {
                                contentDescription = "Exit app selection"
                            },
                        ) {
                            Text("×", style = MaterialTheme.typography.headlineSmall)
                        }
                    },
                    actions = {
                        TextButton(onClick = viewModel::selectAllVisible) { Text("All") }
                        TextButton(
                            enabled = !uiState.operationInProgress && uiState.selectedPackages.isNotEmpty(),
                            onClick = { viewModel.setSelectedHidden(true) },
                        ) { Text("Freeze") }
                        TextButton(
                            enabled = !uiState.operationInProgress && uiState.selectedPackages.isNotEmpty(),
                            onClick = { viewModel.setSelectedHidden(false) },
                        ) { Text("Unfreeze") }
                    },
                )
            } else {
                TopAppBar(
                    title = { Text("Work space") },
                    actions = {
                        IconButton(
                            onClick = { showControls = true },
                            modifier = Modifier.semantics {
                                contentDescription = "Open work profile settings"
                            },
                        ) {
                            Text("⋮", style = MaterialTheme.typography.headlineSmall)
                        }
                        TextButton(onClick = onAdvanced) { Text("Advanced") }
                    },
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = HarborSpacing.screen, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(HarborSpacing.compact),
        ) {
            item {
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = viewModel::setQuery,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search apps") },
                    singleLine = true,
                )
            }
            item {
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    HarborSectionTitle(
                        title = "${uiState.apps.size} apps",
                        supportingText = if (uiState.query.isBlank()) "Installed in this Work space" else "Matching apps",
                    )
                    if (!selectionMode) TextButton(onClick = { selectionMode = true }) { Text("Select") }
                }
            }
            if (visibleApps.isEmpty()) {
                item {
                    HarborEmptyState(
                        title = if (uiState.query.isBlank()) "No apps in Work yet" else "No matching apps",
                        body = if (uiState.query.isBlank()) {
                            "Install an app inside the Work space, then return here to manage it."
                        } else "Try a different app name or package name.",
                    )
                }
            } else {
                items(visibleApps, key = { it.packageName.value }) { app ->
                    ManagedAppRow(
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
                        onOpenActions = { selectedApp = app },
                    )
                }
            }
        }
    }
}

@Composable
private fun ManagedAppRow(
    app: ManagedApp,
    iconProvider: AppIconProvider,
    busy: Boolean,
    selected: Boolean,
    selectionMode: Boolean,
    onToggleSelected: () -> Unit,
    onLongPress: () -> Unit,
    onOpenActions: () -> Unit,
) {
    Card(
        Modifier
            .fillMaxWidth()
            .combinedClickable(
                enabled = !busy,
                onClick = { if (selectionMode && !app.isSystem) onToggleSelected() else onOpenActions() },
                onLongClick = onLongPress,
            ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HarborAppIcon(
                provider = iconProvider,
                packageName = app.packageName,
                modifier = Modifier.size(HarborAppIconSize),
                contentDescription = app.label,
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(app.label, style = MaterialTheme.typography.bodyLarge)
                HarborStatusPill(appStatusLabel(app), appStatusTone(app))
            }
            if (selectionMode) {
                Checkbox(
                    checked = selected,
                    enabled = !app.isSystem && !busy,
                    onCheckedChange = { onToggleSelected() },
                    modifier = Modifier.semantics { contentDescription = "Select ${app.label}" },
                )
            } else {
                Text("⋮", style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppActionSheet(
    app: ManagedApp,
    sheetState: SheetState,
    busy: Boolean,
    onDismiss: () -> Unit,
    onLaunch: () -> Unit,
    onToggleHidden: () -> Unit,
    onDetails: () -> Unit,
    onUninstall: () -> Unit,
    onAddShortcut: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier.padding(horizontal = HarborSpacing.screen, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(app.label, style = MaterialTheme.typography.headlineSmall)
            Text(app.packageName.value, style = MaterialTheme.typography.bodySmall)
            if (app.isLaunchable && !app.isHidden) {
                HarborSettingsRow("Open", "Launch this app in Work", onLaunch, !busy)
            }
            if (!app.isSystem) {
                HarborSettingsRow(
                    if (app.isHidden) "Unfreeze" else "Freeze",
                    if (app.isHidden) "Make this app available again" else "Hide the app without uninstalling it",
                    onToggleHidden,
                    !busy,
                )
            }
            if (app.isLaunchable) {
                HarborSettingsRow(
                    if (app.isHidden) "Add to launcher" else "Add to launcher",
                    "Create a shortcut that can restore this app",
                    onAddShortcut,
                    !busy,
                )
            }
            HarborSettingsRow("App details", "Open Android's system app details", onDetails, !busy)
            if (!app.isSystem) HarborSettingsRow("Uninstall", "Remove this app and its Work data", onUninstall, !busy)
            HarborSectionTitle(
                title = "Status",
                supportingText = "${appStatusLabel(app)} · actions are confirmed by Android where required",
                modifier = Modifier.padding(top = 8.dp, bottom = 20.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkControlsSheet(
    sheetState: SheetState,
    sharingBusy: Boolean,
    sharingRequested: Boolean,
    onDismiss: () -> Unit,
    onOpenPersonalHarbor: () -> Unit,
    onEnableSharing: () -> Unit,
    onAllowApkInstalls: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier.padding(horizontal = HarborSpacing.screen, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Work settings", style = MaterialTheme.typography.headlineSmall)
            Text("Secondary controls stay here so app management remains the main task.", style = MaterialTheme.typography.bodyMedium)
            HarborSettingsRow("Move files to Work", "Open Personal Harbor to choose files", onOpenPersonalHarbor)
            HarborSettingsRow(
                "File sharing",
                if (sharingRequested) "Enabled for Android Share targets" else "Enable Harbor as a Work Share target",
                onEnableSharing,
                !sharingBusy,
            )
            HarborSettingsRow("Install APKs", "Allow Android to ask the source app for consent", onAllowApkInstalls)
            HarborSettingsRow("Android Work settings", "Open the system Settings app", onOpenSettings)
            Text(
                "Android may also offer shared files to other compatible Work apps. Harbor copies files and never deletes the personal original.",
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
