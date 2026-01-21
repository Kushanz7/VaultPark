package com.kushan.vaultpark.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kushan.vaultpark.model.InvoiceNew
import com.kushan.vaultpark.model.User
import com.kushan.vaultpark.ui.screens.BillingScreen
import com.kushan.vaultpark.ui.screens.DriverHistoryScreen
import com.kushan.vaultpark.ui.screens.DriverHomeScreen
import com.kushan.vaultpark.ui.screens.InvoiceDetailsScreen
import com.kushan.vaultpark.ui.screens.ProfileScreen
import com.kushan.vaultpark.ui.screens.SecurityLogsScreen
import com.kushan.vaultpark.ui.screens.SecurityReportsScreen
import com.kushan.vaultpark.ui.screens.SecurityHomeScreen
import com.kushan.vaultpark.ui.screens.notifications.NotificationsScreen
import com.kushan.vaultpark.ui.screens.notifications.NotificationsViewModel
import com.kushan.vaultpark.ui.screens.profile.ChangePasswordScreen
import com.kushan.vaultpark.ui.screens.profile.DriverProfileScreen
import com.kushan.vaultpark.ui.screens.profile.SecurityProfileScreen
import com.kushan.vaultpark.viewmodel.AuthViewModel
import com.kushan.vaultpark.viewmodel.ProfileViewModel

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
            DriverHomeScreen(
                onNavigateToBilling = {
                    navController.navigate(NavScreen.Billing.route)
                },
                onNavigateToHistory = {
                    navController.navigate(NavScreen.History.route)
                },
                onNavigateToSupport = {
                    navController.navigate(NavScreen.Notifications.route)
                }
            )
        }
        
        composable(NavScreen.History.route) {
            DriverHistoryScreen(
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(NavScreen.Billing.route) {
            BillingScreen(
                onBackPressed = {
                    navController.popBackStack()
                },
                onInvoiceSelected = { invoiceId ->
                    navController.navigate(NavScreen.BillingDetails.createRoute(invoiceId))
                }
            )
        }
        
        composable(NavScreen.BillingDetails.route) { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getString("invoiceId") ?: return@composable
            InvoiceDetailsScreen(
                invoice = InvoiceNew(id = invoiceId),
                sessions = emptyList(),
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
                },
                onNavigateToNotifications = {
                    navController.navigate(NavScreen.Notifications.route)
                },
                onNavigateToChangePassword = {
                    navController.navigate(NavScreen.ChangePassword.route)
                },
                onNavigateToDriverProfile = {
                    navController.navigate(NavScreen.DriverProfile.route)
                }
            )
        }
        
        composable(NavScreen.Notifications.route) {
            val notificationsViewModel: NotificationsViewModel = viewModel()
            NotificationsScreen(
                viewModel = notificationsViewModel,
                navController = navController
            )
        }
        
        composable(NavScreen.ChangePassword.route) {
            val profileViewModel: ProfileViewModel = viewModel()
            ChangePasswordScreen(
                viewModel = profileViewModel,
                navController = navController
            )
        }
        
        composable(NavScreen.DriverProfile.route) {
            val profileViewModel: ProfileViewModel = viewModel()
            DriverProfileScreen(
                viewModel = profileViewModel,
                navController = navController,
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
            SecurityHomeScreen(
                onNavigateToLogs = { navController.navigate(NavScreen.Logs.route) },
                onNavigateToReports = { navController.navigate(NavScreen.Reports.route) }
            )
        }
        
        composable(NavScreen.Logs.route) {
            com.kushan.vaultpark.ui.screens.SecurityLogsScreen(
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
                },
                onNavigateToNotifications = {
                    navController.navigate(NavScreen.Notifications.route)
                },
                onNavigateToChangePassword = {
                    navController.navigate(NavScreen.ChangePassword.route)
                },
                onNavigateToSecurityProfile = {
                    navController.navigate(NavScreen.SecurityProfile.route)
                }
            )
        }
        
        composable(NavScreen.Notifications.route) {
            val notificationsViewModel: NotificationsViewModel = viewModel()
            NotificationsScreen(
                viewModel = notificationsViewModel,
                navController = navController
            )
        }
        
        composable(NavScreen.ChangePassword.route) {
            val profileViewModel: ProfileViewModel = viewModel()
            ChangePasswordScreen(
                viewModel = profileViewModel,
                navController = navController
            )
        }
        
        composable(NavScreen.SecurityProfile.route) {
            val profileViewModel: ProfileViewModel = viewModel()
            SecurityProfileScreen(
                viewModel = profileViewModel,
                navController = navController,
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
