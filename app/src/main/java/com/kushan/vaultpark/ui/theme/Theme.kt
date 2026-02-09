package com.kushan.vaultpark.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Role-based Color Schemes
@Composable
fun VaultParkDarkColorScheme(userRole: String?) = darkColorScheme(
    primary = if (userRole == "SECURITY") PrimaryPurple else NeonLime,
    onPrimary = if (userRole == "SECURITY") Color.White else TextDarkLight,
    primaryContainer = if (userRole == "SECURITY") PurpleDark else NeonLime,
    onPrimaryContainer = if (userRole == "SECURITY") Color.White else TextDarkLight,
    
    secondary = SecondaryGold,
    onSecondary = TextDarkLight,
    secondaryContainer = SecondaryGold,
    onSecondaryContainer = TextDarkLight,
    
    tertiary = if (userRole == "SECURITY") SecurityPurpleLight else SoftMintGreen,
    onTertiary = Color.White,
    
    background = DarkBackground,
    onBackground = TextLight,
    
    surface = DarkSurface,
    onSurface = TextLight,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    
    error = StatusError,
    onError = Color.White,
    
    outline = GrayDark,
    outlineVariant = GrayDark,
    scrim = Color.Black,
    
    inverseSurface = LightSurface,
    inverseOnSurface = TextDarkLight,
    inversePrimary = if (userRole == "SECURITY") PrimaryPurple else NeonLime
)

@Composable
fun VaultParkLightColorScheme(userRole: String?) = lightColorScheme(
    primary = if (userRole == "SECURITY") SecurityPurpleLight else DriverGreenLight,
    onPrimary = Color.White,
    primaryContainer = if (userRole == "SECURITY") PurpleLight else SoftMintGreen,
    onPrimaryContainer = Color.White,
    
    secondary = SecondaryGold,
    onSecondary = TextDarkLight,
    secondaryContainer = Color(0xFFFFF8E1),
    onSecondaryContainer = Color(0xFF4A3400),
    
    tertiary = if (userRole == "SECURITY") PrimaryPurple else DriverTextDark,
    onTertiary = Color.White,
    
    background = LightBackground,
    onBackground = TextDarkLight,
    
    surface = LightSurface,
    onSurface = TextDarkLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    
    error = StatusError,
    onError = Color.White,
    
    outline = GrayLight,
    outlineVariant = Divider,
    scrim = Color(0x66000000),
    
    inverseSurface = DarkSurface,
    inverseOnSurface = TextLight,
    inversePrimary = if (userRole == "SECURITY") SecurityPurpleLight else DriverGreenLight
)

@Composable
fun VaultParkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    userRole: String? = null, // "DRIVER" or "SECURITY"
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> VaultParkDarkColorScheme(userRole)
        else -> VaultParkLightColorScheme(userRole)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = VaultParkTypography,
        shapes = VaultParkShapes,
        content = content
    )
}

// Helper to determine if we are in dark mode (effective)
@Composable
fun isAppInDarkTheme(): Boolean {
    return MaterialTheme.colorScheme.background == DarkBackground
}

// Backward compatibility for existing screens
object RoleTheme {
    val driverColor: Color
        @Composable
        get() = if (isAppInDarkTheme()) DriverGreen else DriverGreenLight

    val securityColor: Color
        @Composable
        get() = if (isAppInDarkTheme()) SecurityPurple else SecurityPurpleLight
}
