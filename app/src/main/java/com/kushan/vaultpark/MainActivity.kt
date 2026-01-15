package com.kushan.vaultpark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.kushan.vaultpark.model.UserRole
import com.kushan.vaultpark.ui.navigation.DRIVER_GRAPH
import com.kushan.vaultpark.ui.navigation.SECURITY_GRAPH
import com.kushan.vaultpark.ui.navigation.NavScreen
import com.kushan.vaultpark.ui.navigation.VaultParkBottomNavigation
import com.kushan.vaultpark.ui.navigation.VaultParkNavHost
import com.kushan.vaultpark.ui.navigation.bottomNavItems
import com.kushan.vaultpark.ui.navigation.securityNavScreens
import com.kushan.vaultpark.ui.theme.VaultParkTheme
import com.kushan.vaultpark.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VaultParkTheme {
                VaultParkApp(this)
            }
        }
    }
}

@Composable
fun VaultParkApp(context: MainActivity) {
    val authViewModel: AuthViewModel = viewModel()
    val navController = rememberNavController()
    
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    
    var currentRoute by remember { mutableStateOf("") }

    // Observe navigation changes
    navController.addOnDestinationChangedListener { _, destination, _ ->
        currentRoute = destination.route ?: ""
    }

    // Determine if we should show bottom navigation
    val shouldShowBottomNav = isAuthenticated && 
        (currentRoute == NavScreen.Home.route || 
         currentRoute == NavScreen.History.route || 
         currentRoute == NavScreen.Billing.route || 
         currentRoute == NavScreen.Profile.route ||
         currentRoute == NavScreen.Scanner.route ||
         currentRoute == NavScreen.Logs.route ||
         currentRoute == NavScreen.Reports.route)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (shouldShowBottomNav) {
                val navItems = when (currentUser?.role) {
                    UserRole.DRIVER -> bottomNavItems
                    UserRole.SECURITY -> {
                        listOf(
                            bottomNavItems[0].copy(screen = NavScreen.Scanner), // Home -> Scanner
                            bottomNavItems[1].copy(screen = NavScreen.Logs),    // History -> Logs
                            bottomNavItems[2].copy(screen = NavScreen.Reports), // Billing -> Reports
                            bottomNavItems[3]                                    // Profile stays
                        )
                    }
                    else -> bottomNavItems
                }
                
                VaultParkBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigate = { navScreen ->
                        navController.navigate(navScreen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    items = navItems
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            VaultParkNavHost(
                navController = navController,
                authViewModel = authViewModel,
                isAuthenticated = isAuthenticated,
                currentUser = currentUser
            )
        }
    }
}