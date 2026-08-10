@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package io.github.theodorekonikowski.harbor.feature.advanced

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.theodorekonikowski.harbor.core.topology.AndroidUserId
import io.github.theodorekonikowski.harbor.core.topology.LocalProfileKind
import io.github.theodorekonikowski.harbor.core.topology.MultiUserController
import io.github.theodorekonikowski.harbor.core.topology.PackageName
import io.github.theodorekonikowski.harbor.core.topology.PrivilegedAvailability
import io.github.theodorekonikowski.harbor.core.topology.PrivilegedBackend
import io.github.theodorekonikowski.harbor.core.topology.PrivilegedResult
import io.github.theodorekonikowski.harbor.core.topology.ProfileTopology
import io.github.theodorekonikowski.harbor.core.topology.SystemDiagnostics
import io.github.theodorekonikowski.harbor.core.topology.SystemUser
import io.github.theodorekonikowski.harbor.core.topology.UserVisibleName
import kotlinx.coroutines.launch

@Composable
fun AdvancedScreen(
    backend: PrivilegedBackend,
    multiUserController: MultiUserController,
    topology: ProfileTopology,
    onBack: () -> Unit,
) {
    val backendState by backend.observeState().collectAsState(
        initial = io.github.theodorekonikowski.harbor.core.topology.PrivilegedBackendState(
            PrivilegedAvailability.BINDER_UNAVAILABLE,
        ),
    )
    val scope = rememberCoroutineScope()
    var diagnostics by remember { mutableStateOf<SystemDiagnostics?>(null) }
    var status by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    DisposableEffect(backend) {
        onDispose { backend.release() }
    }

    fun runOperation(operation: suspend () -> PrivilegedResult<*>) {
        scope.launch {
            busy = true
            status = when (val result = operation()) {
                is PrivilegedResult.Success -> "Operation completed"
                is PrivilegedResult.Failure -> result.reason
            }
            busy = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Advanced tools") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Shizuku", style = MaterialTheme.typography.titleLarge)
                        Text(backendState.availability.userLabel())
                        backendState.serverVersion?.let { Text("Server API: $it") }
                        backendState.effectiveUid?.let {
                            Text("Effective UID: $it (${if (it == 0) "root" else "ADB shell"})")
                        }
                        backendState.detail?.let { Text(it) }
                        if (backendState.availability == PrivilegedAvailability.PERMISSION_REQUIRED) {
                            Button(
                                enabled = !busy,
                                onClick = { runOperation { backend.requestPermission() } },
                            ) { Text("Grant permission") }
                        }
                    }
                }
            }

            status?.let { message -> item { Text(message, color = MaterialTheme.colorScheme.primary) } }

            if (backendState.availability == PrivilegedAvailability.READY) {
                item {
                    Button(
                        enabled = !busy,
                        onClick = {
                            scope.launch {
                                busy = true
                                when (val result = backend.diagnostics()) {
                                    is PrivilegedResult.Success -> {
                                        diagnostics = result.value
                                        status = "Diagnostics refreshed"
                                    }
                                    is PrivilegedResult.Failure -> status = result.reason
                                }
                                busy = false
                            }
                        },
                    ) { Text("Refresh diagnostics") }
                }

                diagnostics?.let { value -> item { DiagnosticsCard(value) } }

                if (topology.localKind == LocalProfileKind.MANAGED_PROFILE && topology.harborIsProfileOwner) {
                    item {
                        ClonePanel(
                            backend = backend,
                            topology = topology,
                            busy = busy,
                            onBusy = { busy = it },
                            onStatus = { status = it },
                        )
                    }
                } else {
                    item {
                        MultiUserPanel(
                            controller = multiUserController,
                            busy = busy,
                            onBusy = { busy = it },
                            onStatus = { status = it },
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun DiagnosticsCard(diagnostics: SystemDiagnostics) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Local diagnostics", style = MaterialTheme.typography.titleMedium)
            Text("UID: ${diagnostics.effectiveUid}")
            Text("Current user: ${diagnostics.currentUser?.value ?: "unknown"}")
            Text("Maximum users: ${diagnostics.maximumUsers ?: "unknown"}")
            Text("Users detected: ${diagnostics.users.size}")
            diagnostics.users.forEach { user ->
                Text("• ${user.id.value}: ${user.name}${if (user.isRunning) " (running)" else ""}")
            }
        }
    }
}

@Composable
private fun ClonePanel(
    backend: PrivilegedBackend,
    topology: ProfileTopology,
    busy: Boolean,
    onBusy: (Boolean) -> Unit,
    onStatus: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val sourceUser = topology.associatedProfiles.firstOrNull { !it.isCurrent }?.userId
    var packages by remember { mutableStateOf<List<PackageName>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<PackageName?>(null) }

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Clone an installed package", style = MaterialTheme.typography.titleMedium)
            Text("Harbor asks Android's package manager to install an existing package into this work profile. App data is never copied.")
            if (sourceUser == null) {
                Text("No associated parent profile is visible.")
            } else {
                OutlinedButton(
                    enabled = !busy,
                    onClick = {
                        scope.launch {
                            onBusy(true)
                            when (val result = backend.listPackages(sourceUser)) {
                                is PrivilegedResult.Success -> {
                                    packages = result.value
                                    onStatus("Found ${result.value.size} packages in user ${sourceUser.value}")
                                }
                                is PrivilegedResult.Failure -> onStatus(result.reason)
                            }
                            onBusy(false)
                        }
                    },
                ) { Text("Load parent packages") }

                if (packages.isNotEmpty()) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Filter package names") },
                    )
                    packages.asSequence()
                        .filter { query.isBlank() || it.value.contains(query, ignoreCase = true) }
                        .take(8)
                        .forEach { packageName ->
                            Text(
                                text = packageName.value,
                                modifier = Modifier.fillMaxWidth().clickable { selected = packageName }.padding(8.dp),
                                fontWeight = if (selected == packageName) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                    Button(
                        enabled = selected != null && !busy,
                        onClick = {
                            val packageName = selected ?: return@Button
                            scope.launch {
                                onBusy(true)
                                onStatus(
                                    when (val result = backend.installExisting(packageName, topology.currentUser)) {
                                        is PrivilegedResult.Success -> result.value.message.ifBlank { "Package installed" }
                                        is PrivilegedResult.Failure -> result.reason
                                    },
                                )
                                onBusy(false)
                            }
                        },
                    ) { Text("Clone selected package") }
                }
            }
        }
    }
}

@Composable
private fun MultiUserPanel(
    controller: MultiUserController,
    busy: Boolean,
    onBusy: (Boolean) -> Unit,
    onStatus: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var users by remember { mutableStateOf<List<SystemUser>>(emptyList()) }
    var userName by remember { mutableStateOf("Harbor Lab") }
    var confirmCreate by remember { mutableStateOf(false) }
    var createdUser by remember { mutableStateOf<SystemUser?>(null) }

    if (confirmCreate) {
        AlertDialog(
            onDismissRequest = { confirmCreate = false },
            title = { Text("Create a full Android user?") },
            text = { Text("This consumes device storage and requires user switching. Harbor will not delete users automatically.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmCreate = false
                    val name = runCatching { UserVisibleName(userName) }.getOrElse {
                        onStatus(it.message ?: "Invalid user name")
                        return@TextButton
                    }
                    scope.launch {
                        onBusy(true)
                        when (val result = controller.createFullUser(name)) {
                            is PrivilegedResult.Success -> {
                                createdUser = result.value
                                users = users + result.value
                                onStatus("Created user ${result.value.id.value}")
                            }
                            is PrivilegedResult.Failure -> onStatus(result.reason)
                        }
                        onBusy(false)
                    }
                }) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { confirmCreate = false }) { Text("Cancel") } },
        )
    }

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Multiple full-user workspaces", style = MaterialTheme.typography.titleMedium)
            Text("Experimental. Each full user may provision one standard Harbor work profile after switching into it.")
            OutlinedButton(
                enabled = !busy,
                onClick = {
                    scope.launch {
                        onBusy(true)
                        when (val result = controller.listUsers()) {
                            is PrivilegedResult.Success -> {
                                users = result.value
                                onStatus("Detected ${result.value.size} switchable full users")
                            }
                            is PrivilegedResult.Failure -> onStatus(result.reason)
                        }
                        onBusy(false)
                    }
                },
            ) { Text("List Android users") }
            users.forEach { user ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${user.id.value}: ${user.name}")
                    OutlinedButton(
                        enabled = !busy,
                        onClick = {
                            scope.launch {
                                onBusy(true)
                                onStatus(
                                    when (val result = controller.switchUser(user.id)) {
                                        is PrivilegedResult.Success -> "Switch requested"
                                        is PrivilegedResult.Failure -> result.reason
                                    },
                                )
                                onBusy(false)
                            }
                        },
                    ) { Text("Switch") }
                }
                HorizontalDivider()
            }
            OutlinedTextField(
                value = userName,
                onValueChange = { userName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("New full-user name") },
                supportingText = { Text("Creation is confirmed in a separate dialog") },
            )
            Button(enabled = !busy, onClick = { confirmCreate = true }) { Text("Create full user") }
            createdUser?.let { user ->
                OutlinedButton(
                    enabled = !busy,
                    onClick = {
                        scope.launch {
                            onBusy(true)
                            onStatus(
                                when (val result = controller.installHarbor(user.id)) {
                                    is PrivilegedResult.Success -> "Harbor installed for user ${user.id.value}"
                                    is PrivilegedResult.Failure -> result.reason
                                },
                            )
                            onBusy(false)
                        }
                    },
                ) { Text("Install Harbor for ${user.name}") }
            }
        }
    }
}

private fun PrivilegedAvailability.userLabel(): String = when (this) {
    PrivilegedAvailability.NOT_INSTALLED -> "Not installed. Core Harbor features remain available."
    PrivilegedAvailability.BINDER_UNAVAILABLE -> "Installed but not running."
    PrivilegedAvailability.PERMISSION_REQUIRED -> "Permission is required for Advanced tools."
    PrivilegedAvailability.PERMISSION_DENIED -> "Permission denied. Change it in Shizuku."
    PrivilegedAvailability.READY -> "Ready. Advanced operations are explicitly allowlisted."
    PrivilegedAvailability.UNSUPPORTED -> "This Shizuku version is unsupported."
}
