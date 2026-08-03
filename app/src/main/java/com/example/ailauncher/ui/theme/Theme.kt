package com.example.ailauncher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PitchBlackColorScheme = darkColorScheme(
    primary = PureWhite,
    onPrimary = PitchBlack,
    primaryContainer = PureWhite,
    onPrimaryContainer = PitchBlack,
    secondary = MutedGray,
    onSecondary = PureWhite,
    background = PitchBlack,
    onBackground = PureWhite,
    surface = PitchBlack,
    onSurface = PureWhite,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = PureWhite,
    outline = BorderWhite,
    outlineVariant = MutedGray
)

@Composable
fun AILauncherTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PitchBlackColorScheme,
        content = content
    )
}
