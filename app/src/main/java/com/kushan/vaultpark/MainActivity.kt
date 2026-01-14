package com.kushan.vaultpark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.kushan.vaultpark.ui.navigation.NavScreen
import com.kushan.vaultpark.ui.navigation.VaultParkBottomNavigation
import com.kushan.vaultpark.ui.navigation.VaultParkNavHost
import com.kushan.vaultpark.ui.theme.VaultParkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VaultParkTheme {
                VaultParkApp()
            }
        }
    }
}

@Composable
fun VaultParkApp() {
    val navController = rememberNavController()
    var currentRoute by remember { mutableStateOf(NavScreen.Home.route) }

    // Observe navigation changes
    navController.addOnDestinationChangedListener { _, destination, _ ->
        currentRoute = destination.route ?: NavScreen.Home.route
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
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
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            VaultParkNavHost(
                navController = navController,
                startDestination = NavScreen.Home.route
            )
        }
    }
}