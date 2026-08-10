package com.monstera.harbor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import com.monstera.harbor.core.data.AppCatalogRepository
import com.monstera.harbor.core.data.ManagedApp
import com.monstera.harbor.core.policy.PolicyResult
import com.monstera.harbor.core.policy.WorkProfileController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkProfileScreen(
    catalog: AppCatalogRepository,
    controller: WorkProfileController,
    ownPackage: String,
    onAdvanced: () -> Unit,
    onOpenSystemSettings: () -> Unit,
    onLaunchPackage: (String) -> Boolean,
    onOpenPackageDetails: (String) -> Unit,
    onUninstallPackage: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var apps by remember { mutableStateOf<List<ManagedApp>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var busyPackage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(catalog) {
        catalog.observeApps().collectLatest { apps = it.filterNot { app -> app.packageName.value == ownPackage } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Harbor work profile") },
                actions = { TextButton(onClick = onAdvanced) { Text("Advanced") } },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                label = { Text("Search work apps") },
                singleLine = true,
            )
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("${apps.size} applications", style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = onOpenSystemSettings) { Text("System settings") }
            }
            val visibleApps = remember(apps, query) {
                apps.filter {
                    query.isBlank() || it.label.contains(query, true) || it.packageName.value.contains(query, true)
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(visibleApps, key = { it.packageName.value }) { app ->
                    ManagedAppCard(
                        app = app,
                        busy = busyPackage == app.packageName.value,
                        onToggleHidden = {
                            scope.launch {
                                val requestedHidden = !app.isHidden
                                busyPackage = app.packageName.value
                                when (val result = controller.setApplicationHidden(app.packageName, requestedHidden)) {
                                    is PolicyResult.Success -> apps = apps.map { current ->
                                        if (current.packageName == app.packageName) {
                                            current.copy(isHidden = requestedHidden)
                                        } else {
                                            current
                                        }
                                    }
                                    is PolicyResult.Failure -> snackbar.showSnackbar(result.reason)
                                }
                                busyPackage = null
                            }
                        },
                        onLaunch = {
                            if (!onLaunchPackage(app.packageName.value)) {
                                scope.launch { snackbar.showSnackbar("No launchable activity is available") }
                            }
                        },
                        onDetails = { onOpenPackageDetails(app.packageName.value) },
                        onUninstall = { onUninstallPackage(app.packageName.value) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ManagedAppCard(
    app: ManagedApp,
    busy: Boolean,
    onToggleHidden: () -> Unit,
    onLaunch: () -> Unit,
    onDetails: () -> Unit,
    onUninstall: () -> Unit,
) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(app.label, style = MaterialTheme.typography.titleMedium)
            Text(app.packageName.value, style = MaterialTheme.typography.bodySmall)
            Text(
                when {
                    app.isHidden -> "Frozen"
                    app.isSystem -> "System app"
                    !app.isEnabled -> "Disabled"
                    else -> "Available"
                },
                color = MaterialTheme.colorScheme.primary,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    enabled = !busy && !app.isSystem,
                    onClick = onToggleHidden,
                ) { Text(if (app.isHidden) "Unfreeze" else "Freeze") }
                OutlinedButton(enabled = app.isLaunchable && !app.isHidden, onClick = onLaunch) { Text("Open") }
                OutlinedButton(onClick = onDetails) { Text("Details") }
            }
            if (!app.isSystem) {
                TextButton(onClick = onUninstall) { Text("Uninstall") }
            }
        }
    }
}
