package com.monstera.harbor.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.monstera.harbor.ui.designsystem.HarborColors

private val LightColors = lightColorScheme(
    primary = Color(0xFF007E82),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB8F2EB),
    onPrimaryContainer = Color(0xFF003739),
    secondary = Color(0xFF45676A),
    background = Color(0xFFF2F8F8),
    surface = Color(0xFFF6FBFB),
    surfaceVariant = Color(0xFFDDE9E9),
    onSurface = Color(0xFF172022),
    onSurfaceVariant = Color(0xFF405255),
)

private val DarkColors = darkColorScheme(
    primary = HarborColors.accent,
    onPrimary = HarborColors.accentDark,
    primaryContainer = HarborColors.accentDark,
    onPrimaryContainer = HarborColors.accent,
    secondary = HarborColors.textSecondary,
    background = HarborColors.bgDeep,
    surface = HarborColors.surface,
    surfaceVariant = HarborColors.surfaceRaised,
    onSurface = HarborColors.textPrimary,
    onSurfaceVariant = HarborColors.textSecondary,
    error = HarborColors.danger,
    errorContainer = Color(0xFF422125),
    onErrorContainer = Color(0xFFFFDAD9),
)

@Composable
fun HarborTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
