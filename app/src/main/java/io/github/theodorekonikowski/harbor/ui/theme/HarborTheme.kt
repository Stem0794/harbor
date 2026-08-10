package io.github.theodorekonikowski.harbor.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF005C78),
    onPrimary = Color.White,
    secondary = Color(0xFF46636B),
    background = Color(0xFFF6FAFB),
    surface = Color(0xFFFFFFFF),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7DD0E9),
    onPrimary = Color(0xFF003544),
    secondary = Color(0xFFB4CBD2),
    background = Color(0xFF0E1416),
    surface = Color(0xFF151D20),
)

@Composable
fun HarborTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
