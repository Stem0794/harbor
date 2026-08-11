package com.monstera.harbor.feature.advanced

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
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
    showAdvancedDetails: Boolean = false,
) {
    Card(modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Access", style = MaterialTheme.typography.labelLarge)
                Text(state.level.label(), style = MaterialTheme.typography.titleMedium)
            }
            Text(
                if (showAdvancedDetails) state.secondary.label() else "Standard Android access",
                modifier = Modifier.weight(1.4f),
                style = MaterialTheme.typography.bodySmall,
            )
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
