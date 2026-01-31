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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.kushan.vaultpark.model.UserRole
import com.kushan.vaultpark.ui.navigation.DRIVER_GRAPH
import com.kushan.vaultpark.ui.navigation.SECURITY_GRAPH
import com.kushan.vaultpark.ui.navigation.NavScreen
import com.kushan.vaultpark.ui.navigation.BottomNavItem
import com.kushan.vaultpark.ui.navigation.NeonDarkBottomNavigation
import com.kushan.vaultpark.ui.navigation.VaultParkNavHost
import com.kushan.vaultpark.ui.navigation.securityNavScreens
import com.kushan.vaultpark.ui.theme.VaultParkTheme
import com.kushan.vaultpark.viewmodel.AuthViewModel
import com.kushan.vaultpark.util.DataStoreUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen
        val splashScreen = installSplashScreen()
        
        // Keep splash visible while checking auth
        var keepSplashOnScreen = true
        splashScreen.setKeepOnScreenCondition { keepSplashOnScreen }
        
        super.onCreate(savedInstanceState)
        
        lifecycleScope.launch {
            delay(1500) // Minimum splash duration
            keepSplashOnScreen = false
        }
        
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

         currentRoute == NavScreen.Reports.route ||
         currentRoute == NavScreen.HandoverNotes.route ||
         currentRoute == NavScreen.Notifications.route)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (shouldShowBottomNav) {
                var selectedItem by remember { mutableStateOf(BottomNavItem.Home) }
                
                // Update selected item based on current route
                selectedItem = when (currentRoute) {
                    NavScreen.History.route, NavScreen.Logs.route -> BottomNavItem.History
                    NavScreen.Billing.route, NavScreen.Reports.route -> BottomNavItem.Billing

                    NavScreen.Profile.route -> BottomNavItem.Profile
                    NavScreen.HandoverNotes.route, NavScreen.Notifications.route -> BottomNavItem.Handover
                    else -> BottomNavItem.Home // Home or Scanner
                }
                
                NeonDarkBottomNavigation(
                    selectedItem = selectedItem,
                    userRole = currentUser?.role ?: UserRole.DRIVER,
                    onItemSelected = { item ->
                        val navScreen = when (item) {
                            BottomNavItem.Home -> if (currentUser?.role == UserRole.SECURITY) NavScreen.Scanner else NavScreen.Home
                            BottomNavItem.History -> if (currentUser?.role == UserRole.SECURITY) NavScreen.Logs else NavScreen.History
                            BottomNavItem.Billing -> if (currentUser?.role == UserRole.SECURITY) NavScreen.Reports else NavScreen.Billing

                            BottomNavItem.Profile -> NavScreen.Profile
                            BottomNavItem.Handover -> if (currentUser?.role == UserRole.SECURITY) NavScreen.HandoverNotes else NavScreen.Notifications
                        }
                        navController.navigate(navScreen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
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