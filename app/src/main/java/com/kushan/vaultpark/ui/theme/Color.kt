package com.kushan.vaultpark.ui.theme

import androidx.compose.ui.graphics.Color

// ============ NEON-DARK MINIMAL PALETTE ============

// Background Colors
val Background = Color(0xFF121212)           // Off-black
val Surface = Color(0xFF1E1E1E)              // Card surface
val SurfaceVariant = Color(0xFF2A2A2A)       // Elevated cards

// Brand Colors - Neon Accent
val PrimaryPurple = Color(0xFF7F00FF)        // Electric Violet
val SecondaryGold = Color(0xFFFFD700)        // Gold/Yellow

// Status Colors
val StatusActive = Color(0xFF00C853)         // Green - Active parking
val StatusInactive = Color(0xFF9E9E9E)       // Grey - Inactive
val StatusError = Color(0xFFFF5252)          // Red - Errors

// Text Colors
val TextPrimary = Color(0xFFFFFFFF)          // White
val TextSecondary = Color(0xFFB0B0B0)        // Light grey
val TextTertiary = Color(0xFF707070)         // Medium grey

// Utility Colors (Legacy support)
val DeepBlue = PrimaryPurple
val DeepBlueDark = Color(0xFF0D1B4F)
val DeepBlueLight = Color(0xFF3D5AFE)

val PurpleAccent = PrimaryPurple
val PurpleLight = Color(0xFFB39DDB)
val PurpleDark = Color(0xFF512DA8)

val DarkBackground = Background
val SurfaceColor = Surface
val CardColor = SurfaceVariant

val ErrorColor = StatusError
val SuccessColor = StatusActive
val WarningColor = SecondaryGold
val InfoColor = Color(0xFF2196F3)
