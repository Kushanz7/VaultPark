package com.kushan.vaultpark.ui.components

import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kushan.vaultpark.ui.theme.DarkSurface
import com.kushan.vaultpark.ui.theme.DarkSurfaceVariant

/**
 * ShimmerCard Component
 * Loading skeleton with animated shimmer effect
 */
@Composable
fun ShimmerCard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = DarkSurface,
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header line
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(20.dp)
                    .background(
                        color = DarkSurfaceVariant,
                        shape = MaterialTheme.shapes.small
                    )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Date line
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.3f)
                    .height(16.dp)
                    .background(
                        color = DarkSurfaceVariant,
                        shape = MaterialTheme.shapes.small
                    )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Time section lines
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(16.dp)
                    .background(
                        color = DarkSurfaceVariant,
                        shape = MaterialTheme.shapes.small
                    )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(16.dp)
                    .background(
                        color = DarkSurfaceVariant,
                        shape = MaterialTheme.shapes.small
                    )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom info line
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(14.dp)
                    .background(
                        color = DarkSurfaceVariant,
                        shape = MaterialTheme.shapes.small
                    )
            )
        }
    }
}
