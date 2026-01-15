package com.kushan.vaultpark.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kushan.vaultpark.model.UserRole
import com.kushan.vaultpark.ui.theme.Background
import com.kushan.vaultpark.ui.theme.Poppins
import com.kushan.vaultpark.ui.theme.PrimaryPurple
import com.kushan.vaultpark.ui.theme.Surface
import com.kushan.vaultpark.ui.theme.TextTertiary

enum class BottomNavItem(val icon: ImageVector, val driverLabel: String, val securityLabel: String) {
    Home(Icons.Default.Home, "Home", "Scanner"),
    History(Icons.Default.Schedule, "History", "Logs"),
    Billing(Icons.Default.Receipt, "Billing", "Reports"),
    Profile(Icons.Default.Person, "Profile", "Profile")
}

@Composable
fun NeonDarkBottomNavigation(
    selectedItem: BottomNavItem,
    onItemSelected: (BottomNavItem) -> Unit,
    userRole: UserRole = UserRole.DRIVER,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Surface)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem.entries.forEach { item ->
                BottomNavItemComponent(
                    item = item,
                    isSelected = item == selectedItem,
                    onClick = { onItemSelected(item) },
                    label = if (userRole == UserRole.SECURITY) item.securityLabel else item.driverLabel
                )
            }
        }
    }
}

@Composable
fun BottomNavItemComponent(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    label: String
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryPurple.copy(alpha = 0.2f) else Background,
        animationSpec = tween(200)
    )
    
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryPurple else TextTertiary,
        animationSpec = tween(200)
    )
    
    val textColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryPurple else TextTertiary,
        animationSpec = tween(200)
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = iconColor
        )
        
        Text(
            text = label,
            fontFamily = Poppins,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            color = textColor,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
