package com.kushan.vaultpark.ui.navigation

sealed class NavScreen(val route: String) {
    // Auth
    data object Login : NavScreen("login")
    data object SignUp : NavScreen("signup")
    
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
    data object ParkingLotsMap : NavScreen("parking_lots_map")
    
    // Security Screens
    data object Scanner : NavScreen("scanner")
    data object Logs : NavScreen("logs")
    data object Reports : NavScreen("reports")
    data object SecurityProfile : NavScreen("security_profile")
    data object SecurityGuardParkingLot : NavScreen("security_guard_parking_lot")
    
// New Admin Tools
    data object ActiveSessions : NavScreen("active_sessions")
    data object HandoverNotes : NavScreen("handover_notes")
    data object ManageUsers : NavScreen("manage_users")
    data object Overdue : NavScreen("overdue")
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
    NavScreen.ActiveSessions,
    NavScreen.HandoverNotes,
    NavScreen.Profile
)

val adminNavScreens = listOf(
    NavScreen.ManageUsers,
    NavScreen.ActiveSessions,
    NavScreen.Reports,
    NavScreen.Profile
)

// Navigation graphs
const val DRIVER_GRAPH = "driver_graph"
const val SECURITY_GRAPH = "security_graph"
