package com.kushan.vaultpark.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kushan.vaultpark.ui.theme.SurfaceVariant
import com.kushan.vaultpark.ui.theme.TextTertiary

/**
 * Standard spacing values for the design system
 */
object VaultParkSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
}

/**
 * Elegant divider for separating sections
 */
@Composable
fun VaultParkDivider(
    modifier: Modifier = Modifier,
    thickness: androidx.compose.ui.unit.Dp = 1.dp
) {
    Divider(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness),
        color = SurfaceVariant,
        thickness = thickness
    )
}

/**
 * Vertical spacing helper
 */
@Composable
fun VSpacing(space: androidx.compose.ui.unit.Dp) {
    Spacer(modifier = Modifier.height(space))
}

/**
 * Screen-level content padding wrapper
 */
@Composable
fun ScreenContent(
    modifier: Modifier = Modifier,
    horizontalPadding: androidx.compose.ui.unit.Dp = VaultParkSpacing.xl,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
    ) {
        content()
    }
}
