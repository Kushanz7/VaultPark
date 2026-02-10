package com.kushan.vaultpark.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kushan.vaultpark.model.User
import com.kushan.vaultpark.model.UserRole
import com.kushan.vaultpark.ui.screens.LoginScreen
import com.kushan.vaultpark.ui.screens.WelcomeFlowScreen
import com.kushan.vaultpark.viewmodel.AuthViewModel

@Composable
fun VaultParkNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    isAuthenticated: Boolean,
    currentUser: User?,
    hasCompletedOnboarding: Boolean
) {
    val startDestination = if (isAuthenticated) {
        when (currentUser?.role) {
            UserRole.DRIVER -> DRIVER_GRAPH
            UserRole.SECURITY -> SECURITY_GRAPH
            else -> NavScreen.Auth.route
        }
    } else {
        NavScreen.Auth.route
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth gate: show welcome flow on first install, otherwise login
        composable(NavScreen.Auth.route) {
            var showLogin by remember { mutableStateOf(hasCompletedOnboarding) }
            LaunchedEffect(hasCompletedOnboarding) { showLogin = hasCompletedOnboarding }
            
            if (showLogin) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onLoginSuccess = { user ->
                        val destination = when (user.role) {
                            UserRole.DRIVER -> DRIVER_GRAPH
                            UserRole.SECURITY -> SECURITY_GRAPH
                            else -> NavScreen.Auth.route
                        }
                        navController.navigate(destination) {
                            popUpTo(NavScreen.Auth.route) { inclusive = true }
                        }
                    }
                )
            } else {
                WelcomeFlowScreen(
                    onComplete = { showLogin = true }
                )
            }
        }
        
        // Login Screen (direct route if needed)
        composable(NavScreen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = { user ->
                    val destination = when (user.role) {
                        UserRole.DRIVER -> DRIVER_GRAPH
                        UserRole.SECURITY -> SECURITY_GRAPH
                    }
                    navController.navigate(destination) {
                        popUpTo(NavScreen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Driver Navigation Graph
        driverNavGraph(
            navController = navController,
            currentUser = currentUser,
            authViewModel = authViewModel
        )
        
        // Security Navigation Graph
        securityNavGraph(
            navController = navController,
            currentUser = currentUser,
            authViewModel = authViewModel
        )
    }
}
