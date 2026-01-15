package com.kushan.vaultpark.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kushan.vaultpark.ui.theme.NeonLime
import com.kushan.vaultpark.ui.theme.DarkGrey
import com.kushan.vaultpark.ui.theme.StatusActive
import com.kushan.vaultpark.ui.theme.StatusError
import com.kushan.vaultpark.ui.theme.StatusInactive
import com.kushan.vaultpark.ui.theme.TextPrimary
import com.kushan.vaultpark.ui.theme.Poppins
import com.kushan.vaultpark.ui.utils.pulsingScaleAnimation

enum class BadgeStatus {
    ACTIVE, INACTIVE, ERROR
}

/**
 * StatusBadge - Pill-shaped status indicator with optional pulsing animation
 */
@Composable
fun StatusBadge(
    status: BadgeStatus,
    label: String,
    showPulse: Boolean = true,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (status) {
        BadgeStatus.ACTIVE -> StatusActive
        BadgeStatus.INACTIVE -> StatusInactive
        BadgeStatus.ERROR -> StatusError
    }
    
    val scaleState = pulsingScaleAnimation()
    val scale = scaleState.value
    val dotScale = if (showPulse && status == BadgeStatus.ACTIVE) {
        scale
    } else {
        1f
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pulsing dot
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(8.dp * dotScale)
                .clip(CircleShape)
                .background(backgroundColor)
        )
        
        androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
        
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = Poppins,
            color = backgroundColor
        )
    }
}

/**
 * Premium membership badge
 */
@Composable
fun MembershipBadge(
    membershipType: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(NeonLime.copy(alpha = 0.2f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "★ $membershipType",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = Poppins,
            color = NeonLime
        )
    }
}

/**
 * Status indicator dot only (for use in lists)
 */
@Composable
fun StatusDot(
    status: BadgeStatus,
    showPulse: Boolean = true,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (status) {
        BadgeStatus.ACTIVE -> StatusActive
        BadgeStatus.INACTIVE -> StatusInactive
        BadgeStatus.ERROR -> StatusError
    }
    
    val scaleState = pulsingScaleAnimation()
    val scale = scaleState.value
    val dotScale = if (showPulse && status == BadgeStatus.ACTIVE) {
        scale
    } else {
        1f
    }

    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .size(12.dp * dotScale)
            .clip(CircleShape)
            .background(backgroundColor)
    )
}
