package com.kushan.vaultpark.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kushan.vaultpark.model.User
import com.kushan.vaultpark.ui.components.BadgeStatus
import com.kushan.vaultpark.ui.components.MembershipBadge
import com.kushan.vaultpark.ui.components.ModernCard
import com.kushan.vaultpark.ui.theme.AccentLime
import com.kushan.vaultpark.ui.theme.Poppins
import com.kushan.vaultpark.ui.theme.NeonLime
import com.kushan.vaultpark.ui.theme.StatusActive
import com.kushan.vaultpark.viewmodel.HomeViewModel
import com.kushan.vaultpark.viewmodel.ParkingViewModel
import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import kotlinx.coroutines.delay


@Composable
fun HomeScreen(
    onBackPressed: (() -> Unit)? = null,
    currentUser: User? = null,
    viewModel: HomeViewModel = viewModel(),
    parkingViewModel: ParkingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeSession by parkingViewModel.activeSession.collectAsState()
    val sessionDuration by parkingViewModel.sessionDuration.collectAsState()
    
    var secondsRemaining by remember { mutableIntStateOf(30) }
    var isParked by remember { mutableStateOf(false) }
    var parkingStartTime by remember { mutableStateOf("") }
    var parkingDuration by remember { mutableStateOf("") }
    
    LaunchedEffect(currentUser?.id) {
        currentUser?.id?.let { userId ->
            parkingViewModel.observeActiveSession(userId)
        }
    }
    
    // Countdown timer for QR refresh
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            secondsRemaining = if (secondsRemaining > 1) secondsRemaining - 1 else 30
        }
    }
    
    // Update parking status
    LaunchedEffect(activeSession) {
        isParked = activeSession != null
        if (isParked && activeSession != null) {
            parkingStartTime = "2:30 PM"
            val durationSeconds = ((sessionDuration as? Number)?.toLong() ?: 0L) / 60
            val durationMinutes = ((sessionDuration as? Number)?.toLong() ?: 0L) % 60
            parkingDuration = "${durationSeconds}h ${durationMinutes}m"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Greeting Card
            UserGreetingCard(
                userName = currentUser?.name ?: "Guest",
                vehicleNumber = currentUser?.vehicleNumber ?: "XX-0000",
                membershipType = "Platinum"
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // QR Code Card (Main Focus)
            QRCodeCard(
                secondsRemaining = secondsRemaining,
                qrCodeBitmap = uiState.qrCodeBitmap
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Parking Status Card
            if (isParked) {
                ParkingStatusCard(
                    status = "Parked at Gate A",
                    startTime = parkingStartTime,
                    duration = parkingDuration,
                    isActive = true
                )
            } else {
                ParkingStatusCard(
                    status = "Not Currently Parked",
                    startTime = "",
                    duration = "",
                    isActive = false
                )
            }
        }
    }
}

@Composable
fun UserGreetingCard(
    userName: String,
    vehicleNumber: String,
    membershipType: String,
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Welcome back,",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = Poppins,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = userName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Poppins,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = vehicleNumber,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = Poppins,
                    color = NeonLime
                )
                MembershipBadge(membershipType = membershipType)
            }
        }
    }
}

@Composable
fun QRCodeCard(
    secondsRemaining: Int,
    qrCodeBitmap: android.graphics.Bitmap? = null,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "qr_glow")
    val glowOpacity by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = InfiniteRepeatableSpec(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_opacity"
    )
    
    val textColor by animateColorAsState(
        targetValue = if (secondsRemaining < 5) AccentLime else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(500),
        label = "countdown_color"
    )

    ModernCard(
        modifier = modifier
            .fillMaxWidth()
            .height(420.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Title
            Text(
                text = "Your Access Code",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = Poppins,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // QR Code with Glow Effect
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .border(
                        width = 2.dp,
                        color = NeonLime.copy(alpha = glowOpacity),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                if (qrCodeBitmap != null) {
                    AnimatedContent(
                        targetState = qrCodeBitmap,
                        label = "qr_animation"
                    ) { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier
                                .size(220.dp)
                                .padding(8.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(60.dp),
                        color = NeonLime,
                        strokeWidth = 4.dp
                    )
                }
            }
            
            // Refresh Countdown
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Refreshes in: $secondsRemaining seconds",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = Poppins,
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun ParkingStatusCard(
    status: String,
    startTime: String,
    duration: String,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier
            .fillMaxWidth()
            .height(if (isActive) 130.dp else 100.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Dot
                val dotSize = if (isActive) 12.dp else 8.dp

                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .clip(CircleShape)
                        .background(
                            if (isActive) StatusActive else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                )
                
                Spacer(modifier = Modifier.size(12.dp))
                
                Column {
                    Text(
                        text = status,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = Poppins,
                        color = if (isActive) StatusActive else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (isActive) {
                        Text(
                            text = "Since $startTime · $duration",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = Poppins,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "Scan QR at gate to enter",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = Poppins,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            if (isActive) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "View details",
                    modifier = Modifier.size(20.dp),
                    tint = StatusActive
                )
            }
        }
    }
}

@Composable
fun pulsingScaleAnimation(): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsing_scale")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = InfiniteRepeatableSpec(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    return scale
}
