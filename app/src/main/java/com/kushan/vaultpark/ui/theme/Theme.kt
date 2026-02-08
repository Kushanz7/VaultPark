package com.kushan.vaultpark.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = NeonLime,
    onPrimary = TextDarkLight,
    primaryContainer = Color(0xFFF0FFE0),
    onPrimaryContainer = Color(0xFF3A5F00),
    
    secondary = SoftMintGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F7ED),
    onSecondaryContainer = Color(0xFF0D4D2C),
    
    tertiary = NeonLime,
    onTertiary = TextDarkLight,
    tertiaryContainer = Color(0xFFF0FFE0),
    onTertiaryContainer = Color(0xFF3A5F00),
    
    error = StatusError,
    onError = Color.White,
    errorContainer = Color(0xFFFFEDED),
    onErrorContainer = Color(0xFF8B0000),
    
    background = LightBackground,
    onBackground = TextDarkLight,
    
    surface = LightSurface,
    onSurface = TextDarkLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    
    outline = GrayLight,
    outlineVariant = Divider,
    scrim = Color(0x66000000),
    
    inverseSurface = DarkSurface,
    inverseOnSurface = TextLight,
    inversePrimary = NeonLime
)

private val DarkColorScheme = darkColorScheme(
    primary = NeonLime,
    onPrimary = TextDarkLight,
    primaryContainer = NeonLime,
    onPrimaryContainer = TextDarkLight,
    
    secondary = SoftMintGreen,
    onSecondary = TextDarkLight,
    secondaryContainer = SoftMintGreen,
    onSecondaryContainer = TextDarkLight,
    
    tertiary = NeonLime,
    onTertiary = TextDarkLight,
    tertiaryContainer = NeonLime,
    onTertiaryContainer = TextDarkLight,
    
    error = StatusError,
    onError = TextLight,
    errorContainer = StatusError,
    onErrorContainer = TextLight,
    
    background = DarkBackground,
    onBackground = TextLight,
    
    surface = DarkSurface,
    onSurface = TextLight,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    
    outline = GrayDark,
    outlineVariant = GrayDark,
    scrim = Color(0xFF000000),
    
    inverseSurface = LightSurface,
    inverseOnSurface = TextDarkLight,
    inversePrimary = NeonLime
)

// Global theme state
private val isDarkThemeState = mutableStateOf<Boolean?>(null)

@Composable
fun VaultParkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Use stored theme preference if set, otherwise use system preference
    val currentTheme = remember { isDarkThemeState }
    val effectiveDarkTheme = currentTheme.value ?: darkTheme
    
    val colorScheme = if (effectiveDarkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = VaultParkTypography,
        shapes = VaultParkShapes,
        content = content
    )
}

// Function to toggle theme
fun toggleTheme() {
    isDarkThemeState.value = !(isDarkThemeState.value ?: true)
}

// Function to set theme explicitly
fun setDarkTheme(isDark: Boolean) {
    isDarkThemeState.value = isDark
}

// Function to get current theme state
fun isDarkThemeEnabled(): Boolean {
    return isDarkThemeState.value ?: true
}

object RoleTheme {
    val driverColor: Color
        @Composable
        get() = if (isSystemInDarkTheme() || isDarkThemeEnabled()) DriverGreen else DriverGreenLight

    val securityColor: Color
        @Composable
        get() = if (isSystemInDarkTheme() || isDarkThemeEnabled()) SecurityPurple else SecurityPurpleLight
}
