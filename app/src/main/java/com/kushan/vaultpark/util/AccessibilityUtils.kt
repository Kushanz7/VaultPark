package com.kushan.vaultpark.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

/**
 * Accessibility utilities for content descriptions
 */
object AccessibilityUtils {

    /**
     * Add semantic content description to composable
     */
    fun Modifier.accessibleClickable(description: String): Modifier {
        return this.semantics(mergeDescendants = true) {
            contentDescription = description
        }
    }

    /**
     * Content description for buttons
     */
    fun getButtonDescription(action: String): String {
        return "Button to $action"
    }

    /**
     * Content description for icons
     */
    fun getIconDescription(iconName: String): String {
        return "$iconName icon"
    }

    /**
     * Content description for input fields
     */
    fun getInputDescription(fieldName: String, isRequired: Boolean = false): String {
        return if (isRequired) {
            "$fieldName input field, required"
        } else {
            "$fieldName input field"
        }
    }

    /**
     * Content description for status indicators
     */
    fun getStatusDescription(status: String): String {
        return "$status status"
    }

    /**
     * Common content descriptions
     */
    object CommonDescriptions {
        const val BACK_BUTTON = "Navigate back to previous screen"
        const val CLOSE_BUTTON = "Close dialog or modal"
        const val LOGOUT_BUTTON = "Sign out from account"
        const val SETTINGS_ICON = "Open settings"
        const val PROFILE_ICON = "Open user profile"
        const val SEARCH_ICON = "Search records"
        const val FILTER_ICON = "Apply filters"
        const val DELETE_ICON = "Delete item"
        const val EDIT_ICON = "Edit item"
        const val LOADING = "Content is loading"
        const val ERROR = "An error occurred"
        const val SUCCESS = "Operation completed successfully"
        const val COPY_TO_CLIPBOARD = "Copy to clipboard"
        const val SHARE = "Share content"
        const val MENU = "Open menu options"
    }
}
