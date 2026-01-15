package com.kushan.vaultpark.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Modern rounded shapes matching the UI design
val VaultParkShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),      // Small elements
    small = RoundedCornerShape(12.dp),          // Chips, small buttons
    medium = RoundedCornerShape(16.dp),         // Input fields, medium cards
    large = RoundedCornerShape(20.dp),          // Large cards
    extraLarge = RoundedCornerShape(28.dp)      // Bottom sheets, dialogs
)

// Specific shape definitions for UI components
val CardShape = RoundedCornerShape(20.dp)           // Card containers
val ButtonShape = RoundedCornerShape(24.dp)         // Primary buttons (very rounded)
val ButtonSmallShape = RoundedCornerShape(16.dp)    // Secondary/small buttons
val InputShape = RoundedCornerShape(16.dp)          // Text input fields

// Enhanced shape definitions for card styles (from MindMirror design)
val MainCardShape = RoundedCornerShape(32.dp)       // Main card (very rounded, soft look)
val GlassCardShape = RoundedCornerShape(16.dp)      // Glass style cards
val OptionCardShape = RoundedCornerShape(12.dp)     // Option/selection cards
val LoginCardShape = RoundedCornerShape(24.dp)      // Login and large cards
val ChipShape = RoundedCornerShape(8.dp)            // Chips and tags
val PillShape = CircleShape                         // Pill/capsule buttons and avatars
val ProgressShape = RoundedCornerShape(50.dp)       // Progress bars (pill style)
val QRCodeShape = RoundedCornerShape(16.dp)         // QR code container
val BottomSheetShape = RoundedCornerShape(
    topStart = 24.dp,
    topEnd = 24.dp,
    bottomStart = 0.dp,
    bottomEnd = 0.dp
)
val DialogShape = RoundedCornerShape(24.dp)         // Dialog containers
