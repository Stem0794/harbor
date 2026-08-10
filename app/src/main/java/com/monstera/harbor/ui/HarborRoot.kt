package com.monstera.harbor.ui

import android.app.admin.DevicePolicyManager
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
import com.monstera.harbor.HarborGraph
import com.monstera.harbor.core.policy.WorkProfileStatus
import com.monstera.harbor.feature.advanced.AdvancedScreen
import kotlinx.coroutines.launch

private enum class HarborDestination { HOME, ADVANCED }

@Composable
fun HarborRoot(
    graph: HarborGraph,
    onProvision: () -> Unit,
    onOpenWorkHarbor: () -> Boolean,
    onOpenWorkSettings: () -> Unit,
    onLaunchPackage: (String) -> Boolean,
    onOpenPackageDetails: (String) -> Unit,
    onUninstallPackage: (String) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var topology by remember { mutableStateOf(graph.topologyDetector.detect()) }
    val profileState by graph.policyController.observeState().collectAsStateWithLifecycle(
        initialValue = com.monstera.harbor.core.policy.WorkProfileState(
            WorkProfileStatus.NOT_PROFILE_OWNER,
        ),
    )
    val advancedEnabled by graph.preferences.advancedToolsEnabled.collectAsStateWithLifecycle(false)
    var destination by remember { mutableStateOf(HarborDestination.HOME) }
    var showAdvancedConfirmation by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) topology = graph.topologyDetector.detect()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
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
            onBack = { destination = HarborDestination.HOME },
        )
        return
    }

    if (profileState.status == WorkProfileStatus.ACTIVE && topology.harborIsProfileOwner) {
        WorkProfileScreen(
            catalog = graph.appCatalog,
            controller = graph.policyController,
            ownPackage = context.packageName,
            onAdvanced = ::openAdvanced,
            onOpenWorkSettings = onOpenWorkSettings,
            onLaunchPackage = onLaunchPackage,
            onOpenPackageDetails = onOpenPackageDetails,
            onUninstallPackage = onUninstallPackage,
        )
    } else {
        val policyManager = context.getSystemService(DevicePolicyManager::class.java)
        val provisioningAllowed = policyManager.isProvisioningAllowed(
            DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE,
        )
        PersonalProfileScreen(
            topology = topology,
            provisioningAllowed = provisioningAllowed,
            message = message,
            onProvision = onProvision,
            onOpenWorkHarbor = {
                message = if (onOpenWorkHarbor()) null else "Open the work-badged Harbor icon from your launcher."
            },
            onAdvanced = ::openAdvanced,
        )
    }
}
