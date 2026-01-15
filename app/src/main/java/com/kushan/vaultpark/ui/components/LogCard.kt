package com.kushan.vaultpark.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kushan.vaultpark.model.ParkingSession
import com.kushan.vaultpark.ui.theme.DarkSurface
import com.kushan.vaultpark.ui.theme.NeonLime
import com.kushan.vaultpark.ui.theme.Poppins
import com.kushan.vaultpark.ui.theme.TextLight
import com.kushan.vaultpark.ui.theme.TextSecondaryDark
import com.kushan.vaultpark.ui.theme.TextTertiaryDark
import com.kushan.vaultpark.ui.utils.formatDateTime
import com.kushan.vaultpark.ui.utils.formatDuration

/**
 * LogCard Component
 * Displays a single scan log for security guard
 */
@Composable
fun LogCard(
    session: ParkingSession,
    isEntryOnly: Boolean = false,
    modifier: Modifier = Modifier,
    onCardClick: (ParkingSession) -> Unit = {}
) {
    val interactionSource = MutableInteractionSource()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = DarkSurface,
                shape = MaterialTheme.shapes.medium
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onCardClick(session)
            }
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Driver Name and Scan Type Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = session.driverName,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = TextLight,
                    modifier = Modifier.weight(1f)
                )

                // Scan Type Badge
                val (badgeText, badgeColor) = if (isEntryOnly || session.exitTime == null || session.exitTime == 0L) {
                    "ENTRY" to Color(0xFF4CAF50)
                } else {
                    "EXIT" to Color(0xFFFF6B6B)
                }

                Box(
                    modifier = Modifier
                        .background(
                            color = badgeColor.copy(alpha = 0.2f),
                            shape = MaterialTheme.shapes.extraLarge
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = badgeColor
                    )
                }
            }

            // Vehicle Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = "Vehicle",
                    tint = NeonLime,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = session.vehicleNumber,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = NeonLime
                )
            }

            // Time Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Time",
                    tint = TextSecondaryDark,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = formatDateTime(session.entryTime),
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = TextLight
                )
            }

            // Location Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = TextSecondaryDark,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = session.gateLocation,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = TextSecondaryDark
                )
            }

            // Duration Row (only for EXIT scans)
            if (session.exitTime != null && session.exitTime > 0 && !isEntryOnly) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Duration",
                        tint = Color(0xFFFFB84D),
                        modifier = Modifier.size(16.dp)
                    )
                    val durationMs = session.exitTime - session.entryTime
                    Text(
                        text = "Duration: ${formatDuration(durationMs)}",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = Color(0xFFFFB84D)
                    )
                }
            }

            // Divider
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                color = TextTertiaryDark.copy(alpha = 0.2f),
                thickness = 1.dp
            )
        }
    }
}
