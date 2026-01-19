package com.kushan.vaultpark.ui.navigation

sealed class NavScreen(val route: String) {
    // Auth
    data object Login : NavScreen("login")
    
    // Driver Screens
    data object Home : NavScreen("home")
    data object History : NavScreen("history")
    data object Billing : NavScreen("billing")
    data object BillingDetails : NavScreen("billing_details/{invoiceId}") {
        fun createRoute(invoiceId: String) = "billing_details/$invoiceId"
    }
    data object Profile : NavScreen("profile")
    data object Notifications : NavScreen("notifications")
    data object ChangePassword : NavScreen("change_password")
    data object DriverProfile : NavScreen("driver_profile")
    
    // Security Screens
    data object Scanner : NavScreen("scanner")
    data object Logs : NavScreen("logs")
    data object Reports : NavScreen("reports")
    data object SecurityProfile : NavScreen("security_profile")
}

val driverNavScreens = listOf(
    NavScreen.Home,
    NavScreen.History,
    NavScreen.Billing,
    NavScreen.Profile
)

val securityNavScreens = listOf(
    NavScreen.Scanner,
    NavScreen.Logs,
    NavScreen.Reports,
    NavScreen.Profile
)

// Navigation graphs
const val DRIVER_GRAPH = "driver_graph"
const val SECURITY_GRAPH = "security_graph"
