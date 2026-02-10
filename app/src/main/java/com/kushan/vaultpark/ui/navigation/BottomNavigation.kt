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
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kushan.vaultpark.model.UserRole
import com.kushan.vaultpark.ui.theme.Poppins
import com.kushan.vaultpark.ui.theme.NeonLime
import com.kushan.vaultpark.ui.theme.RoleTheme

enum class BottomNavItem(val icon: ImageVector, val driverLabel: String, val securityLabel: String) {
    Home(Icons.Default.Home, "Home", "Scanner"),
    Map(Icons.Default.Map, "Map", "Handover"),
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
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .padding(horizontal = 8.dp, vertical = 8.dp)
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
                    label = if (userRole == UserRole.SECURITY) item.securityLabel else item.driverLabel,
                    userRole = userRole
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
    label: String,
    userRole: UserRole = UserRole.DRIVER
) {
    val roleColor = if (userRole == UserRole.SECURITY) RoleTheme.securityColor else RoleTheme.driverColor
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) roleColor.copy(alpha = 0.12f) else Color.Transparent,
        animationSpec = tween(200), label = ""
    )
    
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) roleColor else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200), label = ""
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = label,
            modifier = Modifier.size(28.dp),
            tint = iconColor
        )
    }
}
