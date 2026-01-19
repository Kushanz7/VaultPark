package com.kushan.vaultpark.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kushan.vaultpark.ui.theme.PrimaryPurple
import com.kushan.vaultpark.ui.theme.SecondaryGold
import com.kushan.vaultpark.ui.theme.TextLight
import com.kushan.vaultpark.ui.theme.TextSecondaryDark
import com.kushan.vaultpark.ui.theme.DarkSurface
import com.kushan.vaultpark.ui.theme.DarkSurfaceVariant
import com.kushan.vaultpark.ui.theme.StatusSuccess
import com.kushan.vaultpark.ui.theme.TextTertiaryDark
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingUp
import com.kushan.vaultpark.model.TopDriver

/**
 * Metric card for displaying key statistics
 */
@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: @Composable (() -> Unit)? = null,
    trend: String? = null,
    trendColor: Color = StatusSuccess,
    backgroundColor: Color = DarkSurface
) {
    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (icon != null) {
                icon()
            }
            
            Text(
                text = value,
                color = PrimaryPurple,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Default
            )
            
            Text(
                text = label,
                color = TextSecondaryDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
            
            if (trend != null) {
                Text(
                    text = trend,
                    color = trendColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Animated metric card with counter animation
 */
@Composable
fun AnimatedMetricCard(
    modifier: Modifier = Modifier,
    label: String,
    value: Int,
    icon: @Composable (() -> Unit)? = null,
    trend: String? = null,
    trendColor: Color = StatusSuccess
) {
    val animatedValue = remember { Animatable(0f) }
    
    LaunchedEffect(value) {
        animatedValue.animateTo(value.toFloat(), animationSpec = tween(1000))
    }
    
    MetricCard(
        modifier = modifier,
        label = label,
        value = animatedValue.value.toInt().toString(),
        icon = icon,
        trend = trend,
        trendColor = trendColor
    )
}

/**
 * Pulsing metric card for active/live data
 */
@Composable
fun PulsingMetricCard(
    modifier: Modifier = Modifier,
    label: String,
    value: Int,
    icon: @Composable (() -> Unit)? = null
) {
    val scale = remember { Animatable(1f) }
    
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000)
            )
        )
    }
    
    Box(
        modifier = modifier
            .scale(scale.value)
            .background(
                color = DarkSurface,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (icon != null) {
                icon()
            }
            
            Text(
                text = value.toString(),
                color = StatusSuccess,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = label,
                color = TextSecondaryDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

/**
 * Chart card container with title and subtitle
 */
@Composable
fun ChartCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                color = DarkSurface,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                color = TextLight,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = TextTertiaryDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            }
            
            content()
        }
    }
}

/**
 * Top driver list item
 */
@Composable
fun TopDriverItem(
    modifier: Modifier = Modifier,
    rank: Int,
    driver: TopDriver,
    maxVisits: Int,
    onItemClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = DarkSurfaceVariant,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onItemClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Rank badge
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = if (rank == 1) SecondaryGold else PrimaryPurple,
                    shape = RoundedCornerShape(50)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = rank.toString(),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Driver info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = driver.driverName,
                color = TextLight,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            
            Text(
                text = driver.vehicleNumber,
                color = TextSecondaryDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
            
            Text(
                text = "${driver.visitCount} visits · ${String.format("%.1f", driver.totalHours)}h",
                color = PrimaryPurple,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        // Progress bar
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(4.dp)
                .background(
                    color = TextTertiaryDark.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(2.dp)
                )
        ) {
            val progress = if (maxVisits > 0) {
                ((driver.visitCount.toFloat() / maxVisits) * 60).dp
            } else {
                0.dp
            }
            
            Box(
                modifier = Modifier
                    .width(progress)
                    .height(4.dp)
                    .background(
                        color = PrimaryPurple,
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

/**
 * Date range filter chip
 */
@Composable
fun DateRangeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = if (isSelected) PrimaryPurple else DarkSurfaceVariant,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else TextSecondaryDark,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Export button with gradient
 */
@Composable
fun ExportButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(
                color = PrimaryPurple,
                shape = RoundedCornerShape(24.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Export",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = "Export Report as PDF",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Shimmer loading card
 */
@Composable
fun ShimmerLoadingCard(
    modifier: Modifier = Modifier,
    height: Int = 200
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .background(
                color = DarkSurfaceVariant,
                shape = RoundedCornerShape(20.dp)
            )
    )
}

/**
 * Empty state card
 */
@Composable
fun EmptyStateCard(
    modifier: Modifier = Modifier,
    message: String
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                color = DarkSurfaceVariant,
                shape = RoundedCornerShape(20.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Empty",
                tint = TextTertiaryDark,
                modifier = Modifier.size(48.dp)
            )
            
            Text(
                text = message,
                color = TextSecondaryDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
