package com.kushan.vaultpark.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.kushan.vaultpark.model.User
import com.kushan.vaultpark.ui.screens.BillingScreen
import com.kushan.vaultpark.ui.screens.HistoryScreen
import com.kushan.vaultpark.ui.screens.HomeScreen
import com.kushan.vaultpark.ui.screens.ProfileScreen
import com.kushan.vaultpark.ui.screens.SecurityLogsScreen
import com.kushan.vaultpark.ui.screens.SecurityReportsScreen
import com.kushan.vaultpark.ui.screens.SecurityScannerScreen
import com.kushan.vaultpark.viewmodel.AuthViewModel

/**
 * Driver Navigation Graph
 * Contains: Home, History, Billing, Profile
 */
fun NavGraphBuilder.driverNavGraph(
    navController: NavHostController,
    currentUser: User?,
    authViewModel: AuthViewModel
) {
    navigation(
        route = DRIVER_GRAPH,
        startDestination = NavScreen.Home.route
    ) {
        composable(NavScreen.Home.route) {
            HomeScreen(
                currentUser = currentUser,
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
                currentUser = currentUser,
                onBackPressed = {
                    navController.popBackStack()
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(NavScreen.Login.route) {
                        popUpTo(DRIVER_GRAPH) { inclusive = true }
                    }
                }
            )
        }
    }
}

/**
 * Security Navigation Graph
 * Contains: Scanner, Logs, Reports, Profile
 */
fun NavGraphBuilder.securityNavGraph(
    navController: NavHostController,
    currentUser: User?,
    authViewModel: AuthViewModel
) {
    navigation(
        route = SECURITY_GRAPH,
        startDestination = NavScreen.Scanner.route
    ) {
        composable(NavScreen.Scanner.route) {
            SecurityScannerScreen(currentGuardId = currentUser?.id ?: "")
        }
        
        composable(NavScreen.Logs.route) {
            SecurityLogsScreen(
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(NavScreen.Reports.route) {
            SecurityReportsScreen(
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(NavScreen.Profile.route) {
            ProfileScreen(
                currentUser = currentUser,
                onBackPressed = {
                    navController.popBackStack()
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(NavScreen.Login.route) {
                        popUpTo(SECURITY_GRAPH) { inclusive = true }
                    }
                }
            )
        }
    }
}
