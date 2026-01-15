package com.kushan.vaultpark.utils

object DateUtils {
    fun formatDateTime(timestamp: String): String {
        return timestamp  // Simplified for API 24 compatibility
    }

    fun formatTime(timestamp: String): String {
        return timestamp
    }

    fun formatDate(timestamp: String): String {
        return timestamp
    }

    fun getMonthYear(timestamp: String): String {
        return "January 2026"  // Placeholder
    }
}

object CurrencyUtils {
    fun formatCurrency(amount: Double): String {
        return String.format("$%.2f", amount)
    }

    fun parseAmount(amount: String): Double {
        return amount.replace("$", "").toDoubleOrNull() ?: 0.0
    }
}

object ValidationUtils {
    fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }

    fun isValidPhone(phone: String): Boolean {
        return phone.length >= 10 && phone.all { it.isDigit() || it in "-() " }
    }

    fun isNotEmpty(text: String): Boolean {
        return text.isNotBlank()
    }
}
