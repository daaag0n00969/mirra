package dev.dag0n.mirra.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Scheme = darkColorScheme(
    primary = Cyan,
    onPrimary = Color(0xFF042226),
    secondary = CyanDim,
    onSecondary = Ink,
    background = Navy,
    onBackground = Ink,
    surface = NavyRaised,
    onSurface = Ink,
    surfaceVariant = Color(0xFF1A2748),
    onSurfaceVariant = Muted,
    error = Danger,
    onError = Color.White,
    outline = Color(0xFF2A3B63),
)

@Composable
fun MirraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Scheme,
        content = content,
    )
}
