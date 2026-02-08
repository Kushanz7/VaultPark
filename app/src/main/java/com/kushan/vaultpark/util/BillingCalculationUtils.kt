package com.kushan.vaultpark.util

import com.kushan.vaultpark.model.ParkingSession
import com.kushan.vaultpark.model.PricingTier
import com.kushan.vaultpark.model.InvoiceNew
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.min

/**
 * Utility functions for billing calculations
 */
object BillingCalculationUtils {
    
    /**
     * Calculate the number of hours from entry to exit time, rounded up to nearest 15 minutes
     */
    fun calculateSessionHours(entryTime: Long, exitTime: Long?): Double {
        if (exitTime == null) return 0.0
        val durationMs = exitTime - entryTime
        if (durationMs <= 0) return 0.0
        
        // Convert milliseconds to minutes
        val minutes = durationMs / (1000 * 60)
        // Round up to nearest 15 minutes
        val roundedMinutes = ceil(minutes / 15.0) * 15
        // Convert to hours
        return roundedMinutes / 60
    }
    
    /**
     * Calculate cost for a single parking session based on duration and tier
     */
    fun calculateSessionCost(session: ParkingSession, tier: PricingTier): Double {
        val hours = calculateSessionHours(session.entryTime, session.exitTime ?: 0L)
        return hours * tier.hourlyRate
    }
    
    /**
     * Extract date (without time) from a timestamp
     */
    fun getDateFromTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    
    /**
     * Calculate monthly bill for a list of parking sessions
     * Algorithm:
     * 1. Group sessions by date
     * 2. Calculate cost per day with daily cap applied
     * 3. Sum all days in the month
     * 4. Apply monthly unlimited for Platinum (if applicable)
     */
    fun calculateMonthlyBill(
        sessions: List<ParkingSession>,
        tier: PricingTier
    ): Double {
        if (sessions.isEmpty()) return 0.0
        
        // Group sessions by date
        val sessionsByDate = sessions.groupBy { 
            getDateFromTimestamp(it.entryTime)
        }
        
        // Calculate cost per day with daily cap
        var monthlyTotal = 0.0
        sessionsByDate.forEach { (_, daySessions) ->
            var dayTotal = 0.0
            daySessions.forEach { session ->
                dayTotal += calculateSessionCost(session, tier)
            }
            // Apply daily cap
            monthlyTotal += min(dayTotal, tier.dailyCap)
        }
        
        // Apply monthly unlimited for Platinum
        if (tier.monthlyUnlimited != null) {
            monthlyTotal = min(monthlyTotal, tier.monthlyUnlimited)
        }
        
        return monthlyTotal
    }
    
    /**
     * Get total hours from a list of sessions
     */
    fun calculateTotalHours(sessions: List<ParkingSession>): Double {
        return sessions.sumOf { 
            calculateSessionHours(it.entryTime, it.exitTime ?: 0L)
        }
    }
    
    /**
     * Get the calendar date for a specific month and year
     */
    fun getMonthDueDate(month: Int, year: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 5) // 5th of next month
        return calendar.time
    }
    
    /**
     * Format amount as currency string
     */
    fun formatCurrency(amount: Double): String {
        return String.format(Locale.getDefault(), "$%.2f", amount)
    }
    
    /**
     * Check if amount is within a threshold (for rounding purposes)
     */
    fun isAmountRoundingThreshold(amount: Double, threshold: Double = 0.01): Boolean {
        return amount % 1.0 < threshold
    }
    
    /**
     * Calculate overdue charges for an invoice
     * Policy:
     * - Grace period: 5 days after due date
     * - Rate: 2% per day late
     * - Max Rate: 20% of original amount
     */
    fun calculateOverdueCharges(invoice: InvoiceNew): Double {
        if (invoice.dueDate == null || invoice.status == "PAID") return 0.0
        
        val today = LocalDate.now()
        val dueDate = invoice.dueDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        
        if (today.isAfter(dueDate)) {
            val daysLate = ChronoUnit.DAYS.between(dueDate, today).toInt()
            
            // Grace period of 5 days
            if (daysLate > 5) {
                val chargeableDays = daysLate - 5
                val dailyRate = 0.02 // 2% per day
                val maxRate = 0.20 // 20% maximum
                
                // Calculate rate capped at 20%
                val overdueRate = min(chargeableDays * dailyRate, maxRate)
                
                return invoice.totalAmount * overdueRate
            }
        }
        
        return 0.0
    }
    
    /**
     * Check and update status for an invoice (returns new copy if changed, else null)
     */
    fun checkOverdueStatus(invoice: InvoiceNew): InvoiceNew? {
        if (invoice.status == "PAID" || invoice.dueDate == null) return null
        
        val today = LocalDate.now()
        val dueDate = invoice.dueDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        
        var hasChanges = false
        var updatedInvoice = invoice
        
        // Check if overdue
        if (today.isAfter(dueDate)) {
            val daysLate = ChronoUnit.DAYS.between(dueDate, today).toInt()
            
            if (!invoice.isOverdue) {
                updatedInvoice = updatedInvoice.copy(isOverdue = true)
                hasChanges = true
            }
            
            if (invoice.daysOverdue != daysLate) {
                updatedInvoice = updatedInvoice.copy(daysOverdue = daysLate)
                hasChanges = true
            }
            
            // Calculate charges
            val overdueAmount = calculateOverdueCharges(updatedInvoice)
            // Round to 2 decimal places
            val roundedOverdue = (overdueAmount * 100).toInt() / 100.0
            
            if (invoice.overdueAmount != roundedOverdue) {
                updatedInvoice = updatedInvoice.copy(overdueAmount = roundedOverdue)
                hasChanges = true
            }
        }
        
        return if (hasChanges) updatedInvoice else null
    }
}
