package com.kushan.vaultpark.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.kushan.vaultpark.ui.theme.PrimaryPurple
import com.kushan.vaultpark.ui.theme.DarkBackground
import com.kushan.vaultpark.ui.theme.DarkSurface
import com.kushan.vaultpark.ui.theme.TextLight
import com.kushan.vaultpark.ui.theme.TextSecondaryDark
import java.util.Calendar

/**
 * Custom date range picker dialog following VaultPark design
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    onDateRangeSelected: (startTime: Long, endTime: Long) -> Unit,
    onDismiss: () -> Unit
) {
    val dateRangePickerState = rememberDateRangePickerState()
    val isValidRange = remember {
        mutableStateOf(true)
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .background(DarkSurface, shape = RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Text(
                    text = "Select Date Range",
                    color = TextLight,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // Date range picker
                DateRangePicker(
                    state = dateRangePickerState,
                    modifier = Modifier.fillMaxWidth(),
                    title = null,
                    headline = null,
                    showModeToggle = false
                )
                
                // Validation message
                if (!isValidRange.value) {
                    Text(
                        text = "Max date range is 90 days",
                        color = Color(0xFFFF6B6B),
                        fontSize = 12.sp
                    )
                }
                
                // Button row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = TextSecondaryDark
                        )
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Medium)
                    }
                    
                    Button(
                        onClick = {
                            val startDate = dateRangePickerState.selectedStartDateMillis ?: return@Button
                            val endDate = dateRangePickerState.selectedEndDateMillis ?: return@Button
                            
                            // Validate date range (max 90 days)
                            val daysDiff = (endDate - startDate) / (1000 * 60 * 60 * 24)
                            if (daysDiff > 90) {
                                isValidRange.value = false
                                return@Button
                            }
                            
                            // Add 1 day to end date for inclusive range
                            val endDateInclusive = endDate + (1000 * 60 * 60 * 24) - 1
                            
                            onDateRangeSelected(startDate, endDateInclusive)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryPurple
                        )
                    ) {
                        Text(
                            "Select",
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
