package com.monstera.harbor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.AlertDialog
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
import com.monstera.harbor.core.topology.ProfileTopology
import com.monstera.harbor.core.topology.ProfileOwnership
import com.monstera.harbor.core.topology.HarborPrivilegeState
import com.monstera.harbor.core.topology.SystemUser
import com.monstera.harbor.core.data.WorkspaceIconKey
import com.monstera.harbor.core.data.WorkspaceMetadata
import com.monstera.harbor.feature.advanced.PrivilegeBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalProfileScreen(
    topology: ProfileTopology,
    privilegeState: HarborPrivilegeState,
    workspaceUsers: List<SystemUser>,
    workspaceCurrentUserId: com.monstera.harbor.core.topology.AndroidUserId?,
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
    Scaffold(topBar = { TopAppBar(title = { Text("Harbor") }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Private space for everyday apps", style = MaterialTheme.typography.headlineMedium)
            Text("Harbor creates a standard Android work profile. Apps and data in that profile are isolated from this personal profile.")

            PrivilegeBadge(privilegeState)

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Spaces", style = MaterialTheme.typography.titleMedium)
                        TextButton(enabled = !workspaceBusy, onClick = onRefreshWorkspaces) { Text("Refresh") }
                    }
                    Text("Personal", style = MaterialTheme.typography.bodyLarge)
                    Text("Current Android user", style = MaterialTheme.typography.bodySmall)
                    if (associatedHarbor) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text("Work", style = MaterialTheme.typography.bodyLarge)
                                Text("Work profile active", style = MaterialTheme.typography.bodySmall)
                            }
                            TextButton(onClick = onOpenWorkHarbor) { Text("Manage") }
                        }
                    }
                    if (workspaceUsers.isEmpty()) {
                        Text(
                            if (privilegeState.level == com.monstera.harbor.core.topology.HarborPrivilegeLevel.STANDARD) {
                                "Advanced workspaces require optional Shizuku access."
                            } else {
                                "No additional Android users were detected."
                            },
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
                    if (privilegeState.level != com.monstera.harbor.core.topology.HarborPrivilegeLevel.STANDARD) {
                        OutlinedButton(enabled = !workspaceBusy, onClick = { createWorkspace = true }) {
                            Text("Create workspace")
                        }
                    }
                }
            }
            workspaceMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Setup status", style = MaterialTheme.typography.titleMedium)
                    Text(
                        when {
                            associatedHarbor -> "Harbor is installed in an associated profile. Open it there for authoritative policy status."
                            hasForeignOrUnknownProfile && provisioningAllowed ->
                                "Another profile is present, but Android still allows standard work-profile provisioning. Harbor does not assume ownership of the existing profile."
                            hasForeignOrUnknownProfile ->
                                "Another profile is present and Android does not allow provisioning. Harbor does not own or identify that profile."
                            provisioningAllowed -> "This device is ready to create a work profile."
                            else -> "Android does not currently allow another work profile."
                        },
                    )
                    if (associatedHarbor) {
                        Button(onClick = onOpenWorkHarbor) { Text("Open work Harbor") }
                    } else if (provisioningAllowed) {
                        Button(enabled = provisioningAllowed, onClick = onProvision) {
                            Text("Create work profile")
                        }
                    }
                }
            }

            if (associatedHarbor) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Updating Harbor", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "To update the Harbor copy that manages your work profile, download and open the new APK from inside the work profile (the browser or Files app with the briefcase badge). Personal and work copies are separate, so updating one does not update the other.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }

            Text("Harbor does not use accounts, analytics, advertising, or network access.")
            OutlinedButton(onClick = onAdvanced) { Text("Advanced tools") }
            Spacer(Modifier.height(12.dp))
            Text(
                "To remove a work profile, use Android Settings. Removing it permanently deletes all work-profile apps and data.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
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
                Column(Modifier.weight(1f)) {
                    Text(metadata?.alias ?: user.name, style = MaterialTheme.typography.bodyLarge)
                    Text("Android user · profile state unknown", style = MaterialTheme.typography.bodySmall)
                }
                TextButton(enabled = !busy, onClick = onSwitch) { Text("Switch") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(enabled = !busy, onClick = onInstall) { Text("Install Harbor") }
                TextButton(enabled = !busy, onClick = onRename) { Text("Rename") }
                TextButton(enabled = !busy, onClick = onChangeIcon) { Text("Icon: ${metadata?.iconKey?.name ?: "GENERIC"}") }
            }
        }
    }
}
