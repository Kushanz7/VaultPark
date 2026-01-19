package com.kushan.vaultpark.util

/**
 * Form validation utilities
 */
object FormValidation {

    /**
     * Validate email format
     */
    fun isValidEmail(email: String): Boolean {
        val emailPattern = Regex(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        )
        return emailPattern.matches(email) && email.length <= 254
    }

    /**
     * Validate phone number format (10 digits)
     */
    fun isValidPhone(phone: String): Boolean {
        val phonePattern = Regex("^[0-9]{10}$")
        return phonePattern.matches(phone.replace(Regex("[^0-9]"), ""))
    }

    /**
     * Validate password strength
     * - Minimum 8 characters
     * - At least one uppercase letter
     * - At least one lowercase letter
     * - At least one digit
     */
    fun getPasswordStrength(password: String): PasswordStrength {
        return when {
            password.length < 8 -> PasswordStrength.WEAK
            !password.any { it.isUpperCase() } -> PasswordStrength.WEAK
            !password.any { it.isLowerCase() } -> PasswordStrength.WEAK
            !password.any { it.isDigit() } -> PasswordStrength.WEAK
            password.length < 12 -> PasswordStrength.MEDIUM
            password.any { it.isLetterOrDigit().not() } -> PasswordStrength.STRONG
            else -> PasswordStrength.STRONG
        }
    }

    /**
     * Validate that passwords match
     */
    fun doPasswordsMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword && password.isNotEmpty()
    }

    /**
     * Validate vehicle number format
     */
    fun isValidVehicleNumber(vehicleNumber: String): Boolean {
        val vehiclePattern = Regex("^[A-Z]{2}[A-Z0-9]{2}[A-Z]{2}[0-9]{4}$")
        return vehiclePattern.matches(vehicleNumber.replace(Regex("[^A-Z0-9]"), "").toUpperCase())
    }

    /**
     * Validate name (minimum 2 characters, no special characters)
     */
    fun isValidName(name: String): Boolean {
        val namePattern = Regex("^[a-zA-Z\\s'-]{2,50}$")
        return namePattern.matches(name.trim())
    }

    /**
     * Validate not empty
     */
    fun isNotEmpty(text: String): Boolean {
        return text.trim().isNotEmpty()
    }

    /**
     * Get error message for email
     */
    fun getEmailErrorMessage(email: String): String? {
        return when {
            email.isEmpty() -> "Email is required"
            !isValidEmail(email) -> "Please enter a valid email address"
            else -> null
        }
    }

    /**
     * Get error message for password
     */
    fun getPasswordErrorMessage(password: String): String? {
        return when {
            password.isEmpty() -> "Password is required"
            password.length < 8 -> "Password must be at least 8 characters"
            !password.any { it.isUpperCase() } -> "Password must contain an uppercase letter"
            !password.any { it.isLowerCase() } -> "Password must contain a lowercase letter"
            !password.any { it.isDigit() } -> "Password must contain a digit"
            else -> null
        }
    }
}

enum class PasswordStrength {
    WEAK, MEDIUM, STRONG
}
