package com.kushan.vaultpark.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.height

data class BottomNavItem(
    val screen: NavScreen,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        screen = NavScreen.Home,
        label = "Home",
        icon = Icons.Filled.Home
    ),
    BottomNavItem(
        screen = NavScreen.History,
        label = "History",
        icon = Icons.Filled.History
    ),
    BottomNavItem(
        screen = NavScreen.Billing,
        label = "Billing",
        icon = Icons.Filled.Receipt
    ),
    BottomNavItem(
        screen = NavScreen.Profile,
        label = "Profile",
        icon = Icons.Filled.Person
    )
)

@Composable
fun VaultParkBottomNavigation(
    currentRoute: String?,
    onNavigate: (NavScreen) -> Unit
) {
    NavigationBar(
        modifier = androidx.compose.foundation.layout.Modifier.height(80.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.screen.route,
                onClick = { onNavigate(item.screen) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(item.label)
                },
                alwaysShowLabel = true
            )
        }
    }
}
