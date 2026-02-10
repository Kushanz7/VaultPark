package com.kushan.vaultpark.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kushan.vaultpark.model.ParkingSession
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScanResultDialog(
    session: ParkingSession,
    onDismiss: () -> Unit
) {
    val isExit = session.exitTime != null
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(400),
        label = "success_scale"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.surface,
        animationSpec = tween(400),
        label = "bg_color"
    )
    
    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier
                        .width(64.dp)
                        .height(64.dp)
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale
                        )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = if (isExit) "Exit Recorded" else "Entry Recorded",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                InfoRow("Driver", session.driverName.ifEmpty { "Unknown" })
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow("Vehicle", session.vehicleNumber.ifEmpty { "Unknown" })
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow("Gate", session.gateLocation.ifEmpty { "Unknown" })
                
                if (isExit && session.entryTime != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val duration = session.exitTime!! - session.entryTime!!
                    val hours = TimeUnit.MILLISECONDS.toHours(duration)
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60
                    InfoRow("Duration", "${hours}h ${minutes}m")
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Scan Next", color = Color.White)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorDialog(
    errorMessage: String,
    onDismiss: () -> Unit
) {
    val shake by animateFloatAsState(
        targetValue = 0f,
        animationSpec = tween(400),
        label = "error_shake"
    )
    
    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(
                    color = Color(0xFFEF5350).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(24.dp)
                .graphicsLayer(
                    translationX = shake * 5
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Error",
                    tint = Color(0xFFEF5350),
                    modifier = Modifier
                        .width(64.dp)
                        .height(64.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Scan Failed",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = errorMessage,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF5350)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Try Again", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
