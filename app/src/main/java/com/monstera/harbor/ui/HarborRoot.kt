package com.monstera.harbor.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.monstera.harbor.HarborGraph
import com.monstera.harbor.core.policy.WorkProfileStatus
import com.monstera.harbor.core.policy.ManagedProfileProvisioningCapability
import com.monstera.harbor.core.policy.ManagedProfileProvisioningStartResult
import com.monstera.harbor.core.topology.HarborPrivilegeResolver
import com.monstera.harbor.core.topology.PrivilegedAvailability
import com.monstera.harbor.core.topology.PrivilegedBackendState
import com.monstera.harbor.feature.advanced.AdvancedScreen
import com.monstera.harbor.core.topology.UserVisibleName
import kotlinx.coroutines.launch

private enum class HarborDestination { HOME, ADVANCED }

@Composable
fun HarborRoot(
    graph: HarborGraph,
    onProvision: () -> ManagedProfileProvisioningStartResult,
    onOpenWorkHarbor: () -> Boolean,
    onOpenSystemSettings: () -> Unit,
    onLaunchPackage: (String) -> Boolean,
    onOpenPackageDetails: (String) -> Unit,
    onUninstallPackage: (String) -> Unit,
    onAddShortcut: suspend (String) -> Boolean,
    onOpenPersonalHarbor: () -> Boolean,
    onSendFilesToWork: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var topology by remember { mutableStateOf(graph.topologyDetector.detect()) }
    var provisioningCapability by remember {
        mutableStateOf(graph.provisioningPolicy.capability())
    }
    val profileState by graph.policyController.observeState().collectAsStateWithLifecycle(
        initialValue = com.monstera.harbor.core.policy.WorkProfileState(
            WorkProfileStatus.NOT_PROFILE_OWNER,
        ),
    )
    val advancedEnabled by graph.preferences.advancedToolsEnabled.collectAsStateWithLifecycle(false)
    val privilegedState by graph.privilegedBackend.observeState().collectAsStateWithLifecycle(
        initialValue = PrivilegedBackendState(PrivilegedAvailability.BINDER_UNAVAILABLE),
    )
    val privilegeState = HarborPrivilegeResolver.resolve(advancedEnabled, privilegedState)
    val workspaceViewModel: WorkspaceViewModel = viewModel(
        factory = WorkspaceViewModel.Factory(
            graph.privilegedBackend,
            graph.privilegedBackend,
            graph.workspaceMetadataStore,
        ),
    )
    val workspaceState by workspaceViewModel.state.collectAsStateWithLifecycle()
    var destination by remember { mutableStateOf(HarborDestination.HOME) }
    var showAdvancedConfirmation by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                topology = graph.topologyDetector.detect()
                provisioningCapability = graph.provisioningPolicy.capability()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(advancedEnabled, privilegedState.availability) {
        if (advancedEnabled && privilegedState.availability == PrivilegedAvailability.READY) {
            workspaceViewModel.refresh()
        } else {
            workspaceViewModel.clear()
        }
    }

    fun openAdvanced() {
        if (advancedEnabled) destination = HarborDestination.ADVANCED
        else showAdvancedConfirmation = true
    }

    if (showAdvancedConfirmation) {
        AlertDialog(
            onDismissRequest = { showAdvancedConfirmation = false },
            title = { Text("Enable Advanced tools?") },
            text = {
                Text("Advanced tools use an external Shizuku service with ADB or root identity. Harbor restricts operations, but Android and OEM behavior can vary.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showAdvancedConfirmation = false
                    scope.launch {
                        graph.preferences.setAdvancedToolsEnabled(true)
                        destination = HarborDestination.ADVANCED
                    }
                }) { Text("Enable") }
            },
            dismissButton = { TextButton(onClick = { showAdvancedConfirmation = false }) { Text("Cancel") } },
        )
    }

    if (destination == HarborDestination.ADVANCED) {
        AdvancedScreen(
            backend = graph.privilegedBackend,
            multiUserController = graph.privilegedBackend,
            topology = topology,
            privilegeState = HarborPrivilegeResolver.resolve(true, privilegedState),
            iconProvider = graph.iconProvider,
            packageMetadataProvider = graph.packageMetadataProvider,
            onBack = { destination = HarborDestination.HOME },
            onDisable = {
                scope.launch {
                    graph.preferences.setAdvancedToolsEnabled(false)
                    graph.privilegedBackend.release()
                    destination = HarborDestination.HOME
                }
            },
        )
        return
    }

    if (profileState.status == WorkProfileStatus.ACTIVE && topology.harborIsProfileOwner) {
        WorkProfileScreen(
            catalog = graph.appCatalog,
            controller = graph.policyController,
            ownPackage = context.packageName,
            privilegeState = privilegeState,
            iconProvider = graph.iconProvider,
            onAdvanced = ::openAdvanced,
            onOpenSystemSettings = onOpenSystemSettings,
            onLaunchPackage = onLaunchPackage,
            onOpenPackageDetails = onOpenPackageDetails,
            onUninstallPackage = onUninstallPackage,
            onAddShortcut = onAddShortcut,
            onOpenPersonalHarbor = onOpenPersonalHarbor,
        )
    } else {
        PersonalProfileScreen(
            topology = topology,
            privilegeState = privilegeState,
            workspaceUsers = workspaceState.users,
            workspaceCurrentUserId = workspaceState.currentUserId,
            workspaceMetadata = workspaceState.metadata,
            workspaceBusy = workspaceState.loading,
            workspaceMessage = workspaceState.message,
            onRefreshWorkspaces = workspaceViewModel::refresh,
            onSwitchWorkspace = workspaceViewModel::switchUser,
            onInstallWorkspace = workspaceViewModel::installHarbor,
            onCreateWorkspace = { name ->
                runCatching { UserVisibleName(name) }
                    .onSuccess(workspaceViewModel::createFullUser)
                    .onFailure { workspaceViewModel.setMessage(it.message ?: "Invalid workspace name") }
            },
            onRenameWorkspace = workspaceViewModel::rename,
            onChangeWorkspaceIcon = workspaceViewModel::setIcon,
            provisioningCapability = provisioningCapability,
            message = message,
            onProvision = {
                when (val result = onProvision()) {
                    ManagedProfileProvisioningStartResult.Started -> message = null
                    is ManagedProfileProvisioningStartResult.Blocked -> {
                        provisioningCapability = result.capability
                        message = when (result.capability.reason) {
                            com.monstera.harbor.core.policy.ManagedProfileProvisioningBlockReason.ANDROID_MANAGEMENT_STATE ->
                                "Android's current device-management state prevents Harbor from creating its normal Work profile."
                            com.monstera.harbor.core.policy.ManagedProfileProvisioningBlockReason.CAPABILITY_CHECK_FAILED ->
                                "Harbor could not verify that Android currently permits Work profile setup."
                        }
                    }
                }
            },
            onOpenWorkHarbor = {
                message = if (onOpenWorkHarbor()) null else "Open the work-badged Harbor icon from your launcher."
            },
            onSendFilesToWork = onSendFilesToWork,
            onAdvanced = ::openAdvanced,
        )
    }
}
