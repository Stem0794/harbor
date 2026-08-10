package com.monstera.harbor.feature.advanced

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.monstera.harbor.core.topology.HarborPrivilegeLevel
import com.monstera.harbor.core.topology.HarborPrivilegeSecondaryState
import com.monstera.harbor.core.topology.HarborPrivilegeState

@Composable
fun PrivilegeBadge(
    state: HarborPrivilegeState,
    modifier: Modifier = Modifier,
) {
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Privilege", style = MaterialTheme.typography.labelLarge)
            Text(state.level.label(), style = MaterialTheme.typography.titleMedium)
            Text(state.secondary.label(), style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun HarborPrivilegeLevel.label(): String = when (this) {
    HarborPrivilegeLevel.STANDARD -> "Standard"
    HarborPrivilegeLevel.SHIZUKU_ADB -> "Shizuku ADB"
    HarborPrivilegeLevel.SHIZUKU_ROOT -> "Shizuku Root"
}

private fun HarborPrivilegeSecondaryState.label(): String = when (this) {
    HarborPrivilegeSecondaryState.ADVANCED_DISABLED -> "Advanced tools disabled"
    HarborPrivilegeSecondaryState.SHIZUKU_NOT_INSTALLED -> "Shizuku not installed"
    HarborPrivilegeSecondaryState.SHIZUKU_STOPPED -> "Shizuku stopped"
    HarborPrivilegeSecondaryState.SHIZUKU_PERMISSION_REQUIRED -> "Shizuku permission required"
    HarborPrivilegeSecondaryState.SHIZUKU_PERMISSION_DENIED -> "Shizuku permission denied"
    HarborPrivilegeSecondaryState.ADVANCED_WORKSPACE_TOOLS_AVAILABLE -> "Advanced workspace tools available"
    HarborPrivilegeSecondaryState.ROOT_ALLOWLIST_ONLY -> "Harbor's operation allowlist still applies"
    HarborPrivilegeSecondaryState.UNSUPPORTED -> "Shizuku identity or platform is unsupported"
}
