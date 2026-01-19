package com.kushan.vaultpark.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kushan.vaultpark.ui.theme.NeonLime
import com.kushan.vaultpark.ui.theme.PrimaryPurple

/**
 * Generic error screen composable
 */
@Composable
fun ErrorScreen(
    icon: ImageVector,
    title: String,
    message: String,
    buttonText: String = "Retry",
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPulsing: Boolean = true
) {
    val transition = rememberInfiniteTransition(label = "pulse_animation")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier
                .size(80.dp)
                .let { if (isPulsing) it.scale(scale) else it },
            tint = PrimaryPurple
        )

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = message,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.8f)
        )

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onButtonClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryPurple
            )
        ) {
            Text(
                text = buttonText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * No Internet Error Screen
 */
@Composable
fun NoInternetErrorScreen(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    ErrorScreen(
        icon = Icons.Outlined.WifiOff,
        title = "No Internet Connection",
        message = "Please check your connection and try again",
        buttonText = "Retry",
        onButtonClick = onRetry,
        modifier = modifier,
        isPulsing = true
    )
}

/**
 * 404 Not Found Error Screen
 */
@Composable
fun NotFoundErrorScreen(
    onGoHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    ErrorScreen(
        icon = Icons.Default.Search,
        title = "Page Not Found",
        message = "The page you're looking for doesn't exist",
        buttonText = "Go Home",
        onButtonClick = onGoHome,
        modifier = modifier,
        isPulsing = false
    )
}

/**
 * Server Error Screen
 */
@Composable
fun ServerErrorScreen(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    ErrorScreen(
        icon = Icons.Default.CloudOff,
        title = "Something Went Wrong",
        message = "We're working to fix this. Please try again later",
        buttonText = "Retry",
        onButtonClick = onRetry,
        modifier = modifier,
        isPulsing = true
    )
}

/**
 * Permission Denied Error Screen
 */
@Composable
fun PermissionDeniedErrorScreen(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    ErrorScreen(
        icon = Icons.Default.Lock,
        title = "Permission Denied",
        message = "VaultPark needs camera access to scan QR codes",
        buttonText = "Open Settings",
        onButtonClick = onOpenSettings,
        modifier = modifier,
        isPulsing = false
    )
}

/**
 * Session Expired Error Screen
 */
@Composable
fun SessionExpiredErrorScreen(
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    ErrorScreen(
        icon = Icons.Default.Schedule,
        title = "Session Expired",
        message = "Please sign in again to continue",
        buttonText = "Sign In",
        onButtonClick = onSignIn,
        modifier = modifier,
        isPulsing = false
    )
}

/**
 * Network-aware content wrapper
 */
@Composable
fun NetworkAwareContent(
    isConnected: Boolean,
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (isConnected) {
        content()
    } else {
        NoInternetErrorScreen(
            onRetry = onRetry,
            modifier = modifier
        )
    }
}
