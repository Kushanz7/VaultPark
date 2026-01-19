package com.kushan.vaultpark.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import com.kushan.vaultpark.model.DailyTrendData
import com.kushan.vaultpark.model.HourlyData
import com.kushan.vaultpark.ui.theme.PrimaryPurple
import com.kushan.vaultpark.ui.theme.TextLight
import com.kushan.vaultpark.ui.theme.TextSecondaryDark
import com.kushan.vaultpark.ui.theme.TextTertiaryDark
import com.kushan.vaultpark.ui.theme.DarkSurfaceVariant
import com.kushan.vaultpark.util.AnalyticsUtils

/**
 * Bar chart for hourly data (simplified custom implementation)
 */
@Composable
fun HourlyBarChart(
    hourlyData: List<HourlyData>,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    if (hourlyData.isEmpty()) {
        EmptyStateCard(message = "No hourly data available")
        return
    }
    
    val maxValue = hourlyData.maxOfOrNull { it.scanCount } ?: 1
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Y-axis label
        Text(
            text = "Scans",
            color = TextSecondaryDark,
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal
        )
        
        // Chart bars
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            hourlyData.forEach { data ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(200.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Bar
                    val barHeight = if (maxValue > 0) {
                        ((data.scanCount.toFloat() / maxValue) * 180).dp
                    } else {
                        0.dp
                    }
                    
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(barHeight)
                            .background(
                                color = PrimaryPurple,
                                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                            )
                    )
                }
            }
        }
        
        // X-axis labels
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            hourlyData.forEach { data ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = AnalyticsUtils.formatHour(data.hour),
                        color = TextSecondaryDark,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}


/**
 * Line chart for daily trend data (simplified custom implementation)
 */
@Composable
fun DailyTrendLineChart(
    dailyTrendData: List<DailyTrendData>,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    if (dailyTrendData.isEmpty()) {
        EmptyStateCard(message = "No trend data available")
        return
    }
    
    val maxValue = dailyTrendData.maxOfOrNull { it.scanCount } ?: 1
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Y-axis label
        Text(
            text = "Scans",
            color = TextSecondaryDark,
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal
        )
        
        // Chart area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Draw simplified line chart
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                if (dailyTrendData.size > 1) {
                    val points = dailyTrendData.mapIndexed { index, data ->
                        val x = (index.toFloat() / (dailyTrendData.size - 1)) * size.width
                        val y = size.height - ((data.scanCount.toFloat() / maxValue) * size.height)
                        Offset(x, y)
                    }
                    
                    // Draw line
                    for (i in 0 until points.size - 1) {
                        drawLine(
                            color = PrimaryPurple,
                            start = points[i],
                            end = points[i + 1],
                            strokeWidth = 3f
                        )
                    }
                    
                    // Draw points
                    points.forEach { point ->
                        drawCircle(
                            color = PrimaryPurple,
                            radius = 6f,
                            center = point
                        )
                    }
                }
            }
        }
        
        // X-axis labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            dailyTrendData.forEachIndexed { index, data ->
                if (index % maxOf(1, dailyTrendData.size / 5) == 0 || index == dailyTrendData.size - 1) {
                    Text(
                        text = AnalyticsUtils.formatShortDate(data.date),
                        color = TextSecondaryDark,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}


/**
 * Simple pie chart for entry/exit ratio (text-based)
 */
@Composable
fun EntryExitRatioChart(
    entries: Int,
    exits: Int,
    modifier: Modifier = Modifier
) {
    val total = entries + exits
    
    if (total == 0) {
        EmptyStateCard(message = "No entry/exit data")
        return
    }
    
    val entryPercentage = (entries.toFloat() / total * 100).toInt()
    val exitPercentage = (exits.toFloat() / total * 100).toInt()
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Percentage text
        Text(
            text = "$entryPercentage% / $exitPercentage%",
            color = PrimaryPurple,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        
        // Color bars
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Entry bar (green)
            Box(
                modifier = Modifier
                    .weight(entryPercentage.toFloat())
                    .height(8.dp)
                    .background(
                        color = Color(0xFF50C878),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
            
            // Exit bar (red)
            Box(
                modifier = Modifier
                    .weight(exitPercentage.toFloat())
                    .height(8.dp)
                    .background(
                        color = Color(0xFFFF6B6B),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
        
        // Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$entries Entries",
                color = Color(0xFF50C878),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "$exits Exits",
                color = Color(0xFFFF6B6B),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
