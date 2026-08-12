package com.monstera.harbor.ui

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.monstera.harbor.core.data.AppCatalogRepository
import com.monstera.harbor.core.data.AppIconProvider
import com.monstera.harbor.core.data.ManagedApp
import com.monstera.harbor.core.policy.PolicyResult
import com.monstera.harbor.core.policy.WorkProfileController
import com.monstera.harbor.core.topology.HarborPrivilegeState
import com.monstera.harbor.core.topology.PackageName
import com.monstera.harbor.feature.advanced.HarborAppIcon
import com.monstera.harbor.ui.designsystem.HarborColors
import com.monstera.harbor.ui.designsystem.HarborHeader
import com.monstera.harbor.ui.designsystem.HarborIcon
import com.monstera.harbor.ui.designsystem.HarborIconButton
import com.monstera.harbor.ui.designsystem.HarborIconKind
import com.monstera.harbor.ui.designsystem.HarborSearchField
import com.monstera.harbor.ui.designsystem.HarborShapes
import com.monstera.harbor.ui.designsystem.HarborSpacing
import com.monstera.harbor.ui.designsystem.HarborStatusPill
import com.monstera.harbor.ui.designsystem.StatusTone
import com.monstera.harbor.ui.privacy.WorkAppRowClickIntent
import com.monstera.harbor.ui.privacy.appStatusLabel
import com.monstera.harbor.ui.privacy.appStatusTone
import com.monstera.harbor.ui.privacy.launcherShortcutActionLabel
import com.monstera.harbor.ui.privacy.workAppCountLabel
import com.monstera.harbor.ui.privacy.workAppRowClickIntent
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
    val viewModel: WorkAppsViewModel = viewModel(factory = WorkAppsViewModel.Factory(catalog, controller, ownPackage))
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var selectionMode by remember { mutableStateOf(false) }
    var sharingBusy by remember { mutableStateOf(false) }
    var sharingRequested by remember { mutableStateOf(false) }
    var selectedPackage by remember { mutableStateOf<PackageName?>(null) }
    var showControls by remember { mutableStateOf(false) }
    var showNavigation by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val selectedApp = selectedPackage?.let { packageName -> uiState.apps.firstOrNull { it.packageName == packageName } }
    val visibleApps = remember(uiState.apps, uiState.query) { WorkAppsSelection.filter(uiState.apps, uiState.query) }

    LaunchedEffect(uiState.message) { uiState.message?.let { snackbar.showSnackbar(it); viewModel.clearMessage() } }
    LaunchedEffect(uiState.operationInProgress, uiState.selectedPackages) { if (!uiState.operationInProgress && uiState.selectedPackages.isEmpty()) selectionMode = false }

    selectedApp?.let { app ->
        AppActionSheet(
            app = app,
            iconProvider = iconProvider,
            sheetState = sheetState,
            busy = uiState.operationInProgress,
            onDismiss = { selectedPackage = null },
            onLaunch = {
                if (!onLaunchPackage(app.packageName.value)) scope.launch { snackbar.showSnackbar("No launchable activity is available") }
                selectedPackage = null
            },
            onToggleHidden = { viewModel.setApplicationHidden(app.packageName, !app.isHidden) },
            onDetails = { onOpenPackageDetails(app.packageName.value); selectedPackage = null },
            onUninstall = { onUninstallPackage(app.packageName.value); selectedPackage = null },
            onAddShortcut = {
                scope.launch { if (!onAddShortcut(app.packageName.value)) snackbar.showSnackbar("This launcher cannot pin Harbor shortcuts") }
                selectedPackage = null
            },
        )
    }
    if (showControls) {
        WorkControlsSheet(
            sheetState = sheetState,
            sharingBusy = sharingBusy,
            sharingRequested = sharingRequested,
            onDismiss = { showControls = false },
            onOpenPersonalHarbor = { if (!onOpenPersonalHarbor()) scope.launch { snackbar.showSnackbar("Open Personal Harbor from the personal profile") }; showControls = false },
            onEnableSharing = {
                sharingBusy = true
                scope.launch {
                    when (val result = controller.allowPersonalFileSharing()) {
                        is PolicyResult.Success -> { sharingRequested = true; snackbar.showSnackbar("Sharing to Harbor is enabled") }
                        is PolicyResult.Failure -> snackbar.showSnackbar(result.reason)
                    }
                    sharingBusy = false
                }
            },
            onAllowApkInstalls = {
                scope.launch { when (val result = controller.allowApkInstalls()) { is PolicyResult.Success -> snackbar.showSnackbar("APK installs are allowed; retry the APK"); is PolicyResult.Failure -> snackbar.showSnackbar(result.reason) } }
                showControls = false
            },
            onOpenSettings = { onOpenSystemSettings(); showControls = false },
        )
    }
    if (showNavigation) {
        WorkNavigationSheet(sheetState = sheetState, onDismiss = { showNavigation = false }, onOpenPersonalHarbor = { if (!onOpenPersonalHarbor()) scope.launch { snackbar.showSnackbar("Open Personal Harbor from the personal profile") }; showNavigation = false }, onAdvanced = { showNavigation = false; onAdvanced() })
    }

    Scaffold(containerColor = HarborColors.bgDeep, snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (selectionMode) {
                SelectionHeader(selectedCount = uiState.selectedPackages.size, onExit = { selectionMode = false; viewModel.clearSelection() }, onSelectAll = viewModel::selectAllVisible, onFreeze = { viewModel.setSelectedHidden(true) }, onUnfreeze = { viewModel.setSelectedHidden(false) }, busy = uiState.operationInProgress)
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = HarborSpacing.screen, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (!selectionMode) {
                    item {
                        HarborHeader(
                            title = "Work space",
                            subtitle = "Managed by Harbor",
                            work = true,
                            onMenu = { showNavigation = true },
                            onOverflow = { showControls = true },
                        )
                    }
                }
            item { HarborSearchField(value = uiState.query, onValueChange = viewModel::setQuery) }
            item {
                Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(workAppCountLabel(uiState.apps.size, visibleApps.size, uiState.query), color = HarborColors.textPrimary, style = androidx.compose.material3.MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Medium))
                        Text(if (uiState.query.isBlank()) "Installed in this Work space" else "Matching apps", color = HarborColors.textSecondary, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                    }
                    if (!selectionMode) TextButton(onClick = { selectionMode = true }) { Text("Select", color = HarborColors.accent) }
                }
            }
            if (visibleApps.isEmpty()) {
                item { EmptyWorkState(queryBlank = uiState.query.isBlank()) }
            } else {
                items(visibleApps, key = { it.packageName.value }) { app ->
                    Direction2ManagedAppRow(app = app, iconProvider = iconProvider, busy = uiState.operationInProgress, selected = app.packageName in uiState.selectedPackages, selectionMode = selectionMode, onToggleSelected = { viewModel.toggleSelection(app.packageName) }, onLongPress = { if (!app.isSystem) { selectionMode = true; viewModel.toggleSelection(app.packageName) } }, onOpenActions = { selectedPackage = app.packageName })
                }
            }
                item { Spacer(Modifier.height(12.dp)) }
            }
        }
    }
}

@Composable
private fun SelectionHeader(selectedCount: Int, onExit: () -> Unit, onSelectAll: () -> Unit, onFreeze: () -> Unit, onUnfreeze: () -> Unit, busy: Boolean) {
    Row(Modifier.fillMaxWidth().padding(horizontal = HarborSpacing.screen, vertical = 8.dp).height(64.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        HarborIconButton(HarborIconKind.Details, "Exit app selection", onExit, tint = HarborColors.textPrimary)
        Text("$selectedCount selected", color = HarborColors.textPrimary, style = androidx.compose.material3.MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        TextButton(enabled = !busy, onClick = onSelectAll) { Text("All", color = HarborColors.accent) }
        TextButton(enabled = !busy && selectedCount > 0, onClick = onFreeze) { Text("Freeze", color = HarborColors.accent) }
        TextButton(enabled = !busy && selectedCount > 0, onClick = onUnfreeze) { Text("Unfreeze", color = HarborColors.accent) }
    }
}

@Composable
private fun Direction2ManagedAppRow(app: ManagedApp, iconProvider: AppIconProvider, busy: Boolean, selected: Boolean, selectionMode: Boolean, onToggleSelected: () -> Unit, onLongPress: () -> Unit, onOpenActions: () -> Unit) {
    val intent = workAppRowClickIntent(selectionMode, app.isSystem)
    Surface(
        modifier = Modifier.fillMaxWidth().clip(HarborShapes.row).combinedClickable(enabled = !busy, onClick = { when (intent) { WorkAppRowClickIntent.ToggleSelection -> onToggleSelected(); WorkAppRowClickIntent.OpenActions -> onOpenActions(); WorkAppRowClickIntent.NoOp -> Unit } }, onLongClick = onLongPress),
        shape = HarborShapes.row,
        color = if (selected) HarborColors.surface else HarborColors.surfaceRaised,
    ) {
        Row(Modifier.fillMaxWidth().height(82.dp).padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            HarborAppIcon(provider = iconProvider, packageName = app.packageName, modifier = Modifier.size(48.dp), contentDescription = app.label)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(app.label, color = HarborColors.textPrimary, style = androidx.compose.material3.MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(app.packageName.value, color = HarborColors.textSecondary, style = androidx.compose.material3.MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            HarborStatusPill(appStatusLabel(app), appStatusTone(app))
            if (selectionMode) {
                androidx.compose.material3.Checkbox(checked = selected, enabled = !app.isSystem && !busy, onCheckedChange = { onToggleSelected() }, modifier = Modifier.semantics { contentDescription = "Select ${app.label}" })
            } else {
                HarborIcon(HarborIconKind.Overflow, Modifier.size(22.dp), HarborColors.textSecondary, "Actions for ${app.label}")
            }
        }
    }
}

@Composable
private fun EmptyWorkState(queryBlank: Boolean) {
    Surface(Modifier.fillMaxWidth(), shape = HarborShapes.card, color = HarborColors.surface, border = androidx.compose.foundation.BorderStroke(1.dp, HarborColors.stroke)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(if (queryBlank) "No apps in Work yet" else "No matching apps", color = HarborColors.textPrimary, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            Text(if (queryBlank) "Install an app inside the Work space, then return here to manage it." else "Try a different app name or package name.", color = HarborColors.textSecondary, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppActionSheet(app: ManagedApp, iconProvider: AppIconProvider, sheetState: SheetState, busy: Boolean, onDismiss: () -> Unit, onLaunch: () -> Unit, onToggleHidden: () -> Unit, onDetails: () -> Unit, onUninstall: () -> Unit, onAddShortcut: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = HarborColors.sheet, dragHandle = { Box(Modifier.padding(top = 10.dp).size(width = 44.dp, height = 4.dp).clip(RoundedCornerShape(50)).background(HarborColors.textSecondary)) }) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
            Row(Modifier.fillMaxWidth().padding(bottom = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                HarborAppIcon(provider = iconProvider, packageName = app.packageName, modifier = Modifier.size(52.dp), contentDescription = app.label)
                Column(Modifier.weight(1f)) { Text(app.label, color = HarborColors.textPrimary, style = androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)); Text(app.packageName.value, color = HarborColors.textSecondary, style = androidx.compose.material3.MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis) }
            }
            if (app.isLaunchable && !app.isHidden) Direction2ActionRow(HarborIconKind.Open, "Open", null, !busy, onLaunch)
            if (!app.isSystem) Direction2ActionRow(HarborIconKind.Freeze, if (app.isHidden) "Unfreeze" else "Freeze", if (app.isHidden) "Make the app available again" else "Prevent the app from running", !busy, onToggleHidden, trailing = { Switch(checked = app.isHidden, onCheckedChange = { onToggleHidden() }, enabled = !busy) })
            if (app.isLaunchable) Direction2ActionRow(HarborIconKind.Shortcut, launcherShortcutActionLabel(app.isHidden), null, !busy, onAddShortcut)
            Direction2ActionRow(HarborIconKind.Details, "App details", null, !busy, onDetails)
            if (!app.isSystem) Direction2ActionRow(HarborIconKind.Uninstall, "Uninstall", null, !busy, onUninstall, tint = HarborColors.danger)
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun Direction2ActionRow(icon: HarborIconKind, title: String, body: String?, enabled: Boolean, onClick: () -> Unit, tint: Color = HarborColors.textPrimary, trailing: (@Composable () -> Unit)? = null) {
    Surface(onClick = onClick, enabled = enabled, color = Color.Transparent, modifier = Modifier.fillMaxWidth().height(if (body == null) 58.dp else 66.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            HarborIcon(icon, Modifier.size(25.dp), if (title == "Uninstall") HarborColors.danger else HarborColors.textSecondary, title)
            Column(Modifier.weight(1f)) { Text(title, color = tint, style = androidx.compose.material3.MaterialTheme.typography.bodyLarge); body?.let { Text(it, color = HarborColors.textSecondary, style = androidx.compose.material3.MaterialTheme.typography.bodySmall) } }
            trailing?.invoke()
        }
    }
    HorizontalDivider(color = HarborColors.stroke)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkNavigationSheet(sheetState: SheetState, onDismiss: () -> Unit, onOpenPersonalHarbor: () -> Unit, onAdvanced: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = HarborColors.sheet, dragHandle = { Box(Modifier.padding(top = 10.dp).size(width = 44.dp, height = 4.dp).clip(RoundedCornerShape(50)).background(HarborColors.textSecondary)) }) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
            Text("Work space", color = HarborColors.textPrimary, style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
            Text("Managed by Harbor · Local catalog", color = HarborColors.textSecondary)
            Spacer(Modifier.height(10.dp))
            Direction2ActionRow(HarborIconKind.Home, "Open Personal Harbor", "Return to the Personal profile", true, onOpenPersonalHarbor)
            Direction2ActionRow(HarborIconKind.Shield, "Advanced", "Optional Shizuku developer tools", true, onAdvanced)
            Spacer(Modifier.height(18.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkControlsSheet(sheetState: SheetState, sharingBusy: Boolean, sharingRequested: Boolean, onDismiss: () -> Unit, onOpenPersonalHarbor: () -> Unit, onEnableSharing: () -> Unit, onAllowApkInstalls: () -> Unit, onOpenSettings: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = HarborColors.sheet, dragHandle = { Box(Modifier.padding(top = 10.dp).size(width = 44.dp, height = 4.dp).clip(RoundedCornerShape(50)).background(HarborColors.textSecondary)) }) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
            Text("Work controls", color = HarborColors.textPrimary, style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
            Text("Profile actions stay connected to Android policy", color = HarborColors.textSecondary)
            Spacer(Modifier.height(10.dp))
            Direction2ActionRow(HarborIconKind.Home, "Move files to Work", "Open Personal Harbor to choose files", true, onOpenPersonalHarbor)
            Direction2ActionRow(HarborIconKind.Send, "File sharing", if (sharingRequested) "Enabled for Android Share targets" else "Enable Harbor as a Work Share target", !sharingBusy, onEnableSharing)
            Direction2ActionRow(HarborIconKind.Open, "Install APKs", "Allow Android to ask the source app for consent", true, onAllowApkInstalls)
            Direction2ActionRow(HarborIconKind.Settings, "Android Work settings", "Open the system Settings app", true, onOpenSettings)
            Text("Android may also offer shared files to other compatible Work apps. Harbor copies files and never deletes the personal original.", color = HarborColors.textSecondary, style = androidx.compose.material3.MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 10.dp, bottom = 22.dp))
        }
    }
}
