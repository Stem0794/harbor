package com.monstera.harbor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.monstera.harbor.core.data.WorkspaceIconKey
import com.monstera.harbor.core.data.WorkspaceMetadata
import com.monstera.harbor.core.topology.AndroidUserId
import com.monstera.harbor.core.topology.HarborPrivilegeLevel
import com.monstera.harbor.core.topology.HarborPrivilegeState
import com.monstera.harbor.core.topology.ProfileOwnership
import com.monstera.harbor.core.topology.ProfileTopology
import com.monstera.harbor.core.topology.SystemUser
import com.monstera.harbor.feature.advanced.PrivilegeBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalProfileScreen(
    topology: ProfileTopology,
    privilegeState: HarborPrivilegeState,
    workspaceUsers: List<SystemUser>,
    workspaceCurrentUserId: AndroidUserId?,
    workspaceMetadata: List<WorkspaceMetadata>,
    workspaceBusy: Boolean,
    workspaceMessage: String?,
    onRefreshWorkspaces: () -> Unit,
    onSwitchWorkspace: (SystemUser) -> Unit,
    onInstallWorkspace: (SystemUser) -> Unit,
    onCreateWorkspace: (String) -> Unit,
    onRenameWorkspace: (SystemUser, String?) -> Unit,
    onChangeWorkspaceIcon: (SystemUser, WorkspaceIconKey) -> Unit,
    provisioningAllowed: Boolean,
    message: String?,
    onProvision: () -> Unit,
    onOpenWorkHarbor: () -> Unit,
    onSendFilesToWork: () -> Unit,
    onAdvanced: () -> Unit,
) {
    var createWorkspace by remember { mutableStateOf(false) }
    var newWorkspaceName by remember { mutableStateOf("Harbor Lab") }
    var renameTarget by remember { mutableStateOf<SystemUser?>(null) }
    var alias by remember { mutableStateOf("") }

    if (createWorkspace) {
        AlertDialog(
            onDismissRequest = { createWorkspace = false },
            title = { Text("Create workspace") },
            text = {
                OutlinedTextField(
                    value = newWorkspaceName,
                    onValueChange = { newWorkspaceName = it },
                    label = { Text("Android user name") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(enabled = !workspaceBusy, onClick = {
                    createWorkspace = false
                    onCreateWorkspace(newWorkspaceName)
                }) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { createWorkspace = false }) { Text("Cancel") } },
        )
    }

    renameTarget?.let { user ->
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("Rename workspace") },
            text = {
                OutlinedTextField(
                    value = alias,
                    onValueChange = { alias = it },
                    label = { Text("Harbor-only name") },
                    supportingText = { Text("This does not rename the Android user") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    renameTarget = null
                    onRenameWorkspace(user, alias)
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { renameTarget = null }) { Text("Cancel") } },
        )
    }

    val associatedHarbor = topology.associatedProfiles.any {
        !it.isCurrent && it.ownership == ProfileOwnership.HARBOR_INSTALLED_OWNER_UNKNOWN
    }
    val hasForeignOrUnknownProfile = topology.associatedProfiles.any {
        !it.isCurrent && it.ownership == ProfileOwnership.FOREIGN_OR_UNKNOWN
    }
    val setupMessage = when {
        associatedHarbor -> "Work profile active and managed by Harbor."
        hasForeignOrUnknownProfile && provisioningAllowed ->
            "Another profile exists. Harbor will not take ownership of it."
        hasForeignOrUnknownProfile ->
            "Another profile exists and Android does not currently allow another work profile."
        provisioningAllowed -> "Ready to create a work profile."
        else -> "Android does not currently allow another work profile."
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Harbor") },
                actions = { TextButton(onClick = onAdvanced) { Text("Advanced") } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Personal profile", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Keep work apps and data in a separate Android space.",
                style = MaterialTheme.typography.bodyMedium,
            )
            PrivilegeBadge(privilegeState)

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Spaces", style = MaterialTheme.typography.titleMedium)
                            Text(setupMessage, style = MaterialTheme.typography.bodySmall)
                        }
                        TextButton(enabled = !workspaceBusy, onClick = onRefreshWorkspaces) {
                            Text("Refresh")
                        }
                    }
                    ProfileRow(
                        name = "Personal",
                        detail = "This Android user",
                    )
                    ProfileRow(
                        name = "Work",
                        detail = if (associatedHarbor) "Active · Harbor manages it" else "Not set up",
                        action = if (associatedHarbor) {
                            { OutlinedButton(onClick = onOpenWorkHarbor) { Text("Manage") } }
                        } else if (provisioningAllowed) {
                            { Button(onClick = onProvision) { Text("Create") } }
                        } else {
                            null
                        },
                    )
                }
            }

            if (associatedHarbor) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Move files to Work", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Choose files in Personal; Harbor copies them to Work/Downloads/Harbor.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Button(onClick = onSendFilesToWork) { Text("Choose files") }
                    }
                }
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Update Harbor", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Update the work copy from a browser or Files app with the briefcase badge.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            if (workspaceUsers.isNotEmpty() || privilegeState.level != HarborPrivilegeLevel.STANDARD) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Additional workspaces", style = MaterialTheme.typography.titleMedium)
                        if (workspaceUsers.isEmpty()) {
                            Text(
                                "Optional Shizuku access is required to manage Android users.",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        } else {
                            workspaceUsers.filter { it.id != workspaceCurrentUserId }.forEach { user ->
                                val metadata = workspaceMetadata.firstOrNull {
                                    !it.stale && it.androidUserId == user.id && it.lastKnownSystemName == user.name
                                }
                                WorkspaceCard(
                                    user = user,
                                    metadata = metadata,
                                    busy = workspaceBusy,
                                    onSwitch = { onSwitchWorkspace(user) },
                                    onInstall = { onInstallWorkspace(user) },
                                    onRename = {
                                        alias = metadata?.alias.orEmpty()
                                        renameTarget = user
                                    },
                                    onChangeIcon = {
                                        val icons = WorkspaceIconKey.entries
                                        val current = metadata?.iconKey ?: WorkspaceIconKey.GENERIC
                                        onChangeWorkspaceIcon(user, icons[(icons.indexOf(current) + 1) % icons.size])
                                    },
                                )
                            }
                        }
                        if (privilegeState.level != HarborPrivilegeLevel.STANDARD) {
                            OutlinedButton(enabled = !workspaceBusy, onClick = { createWorkspace = true }) {
                                Text("Create workspace")
                            }
                        }
                    }
                }
            }

            workspaceMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
            message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }

            Text(
                "No accounts, analytics, advertising, or network access.",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                "Remove a work profile in Android Settings. This permanently deletes its apps and data.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun ProfileRow(
    name: String,
    detail: String,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(name, style = MaterialTheme.typography.bodyLarge)
            Text(detail, style = MaterialTheme.typography.bodySmall)
        }
        action?.invoke()
    }
}

@Composable
private fun WorkspaceCard(
    user: SystemUser,
    metadata: WorkspaceMetadata?,
    busy: Boolean,
    onSwitch: () -> Unit,
    onInstall: () -> Unit,
    onRename: () -> Unit,
    onChangeIcon: () -> Unit,
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(metadata?.alias ?: user.name, style = MaterialTheme.typography.bodyLarge)
                    Text("Android user · profile state unknown", style = MaterialTheme.typography.bodySmall)
                }
                TextButton(enabled = !busy, onClick = onSwitch) { Text("Switch") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(enabled = !busy, onClick = onInstall) { Text("Install Harbor") }
                TextButton(enabled = !busy, onClick = onRename) { Text("Rename") }
                TextButton(enabled = !busy, onClick = onChangeIcon) {
                    Text("Icon: ${metadata?.iconKey?.name ?: "GENERIC"}")
                }
            }
        }
    }
}
