package com.kushan.vaultpark.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kushan.vaultpark.ui.theme.DarkSurface
import com.kushan.vaultpark.ui.theme.NeonLime
import com.kushan.vaultpark.ui.theme.Poppins
import com.kushan.vaultpark.ui.theme.SoftMintGreen
import com.kushan.vaultpark.ui.theme.TextLight
import com.kushan.vaultpark.ui.theme.TextSecondaryDark

/**
 * StatCard Component
 * Displays a single statistic with icon, value, and label
 */
@Composable
fun StatCard(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    iconColor: androidx.compose.ui.graphics.Color = NeonLime,
    valueColor: androidx.compose.ui.graphics.Color = NeonLime
) {
    Column(
        modifier = modifier
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = valueColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontFamily = Poppins,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = TextSecondaryDark
        )
    }
}

/**
 * Stats Card Container for horizontal layout of multiple stats
 */
@Composable
fun StatsCardContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = DarkSurface,
                shape = MaterialTheme.shapes.extraLarge
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}

/**
 * Animated Counter for stats values
 */
@Composable
fun AnimatedCounterText(
    value: Int,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight = FontWeight.Bold,
    fontSize: androidx.compose.ui.unit.TextUnit = 24.sp,
    color: androidx.compose.ui.graphics.Color = NeonLime,
    fontFamily: androidx.compose.ui.text.font.FontFamily = Poppins
) {
    Text(
        text = value.toString(),
        modifier = modifier,
        fontFamily = fontFamily,
        fontWeight = fontWeight,
        fontSize = fontSize,
        color = color
    )
}

/**
 * Info Message Card
 */
@Composable
fun InfoCard(
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Info
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = DarkSurface,
                shape = MaterialTheme.shapes.medium
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SoftMintGreen,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = message,
            fontFamily = Poppins,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = TextSecondaryDark
        )
    }
}
