package com.kushan.vaultpark.ui.theme

import androidx.compose.ui.graphics.Color

// ============ MODERN PARKING APP PALETTE ============

// Primary Brand Colors
val NeonLime = Color(0xFFA4FF07)            // Primary accent - Pay Now buttons, active states
val SoftMintGreen = Color(0xFF50C878)       // Success/Safe - Free spots, confirmations

// Billing Theme Colors (Purple/Gold)
val PrimaryPurple = Color(0xFF7C3AED)       // Primary purple for billing
val SecondaryGold = Color(0xFFFCD34D)       // Secondary gold for accents
val PurpleDark = Color(0xFF6D28D9)          // Darker purple
val PurpleLight = Color(0xFFA78BFA)         // Lighter purple
// Security Role Colors
val SecurityPurple = PrimaryPurple          // Default/Dark mode purple
val SecurityPurpleLight = Color(0xFF6D28D9) // Darker purple for Light mode (better contrast)

// Driver Role Colors
val DriverGreen = NeonLime                  // Default/Dark mode green
val DriverGreenLight = Color(0xFF4C9F06)    // Darker green for Light mode (better contrast)
val DriverGreenSecondary = SoftMintGreen    // Secondary driver green

// Surface Colors
val DarkGrey = Color(0xFF6E6E6E)            // Secondary surface - Unselected buttons, inactive tabs
val MidnightBlack = Color(0xFF1A1B1E)       // Main app background (Dark mode)
val OffWhite = Color(0xFFF2F2F2)            // Card/Text color (Light mode)

// Light Theme Colors
val LightBackground = Color(0xFFFAFAFA)     // Light gray background
val LightSurface = Color(0xFFFFFFFF)        // White surface/cards
val LightSurfaceVariant = Color(0xFFF5F5F5) // Light gray variant

// Dark Theme Colors (Primary)
val DarkBackground = MidnightBlack          // #1A1B1E - Deep background
val DarkSurface = Color(0xFF2A2B2E)         // Slightly lighter than background
val DarkSurfaceVariant = Color(0xFF3A3B3E)  // Even lighter variant

// Text Colors - Light Theme
val TextDarkLight = Color(0xFF1A1B1E)       // Dark text for light mode
val TextSecondaryLight = Color(0xFF666666)  // Secondary text
val TextTertiaryLight = Color(0xFF999999)   // Tertiary/hint text

// Text Colors - Dark Theme
val TextLight = Color(0xFFF2F2F2)           // Off-white text for dark mode
val TextSecondaryDark = Color(0xFFCCCCCC)   // Secondary text dark mode
val TextTertiaryDark = Color(0xFF999999)    // Tertiary text dark mode

// Status & Utility Colors
val StatusSuccess = SoftMintGreen           // #50C878 - Green success
val StatusError = Color(0xFFFF6B6B)         // Red - Errors
val StatusWarning = Color(0xFFFFB84D)       // Orange - Warning
val StatusInfo = Color(0xFF4DA6FF)          // Blue - Info

// Neutral Colors
val GrayLight = Color(0xFFE8E8E8)           // Light gray
val GrayMedium = DarkGrey                   // #6E6E6E - Medium gray
val GrayDark = Color(0xFF4A4A4A)            // Dark gray
val Divider = Color(0xFFE0E0E0)             // Divider color

// Component Background Colors
val InputBackground = Color(0xFFFAFAFA)    // Input field background (light)
val InputBackgroundDark = Color(0xFF3A3B3E) // Input field background (dark)

// Legacy Support (mapped to new colors)
val Background = DarkBackground
val Surface = DarkSurface
val SurfaceVariant = DarkSurfaceVariant
val PrimaryAccent = NeonLime
val SecondaryAccent = SoftMintGreen
val StatusInactive = GrayMedium
val StatusActive = SoftMintGreen             // Active states use SoftMintGreen
val TextPrimary = TextLight
val TextSecondary = TextSecondaryLight
val TextTertiary = TextTertiaryLight
val AccentLime = NeonLime                    // Mapped to NeonLime

val ErrorColor = StatusError
val SuccessColor = StatusSuccess
val WarningColor = StatusWarning
val InfoColor = StatusInfo
