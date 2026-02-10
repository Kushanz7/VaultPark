package com.kushan.vaultpark.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kushan.vaultpark.model.User
import com.kushan.vaultpark.model.UserRole
import com.kushan.vaultpark.ui.screens.LoginScreen
import com.kushan.vaultpark.ui.screens.SignUpScreen
import com.kushan.vaultpark.viewmodel.AuthViewModel

@Composable
fun VaultParkNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    isAuthenticated: Boolean,
    currentUser: User?
) {
    val startDestination = if (isAuthenticated) {
        when (currentUser?.role) {
            UserRole.DRIVER -> DRIVER_GRAPH
            UserRole.SECURITY -> SECURITY_GRAPH
            else -> NavScreen.Login.route
        }
    } else {
        NavScreen.Login.route
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Login Screen
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
                },
                onNavigateToSignUp = {
                    navController.navigate(NavScreen.SignUp.route)
                }
            )
        }
        
        // Sign Up Screen
        composable(NavScreen.SignUp.route) {
            SignUpScreen(
                authViewModel = authViewModel,
                onSignUpSuccess = { user ->
                    val destination = when (user.role) {
                        UserRole.DRIVER -> DRIVER_GRAPH
                        UserRole.SECURITY -> SECURITY_GRAPH
                    }
                    navController.navigate(destination) {
                        popUpTo(NavScreen.SignUp.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
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
