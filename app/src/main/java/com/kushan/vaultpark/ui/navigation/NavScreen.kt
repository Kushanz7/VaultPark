package com.kushan.vaultpark.ui.navigation

sealed class NavScreen(val route: String) {
    data object Home : NavScreen("home")
    data object History : NavScreen("history")
    data object Billing : NavScreen("billing")
    data object Profile : NavScreen("profile")
}

val navScreens = listOf(
    NavScreen.Home,
    NavScreen.History,
    NavScreen.Billing,
    NavScreen.Profile
)
