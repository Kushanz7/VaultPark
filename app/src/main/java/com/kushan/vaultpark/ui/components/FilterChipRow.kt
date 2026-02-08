package com.kushan.vaultpark.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kushan.vaultpark.ui.theme.DarkSurfaceVariant
import com.kushan.vaultpark.ui.theme.NeonLime
import com.kushan.vaultpark.ui.theme.Poppins
import com.kushan.vaultpark.ui.theme.TextSecondaryDark
import com.kushan.vaultpark.ui.theme.TextLight
import com.kushan.vaultpark.ui.theme.SecurityPurple
import androidx.compose.ui.graphics.Color

/**
 * FilterChipRow Component
 * Horizontal scrolling chip selector for filters
 */
@Composable
fun <T> FilterChipRow(
    items: List<T>,
    selectedItem: T,
    itemLabel: (T) -> String,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            FilterChip(
                label = itemLabel(item),
                isSelected = item == selectedItem,
                onClick = { onItemSelected(item) }
            )
        }
    }
}

/**
 * Individual FilterChip
 */
@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    useSecurity: Boolean = false
) {
    val selectedColor = if (useSecurity) SecurityPurple else NeonLime
    
    Box(
        modifier = modifier
            .height(40.dp)
            .background(
                color = if (isSelected) selectedColor else DarkSurfaceVariant,
                shape = MaterialTheme.shapes.extraLarge
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = Poppins,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = if (isSelected) Color.White else TextSecondaryDark
        )
    }
}

/**
 * Multiple Filter Row with chips (for two rows of filters)
 */
@Composable
fun MultiFilterRow(
    label: String,
    items: List<String>,
    selectedItems: Set<String>,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            FilterChip(
                label = item,
                isSelected = item in selectedItems,
                onClick = { onItemSelected(item) }
            )
        }
    }
}
