package com.monstera.harbor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.monstera.harbor.core.topology.ProfileTopology
import com.monstera.harbor.core.topology.ProfileOwnership

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalProfileScreen(
    topology: ProfileTopology,
    provisioningAllowed: Boolean,
    message: String?,
    onProvision: () -> Unit,
    onOpenWorkHarbor: () -> Unit,
    onAdvanced: () -> Unit,
) {
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
