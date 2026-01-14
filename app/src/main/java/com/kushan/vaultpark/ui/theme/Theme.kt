package com.kushan.vaultpark.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = DeepBlue,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = DeepBlueLight,
    onPrimaryContainer = DeepBlueDark,
    
    secondary = PurpleAccent,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = PurpleLight,
    onSecondaryContainer = PurpleDark,
    
    tertiary = InfoColor,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF1565C0),
    onTertiaryContainer = Color(0xFFE3F2FD),
    
    error = ErrorColor,
    onError = Color(0xFF000000),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFF9DEDC),
    
    background = DarkBackground,
    onBackground = TextPrimary,
    
    surface = SurfaceColor,
    onSurface = TextPrimary,
    surfaceVariant = CardColor,
    onSurfaceVariant = TextSecondary,
    
    outline = TextTertiary,
    outlineVariant = Color(0xFF49454E)
)

@Composable
fun VaultParkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = VaultParkTypography,
        content = content
    )
}
