package com.kushan.vaultpark.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NeonDarkColorScheme = darkColorScheme(
    primary = PrimaryPurple,
    onPrimary = TextPrimary,
    primaryContainer = PrimaryPurple,
    onPrimaryContainer = TextPrimary,
    
    secondary = SecondaryGold,
    onSecondary = Background,
    secondaryContainer = SecondaryGold,
    onSecondaryContainer = Background,
    
    tertiary = SecondaryGold,
    onTertiary = Background,
    tertiaryContainer = SecondaryGold,
    onTertiaryContainer = Background,
    
    error = StatusError,
    onError = TextPrimary,
    errorContainer = StatusError,
    onErrorContainer = TextPrimary,
    
    background = Background,
    onBackground = TextPrimary,
    
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    
    outline = TextTertiary,
    outlineVariant = TextTertiary,
    scrim = Color(0xFF000000)
)

@Composable
fun VaultParkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = NeonDarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = VaultParkTypography,
        shapes = VaultParkShapes,
        content = content
    )
}
