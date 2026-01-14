package com.kushan.vaultpark.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kushan.vaultpark.ui.screens.HomeScreen
import com.kushan.vaultpark.ui.screens.HistoryScreen
import com.kushan.vaultpark.ui.screens.BillingScreen
import com.kushan.vaultpark.ui.screens.ProfileScreen

@Composable
fun VaultParkNavHost(
    navController: NavHostController,
    startDestination: String = NavScreen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(NavScreen.Home.route) {
            HomeScreen(
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(NavScreen.History.route) {
            HistoryScreen(
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(NavScreen.Billing.route) {
            BillingScreen(
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(NavScreen.Profile.route) {
            ProfileScreen(
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }
    }
}
