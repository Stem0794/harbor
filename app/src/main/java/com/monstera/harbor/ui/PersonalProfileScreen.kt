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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
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
import com.monstera.harbor.ui.designsystem.HarborEmptyState
import com.monstera.harbor.ui.designsystem.HarborHeroCard
import com.monstera.harbor.ui.designsystem.HarborInfoCard
import com.monstera.harbor.ui.designsystem.HarborPrivacyCard
import com.monstera.harbor.ui.designsystem.HarborSectionTitle
import com.monstera.harbor.ui.designsystem.HarborSpacing
import com.monstera.harbor.ui.privacy.privacyFacts
import com.monstera.harbor.ui.privacy.workSpacePresentation

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
    val presentation = workSpacePresentation(
        harborManagedProfile = associatedHarbor,
        foreignProfile = hasForeignOrUnknownProfile,
        provisioningAllowed = provisioningAllowed,
    )

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
                .padding(horizontal = HarborSpacing.screen, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(HarborSpacing.section),
        ) {
            HarborHeroCard(
                title = presentation.title,
                body = presentation.body,
                tone = presentation.tone,
                primaryLabel = presentation.primaryAction,
                onPrimary = if (associatedHarbor) onOpenWorkHarbor else onProvision,
                secondaryLabel = if (associatedHarbor) "Send files" else null,
                onSecondary = if (associatedHarbor) onSendFilesToWork else null,
            )

            HarborPrivacyCard(privacyFacts())

            HarborInfoCard(
                title = "Your spaces",
                body = "Personal and Work are separate Android spaces. Harbor only manages profiles it owns.",
            ) {
                SpaceSummaryRow("Personal", "This Android user")
                SpaceSummaryRow(
                    "Work",
                    if (associatedHarbor) "Ready · managed by Harbor" else "Not set up",
                    action = if (associatedHarbor) {
                        { OutlinedButton(onClick = onOpenWorkHarbor) { Text("Open Work") } }
                    } else if (provisioningAllowed) {
                        { OutlinedButton(onClick = onProvision) { Text("Create") } }
                    } else null,
                )
            }

            if (associatedHarbor) {
                HarborInfoCard(
                    title = "Update Harbor in Work",
                    body = "Use the browser or Files app inside Work and choose the Harbor icon with the briefcase badge.",
                )
            }

            if (workspaceUsers.isNotEmpty() || privilegeState.level != HarborPrivilegeLevel.STANDARD) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    HarborSectionTitle(
                        title = "Additional workspaces",
                        supportingText = "Experimental full-user workspaces are managed through Advanced tools.",
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(
                        enabled = !workspaceBusy,
                        onClick = onRefreshWorkspaces,
                    ) {
                        Text("Refresh")
                    }
                }
                HarborInfoCard(
                    title = "Optional workspaces",
                    body = if (workspaceUsers.isEmpty()) {
                        "Enable Advanced tools to manage additional Android users."
                    } else null,
                ) {
                    workspaceUsers.filter { it.id != workspaceCurrentUserId }.forEach { user ->
                        val metadata = workspaceMetadata.firstOrNull {
                            !it.stale && it.androidUserId == user.id && it.lastKnownSystemName == user.name
                        }
                        WorkspaceRow(
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
                    if (privilegeState.level != HarborPrivilegeLevel.STANDARD) {
                        OutlinedButton(enabled = !workspaceBusy, onClick = { createWorkspace = true }) {
                            Text("Create workspace")
                        }
                    }
                }
            }

            workspaceMessage?.let { HarborEmptyState("Workspace update", it) }
            message?.let { HarborEmptyState("Harbor status", it) }
            Text(
                "Remove a Work profile in Android Settings. This permanently deletes its apps and data.",
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SpaceSummaryRow(
    name: String,
    detail: String,
    action: (@Composable () -> Unit)? = null,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(name, style = androidx.compose.material3.MaterialTheme.typography.bodyLarge)
            Text(detail, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
        }
        action?.invoke()
    }
}

@Composable
private fun WorkspaceRow(
    user: SystemUser,
    metadata: WorkspaceMetadata?,
    busy: Boolean,
    onSwitch: () -> Unit,
    onInstall: () -> Unit,
    onRename: () -> Unit,
    onChangeIcon: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(metadata?.alias ?: user.name, style = androidx.compose.material3.MaterialTheme.typography.bodyLarge)
                Text("Android user · profile state unknown", style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
            }
            OutlinedButton(enabled = !busy, onClick = onSwitch) { Text("Switch") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            TextButton(enabled = !busy, onClick = onInstall) { Text("Install Harbor") }
            TextButton(enabled = !busy, onClick = onRename) { Text("Rename") }
            TextButton(enabled = !busy, onClick = onChangeIcon) { Text("Icon") }
        }
    }
}
