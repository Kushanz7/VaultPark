package com.kushan.vaultpark.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateUtils {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm a")
    private val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

    fun formatDateTime(dateTime: LocalDateTime): String {
        return dateTime.format(dateTimeFormatter)
    }

    fun formatTime(dateTime: LocalDateTime): String {
        return dateTime.format(timeFormatter)
    }

    fun formatDate(dateTime: LocalDateTime): String {
        return dateTime.format(dateFormatter)
    }

    fun getMonthYear(dateTime: LocalDateTime): String {
        return dateTime.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
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
