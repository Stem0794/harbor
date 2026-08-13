package com.monstera.harbor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.monstera.harbor.core.data.WorkspaceIconKey
import com.monstera.harbor.core.data.WorkspaceMetadata
import com.monstera.harbor.core.topology.AndroidUserId
import com.monstera.harbor.core.topology.HarborPrivilegeLevel
import com.monstera.harbor.core.topology.HarborPrivilegeState
import com.monstera.harbor.core.topology.ProfileOwnership
import com.monstera.harbor.core.topology.ProfileTopology
import com.monstera.harbor.core.topology.SystemUser
import com.monstera.harbor.ui.designsystem.HarborBottomBar
import com.monstera.harbor.ui.designsystem.HarborColors
import com.monstera.harbor.ui.designsystem.HarborHeroBackground
import com.monstera.harbor.ui.designsystem.HarborIcon
import com.monstera.harbor.ui.designsystem.HarborIconKind
import com.monstera.harbor.ui.designsystem.HarborHeader
import com.monstera.harbor.ui.designsystem.HarborPrivacyPanel
import com.monstera.harbor.ui.designsystem.HarborQuickActionTile
import com.monstera.harbor.ui.designsystem.HarborShapes
import com.monstera.harbor.ui.designsystem.HarborSpacing
import com.monstera.harbor.ui.designsystem.PrivacyFact
import com.monstera.harbor.ui.designsystem.StatusTone
import com.monstera.harbor.ui.privacy.PersonalHeroAction
import com.monstera.harbor.ui.privacy.personalHeroActions
import com.monstera.harbor.ui.privacy.privacyFacts
import com.monstera.harbor.ui.privacy.workSpacePresentation

private data class HeroQuickAction(
    val label: String,
    val icon: HarborIconKind,
    val onClick: () -> Unit,
)

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
    var showSettings by remember { mutableStateOf(false) }
    val settingsSheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (createWorkspace) {
        AlertDialog(
            onDismissRequest = { createWorkspace = false },
            title = { Text("Create workspace") },
            text = {
                OutlinedTextField(value = newWorkspaceName, onValueChange = { newWorkspaceName = it }, label = { Text("Android user name") }, singleLine = true)
            },
            confirmButton = {
                TextButton(enabled = !workspaceBusy, onClick = { createWorkspace = false; onCreateWorkspace(newWorkspaceName) }) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { createWorkspace = false }) { Text("Cancel") } },
        )
    }

    renameTarget?.let { user ->
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("Rename workspace") },
            text = {
                OutlinedTextField(value = alias, onValueChange = { alias = it }, label = { Text("Harbor-only name") }, supportingText = { Text("This does not rename the Android user") }, singleLine = true)
            },
            confirmButton = { TextButton(onClick = { renameTarget = null; onRenameWorkspace(user, alias) }) { Text("Save") } },
            dismissButton = { TextButton(onClick = { renameTarget = null }) { Text("Cancel") } },
        )
    }

    if (showSettings) {
        PersonalSettingsSheet(
            sheetState = settingsSheet,
            onDismiss = { showSettings = false },
            onAdvanced = { showSettings = false; onAdvanced() },
        )
    }

    val associatedHarbor = topology.associatedProfiles.any { !it.isCurrent && it.ownership == ProfileOwnership.HARBOR_INSTALLED_OWNER_UNKNOWN }
    val hasForeignOrUnknownProfile = topology.associatedProfiles.any { !it.isCurrent && it.ownership == ProfileOwnership.FOREIGN_OR_UNKNOWN }
    val presentation = workSpacePresentation(associatedHarbor, hasForeignOrUnknownProfile, provisioningAllowed)
    val ready = associatedHarbor
    val resolvedHeroActions = personalHeroActions(associatedHarbor, hasForeignOrUnknownProfile, provisioningAllowed)

    fun heroAction(action: PersonalHeroAction): HeroQuickAction = when (action) {
        PersonalHeroAction.OPEN_WORK -> HeroQuickAction("Open Work", HarborIconKind.Work, onOpenWorkHarbor)
        PersonalHeroAction.PROVISION_WORK -> HeroQuickAction("Create Work space", HarborIconKind.Plus, onProvision)
        PersonalHeroAction.SEND_FILES -> HeroQuickAction("Send files", HarborIconKind.Send, onSendFilesToWork)
        PersonalHeroAction.ADVANCED -> HeroQuickAction("Advanced", HarborIconKind.Settings, onAdvanced)
    }

    Scaffold(
        containerColor = HarborColors.bgPersonal,
        topBar = {
            HarborHeader(
                title = "Harbor",
                onAdvanced = onAdvanced,
            )
        },
        bottomBar = {
            HarborBottomBar(
                active = HarborIconKind.Home,
                onWork = if (ready) onOpenWorkHarbor else null,
                onSettings = { showSettings = true },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(HarborColors.bgPersonal)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = HarborSpacing.screen)
                .padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Direction2Hero(
                title = presentation.title,
                body = presentation.body,
                tone = presentation.tone,
                primaryAction = resolvedHeroActions.primary?.let(::heroAction),
                quickLeft = resolvedHeroActions.quickLeft?.let(::heroAction),
                quickRight = resolvedHeroActions.quickRight?.let(::heroAction),
            )
            HarborPrivacyPanel(privacyFacts())
            SecondaryPersonalContent(
                workspaceUsers = workspaceUsers,
                workspaceCurrentUserId = workspaceCurrentUserId,
                workspaceMetadata = workspaceMetadata,
                workspaceBusy = workspaceBusy,
                workspaceMessage = workspaceMessage,
                privilegeState = privilegeState,
                onRefreshWorkspaces = onRefreshWorkspaces,
                onSwitchWorkspace = onSwitchWorkspace,
                onInstallWorkspace = onInstallWorkspace,
                onCreateWorkspace = { createWorkspace = true },
                onRenameWorkspace = { user, currentAlias -> alias = currentAlias.orEmpty(); renameTarget = user },
                onChangeWorkspaceIcon = onChangeWorkspaceIcon,
                ready = ready,
                onOpenWorkHarbor = onOpenWorkHarbor,
                message = message,
            )
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun Direction2Hero(
    title: String,
    body: String,
    tone: StatusTone,
    primaryAction: HeroQuickAction?,
    quickLeft: HeroQuickAction?,
    quickRight: HeroQuickAction?,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 410.dp)
            .clip(HarborShapes.hero)
            .background(Brush.horizontalGradient(listOf(HarborColors.heroStart, HarborColors.heroEnd))),
    ) {
        // Keep the artwork in a stable 410.dp viewport while the hero surface
        // itself can grow for large text and accessibility settings.
        HarborHeroBackground(
            Modifier
                .fillMaxWidth()
                .height(410.dp)
                .align(Alignment.TopCenter),
        )
        Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            val (statusIcon, statusTint) = when (tone) {
                StatusTone.Positive -> HarborIconKind.Shield to HarborColors.accent
                StatusTone.Neutral -> HarborIconKind.Plus to HarborColors.accent
                StatusTone.Warning -> HarborIconKind.Warning to HarborColors.warning
                StatusTone.Critical -> HarborIconKind.Warning to HarborColors.danger
            }
            HarborIcon(statusIcon, Modifier.size(30.dp), statusTint)
            Text(title, color = HarborColors.textPrimary, style = androidx.compose.material3.MaterialTheme.typography.headlineLarge.copy(fontSize = androidx.compose.ui.unit.TextUnit(30f, androidx.compose.ui.unit.TextUnitType.Sp), lineHeight = androidx.compose.ui.unit.TextUnit(34f, androidx.compose.ui.unit.TextUnitType.Sp), fontWeight = FontWeight.Bold), modifier = Modifier.fillMaxWidth(.68f))
            Text(body, color = HarborColors.textSecondary, style = androidx.compose.material3.MaterialTheme.typography.bodyLarge.copy(fontSize = androidx.compose.ui.unit.TextUnit(14f, androidx.compose.ui.unit.TextUnitType.Sp), lineHeight = androidx.compose.ui.unit.TextUnit(19f, androidx.compose.ui.unit.TextUnitType.Sp)), modifier = Modifier.fillMaxWidth(.7f))
            primaryAction?.let { action ->
                Surface(onClick = action.onClick, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp), shape = RoundedCornerShape(18.dp), color = HarborColors.accent, contentColor = HarborColors.accentDark) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Text(action.label, style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                        Spacer(Modifier.weight(1f))
                        HarborIcon(HarborIconKind.ArrowRight, Modifier.size(25.dp), HarborColors.accentDark)
                    }
                }
            }
            val quickActions = listOfNotNull(quickLeft, quickRight)
            if (primaryAction != null && quickActions.isNotEmpty()) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HorizontalDivider(Modifier.weight(1f), color = HarborColors.stroke)
                    Text("or", color = HarborColors.textMuted, style = androidx.compose.material3.MaterialTheme.typography.labelMedium)
                    HorizontalDivider(Modifier.weight(1f), color = HarborColors.stroke)
                }
            }
            if (quickActions.isNotEmpty()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    quickActions.forEach { action ->
                        HarborQuickActionTile(action.label, action.icon, action.onClick, if (quickActions.size == 1) Modifier.fillMaxWidth() else Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun SecondaryPersonalContent(
    workspaceUsers: List<SystemUser>,
    workspaceCurrentUserId: AndroidUserId?,
    workspaceMetadata: List<WorkspaceMetadata>,
    workspaceBusy: Boolean,
    workspaceMessage: String?,
    privilegeState: HarborPrivilegeState,
    onRefreshWorkspaces: () -> Unit,
    onSwitchWorkspace: (SystemUser) -> Unit,
    onInstallWorkspace: (SystemUser) -> Unit,
    onCreateWorkspace: () -> Unit,
    onRenameWorkspace: (SystemUser, String?) -> Unit,
    onChangeWorkspaceIcon: (SystemUser, WorkspaceIconKey) -> Unit,
    ready: Boolean,
    onOpenWorkHarbor: () -> Unit,
    message: String?,
) {
    Surface(Modifier.fillMaxWidth(), shape = HarborShapes.card, color = HarborColors.surface, border = androidx.compose.foundation.BorderStroke(1.dp, HarborColors.stroke)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Your spaces", color = HarborColors.textPrimary, style = androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold))
            Text("Personal and Work are separate Android spaces. Harbor only manages profiles it owns.", color = HarborColors.textSecondary, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
            SpaceSummaryRow("Personal", "This Android user")
            SpaceSummaryRow("Work", if (ready) "Ready · managed by Harbor" else "Not set up", if (ready) onOpenWorkHarbor else null)
        }
    }
    if (ready) {
        Surface(Modifier.fillMaxWidth(), shape = HarborShapes.card, color = HarborColors.surface, border = androidx.compose.foundation.BorderStroke(1.dp, HarborColors.stroke)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Update Harbor in Work", color = HarborColors.textPrimary, style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                Text("Use a browser or Files app inside Work and choose the Harbor icon with the briefcase badge.", color = HarborColors.textSecondary, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
            }
        }
    }
    if (workspaceUsers.isNotEmpty() || privilegeState.level != HarborPrivilegeLevel.STANDARD) {
        Surface(Modifier.fillMaxWidth(), shape = HarborShapes.card, color = HarborColors.surface, border = androidx.compose.foundation.BorderStroke(1.dp, HarborColors.stroke)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) { Text("Additional workspaces", color = HarborColors.textPrimary, style = androidx.compose.material3.MaterialTheme.typography.titleMedium); Text("Experimental full-user workspaces", color = HarborColors.textSecondary, style = androidx.compose.material3.MaterialTheme.typography.bodySmall) }
                    TextButton(enabled = !workspaceBusy, onClick = onRefreshWorkspaces) { Text("Refresh") }
                }
                workspaceUsers.filter { it.id != workspaceCurrentUserId }.forEach { user ->
                    val metadata = workspaceMetadata.firstOrNull { !it.stale && it.androidUserId == user.id && it.lastKnownSystemName == user.name }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) { Text(metadata?.alias ?: user.name, color = HarborColors.textPrimary); Text("Android user · profile state unknown", color = HarborColors.textSecondary, style = androidx.compose.material3.MaterialTheme.typography.bodySmall) }
                        TextButton(enabled = !workspaceBusy, onClick = { onSwitchWorkspace(user) }) { Text("Switch") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) { TextButton(enabled = !workspaceBusy, onClick = { onInstallWorkspace(user) }) { Text("Install Harbor") }; TextButton(enabled = !workspaceBusy, onClick = { onRenameWorkspace(user, metadata?.alias) }) { Text("Rename") }; TextButton(enabled = !workspaceBusy, onClick = { val icons = WorkspaceIconKey.entries; val current = metadata?.iconKey ?: WorkspaceIconKey.GENERIC; onChangeWorkspaceIcon(user, icons[(icons.indexOf(current) + 1) % icons.size]) }) { Text("Icon") } }
                }
                if (privilegeState.level != HarborPrivilegeLevel.STANDARD) TextButton(enabled = !workspaceBusy, onClick = onCreateWorkspace) { Text("Create workspace") }
            }
        }
    }
    workspaceMessage?.let { LocalMessageCard("Workspace update", it) }
    message?.let { LocalMessageCard("Harbor status", it) }
    Text("Remove a Work profile in Android Settings. This permanently deletes its apps and data.", color = HarborColors.textMuted, style = androidx.compose.material3.MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 4.dp))
}

@Composable
private fun SpaceSummaryRow(name: String, detail: String, action: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) { Text(name, color = HarborColors.textPrimary, style = androidx.compose.material3.MaterialTheme.typography.bodyLarge); Text(detail, color = HarborColors.textSecondary, style = androidx.compose.material3.MaterialTheme.typography.bodySmall) }
        action?.let { TextButton(onClick = it) { Text("Open Work") } }
    }
}

@Composable
private fun LocalMessageCard(title: String, body: String) {
    Surface(Modifier.fillMaxWidth(), shape = HarborShapes.card, color = HarborColors.surfaceLow) { Column(Modifier.padding(16.dp)) { Text(title, color = HarborColors.textPrimary, style = androidx.compose.material3.MaterialTheme.typography.titleMedium); Text(body, color = HarborColors.textSecondary, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium) } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonalSettingsSheet(sheetState: SheetState, onDismiss: () -> Unit, onAdvanced: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = HarborColors.sheet, dragHandle = { Box(Modifier.padding(top = 10.dp).size(width = 44.dp, height = 4.dp).clip(RoundedCornerShape(50)).background(HarborColors.textSecondary)) }) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Harbor", color = HarborColors.textPrimary, style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
            Text("Local presentation and help", color = HarborColors.textSecondary)
            Direction2ActionSheetRow(HarborIconKind.Shield, "Advanced tools", "Optional Shizuku developer tools", onAdvanced)
            Direction2InfoSheetRow(HarborIconKind.Device, "Update Harbor", "Install the Work copy from inside the Work profile")
            Direction2InfoSheetRow(HarborIconKind.Lock, "Work-profile guidance", "Remove profiles from Android Settings")
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun Direction2ActionSheetRow(icon: HarborIconKind, title: String, body: String, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.Transparent, modifier = Modifier.fillMaxWidth().heightIn(min = 62.dp)) {
        Direction2SheetRowContent(icon, title, body)
    }
}

@Composable
private fun Direction2InfoSheetRow(icon: HarborIconKind, title: String, body: String) {
    Direction2SheetRowContent(
        icon = icon,
        title = title,
        body = body,
        modifier = Modifier.fillMaxWidth().heightIn(min = 62.dp).padding(horizontal = 4.dp),
    )
}

@Composable
private fun Direction2SheetRowContent(icon: HarborIconKind, title: String, body: String, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        HarborIcon(icon, Modifier.size(25.dp), HarborColors.textSecondary)
        Column(Modifier.weight(1f)) {
            Text(title, color = HarborColors.textPrimary, style = androidx.compose.material3.MaterialTheme.typography.bodyLarge)
            Text(body, color = HarborColors.textSecondary, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
        }
    }
}
