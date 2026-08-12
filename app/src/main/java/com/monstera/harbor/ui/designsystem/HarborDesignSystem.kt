package com.monstera.harbor.ui.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

object HarborSpacing {
    val screen = 20.dp
    val section = 18.dp
    val card = 16.dp
    val compact = 10.dp
}

object HarborShapes {
    val hero = RoundedCornerShape(28.dp)
    val card = RoundedCornerShape(20.dp)
    val row = RoundedCornerShape(16.dp)
    val pill = RoundedCornerShape(50)
}

val HarborAppIconSize = 48.dp

enum class StatusTone {
    Positive,
    Neutral,
    Warning,
    Critical,
}

@Composable
fun HarborStatusPill(
    text: String,
    tone: StatusTone = StatusTone.Neutral,
    modifier: Modifier = Modifier,
) {
    val (container, content) = when (tone) {
        StatusTone.Positive -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        StatusTone.Neutral -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        StatusTone.Warning -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        StatusTone.Critical -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    }
    Surface(
        modifier = modifier,
        shape = HarborShapes.pill,
        color = container,
        contentColor = content,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
fun HarborHeroCard(
    title: String,
    body: String,
    tone: StatusTone,
    primaryLabel: String? = null,
    onPrimary: (() -> Unit)? = null,
    secondaryLabel: String? = null,
    onSecondary: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val accent = when (tone) {
        StatusTone.Positive -> MaterialTheme.colorScheme.primaryContainer
        StatusTone.Neutral -> MaterialTheme.colorScheme.surfaceVariant
        StatusTone.Warning -> MaterialTheme.colorScheme.tertiaryContainer
        StatusTone.Critical -> MaterialTheme.colorScheme.errorContainer
    }
    Card(
        modifier = modifier,
        shape = HarborShapes.hero,
        colors = CardDefaults.cardColors(containerColor = accent),
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            HarborStatusPill(
                text = when (tone) {
                    StatusTone.Positive -> "Ready"
                    StatusTone.Neutral -> "Harbor"
                    StatusTone.Warning -> "Needs attention"
                    StatusTone.Critical -> "Unavailable"
                },
                tone = tone,
            )
            Text(title, style = MaterialTheme.typography.headlineSmall)
            Text(body, style = MaterialTheme.typography.bodyLarge)
            if (primaryLabel != null && onPrimary != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = onPrimary) { Text(primaryLabel) }
                    if (secondaryLabel != null && onSecondary != null) {
                        OutlinedButton(onClick = onSecondary) { Text(secondaryLabel) }
                    }
                }
            }
        }
    }
}

@Composable
fun HarborInfoCard(
    title: String,
    body: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable (() -> Unit)? = null,
) {
    Card(
        modifier = modifier,
        shape = HarborShapes.card,
    ) {
        Column(
            modifier = Modifier.padding(HarborSpacing.card),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            body?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            content?.invoke()
        }
    }
}

@Composable
fun HarborSectionTitle(
    title: String,
    supportingText: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 2.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        supportingText?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

data class PrivacyFact(val title: String, val body: String)

@Composable
fun HarborPrivacyCard(
    facts: List<PrivacyFact>,
    modifier: Modifier = Modifier,
) {
    HarborInfoCard(
        title = "Privacy by default",
        modifier = modifier,
    ) {
        facts.forEach { fact ->
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(fact.title, style = MaterialTheme.typography.labelLarge)
                Text(fact.body, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun HarborEmptyState(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = HarborShapes.card,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun HarborSettingsRow(
    title: String,
    body: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "$title. $body"
                role = Role.Button
            },
        shape = HarborShapes.row,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("›", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun HarborSpacer(height: androidx.compose.ui.unit.Dp = HarborSpacing.section) {
    Spacer(Modifier.height(height))
}
