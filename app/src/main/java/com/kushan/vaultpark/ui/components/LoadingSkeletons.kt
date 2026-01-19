package com.kushan.vaultpark.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Shimmer effect for skeleton loading screens
 */
@Composable
fun shimmerBrush(
    targetValue: Float = 1000f
): Brush {
    val shimmerColors = listOf(
        Color(0xFFE0E0E0).copy(alpha = 0.6f),
        Color(0xFFF5F5F5).copy(alpha = 0.2f),
        Color(0xFFE0E0E0).copy(alpha = 0.6f),
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_animation"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = androidx.compose.ui.geometry.Offset.Zero,
        end = androidx.compose.ui.geometry.Offset(
            x = translateAnimation.value,
            y = translateAnimation.value
        )
    )
}

/**
 * Skeleton loading card for parking sessions
 */
@Composable
fun SessionCardSkeleton(
    modifier: Modifier = Modifier
) {
    val shimmer = shimmerBrush()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Column {
            // Header skeleton
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .background(shimmer, shape = RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Time info skeleton
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(16.dp)
                        .background(shimmer, shape = RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.size(12.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(16.dp)
                        .background(shimmer, shape = RoundedCornerShape(4.dp))
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom info skeleton
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .background(shimmer, shape = RoundedCornerShape(4.dp))
            )
        }
    }
}

/**
 * Skeleton loading for statistics/metric cards
 */
@Composable
fun StatCardSkeleton(
    modifier: Modifier = Modifier
) {
    val shimmer = shimmerBrush()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon area skeleton
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(shimmer, shape = RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Value skeleton
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(20.dp)
                    .background(shimmer, shape = RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Label skeleton
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(14.dp)
                    .background(shimmer, shape = RoundedCornerShape(4.dp))
            )
        }
    }
}

/**
 * Skeleton loading for invoice cards
 */
@Composable
fun InvoiceCardSkeleton(
    modifier: Modifier = Modifier
) {
    val shimmer = shimmerBrush()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(18.dp)
                        .background(shimmer, shape = RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(16.dp)
                        .background(shimmer, shape = RoundedCornerShape(4.dp))
                )
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(shimmer, shape = RoundedCornerShape(8.dp))
            )
        }
    }
}

/**
 * Skeleton loading for profile header
 */
@Composable
fun ProfileHeaderSkeleton(
    modifier: Modifier = Modifier
) {
    val shimmer = shimmerBrush()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar skeleton
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(shimmer, shape = RoundedCornerShape(40.dp))
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Name skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(24.dp)
                .background(shimmer, shape = RoundedCornerShape(4.dp))
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Email skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(16.dp)
                .background(shimmer, shape = RoundedCornerShape(4.dp))
        )
    }
}

/**
 * Loading skeleton list for screens
 */
@Composable
fun LoadingSkeletonList(
    itemCount: Int = 5,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    LazyColumn(
        modifier = modifier
    ) {
        repeat(itemCount) {
            item {
                content()
            }
        }
    }
}
