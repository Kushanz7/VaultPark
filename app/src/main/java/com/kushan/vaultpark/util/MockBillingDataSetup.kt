package com.kushan.vaultpark.util

import com.google.firebase.firestore.FirebaseFirestore
import com.kushan.vaultpark.model.InvoiceNew
import com.kushan.vaultpark.model.PaymentMethod
import com.kushan.vaultpark.model.PricingTier
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.UUID

/**
 * Setup mock data for testing billing functionality
 * Call this once during app initialization if needed
 */
object MockBillingDataSetup {
    
    private val db = FirebaseFirestore.getInstance()
    
    /**
     * Setup all mock billing data
     */
    suspend fun setupMockData(userId: String) {
        setupPricingTiers()
        setupMockInvoices(userId)
        setupMockPaymentMethods(userId)
    }
    
    /**
     * Setup pricing tiers
     */
    private suspend fun setupPricingTiers() {
        try {
            val goldTier = PricingTier(
                membershipType = "Gold",
                hourlyRate = 5.0,
                dailyCap = 40.0,
                monthlyUnlimited = null
            )
            
            val platinumTier = PricingTier(
                membershipType = "Platinum",
                hourlyRate = 4.0,
                dailyCap = 30.0,
                monthlyUnlimited = 200.0
            )
            
            db.collection("pricingTiers")
                .document("gold")
                .set(goldTier)
                .await()
            
            db.collection("pricingTiers")
                .document("platinum")
                .set(platinumTier)
                .await()
        } catch (e: Exception) {
            // Already exists or error setting up
        }
    }
    
    /**
     * Setup mock invoices for last 6 months
     */
    private suspend fun setupMockInvoices(userId: String) {
        try {
            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH) + 1
            val currentYear = calendar.get(Calendar.YEAR)
            
            val invoices = listOf(
                // Current month
                InvoiceNew(
                    id = UUID.randomUUID().toString(),
                    driverId = userId,
                    driverName = "Test Driver",
                    month = currentMonth,
                    year = currentYear,
                    totalSessions = 12,
                    totalHours = 24.5,
                    totalAmount = 125.50,
                    sessionIds = emptyList(),
                    status = "PENDING"
                ),
                // Last month
                InvoiceNew(
                    id = UUID.randomUUID().toString(),
                    driverId = userId,
                    driverName = "Test Driver",
                    month = if (currentMonth == 1) 12 else currentMonth - 1,
                    year = if (currentMonth == 1) currentYear - 1 else currentYear,
                    totalSessions = 10,
                    totalHours = 20.0,
                    totalAmount = 100.0,
                    sessionIds = emptyList(),
                    status = "PAID"
                ),
                // Two months ago
                InvoiceNew(
                    id = UUID.randomUUID().toString(),
                    driverId = userId,
                    driverName = "Test Driver",
                    month = if (currentMonth <= 2) (currentMonth + 10) else (currentMonth - 2),
                    year = if (currentMonth <= 2) currentYear - 1 else currentYear,
                    totalSessions = 15,
                    totalHours = 30.0,
                    totalAmount = 150.0,
                    sessionIds = emptyList(),
                    status = "PAID"
                )
            )
            
            invoices.forEach { invoice ->
                db.collection("invoices")
                    .document(invoice.id)
                    .set(invoice)
                    .await()
            }
        } catch (e: Exception) {
            // Already exists or error
        }
    }
    
    /**
     * Setup mock payment methods
     */
    private suspend fun setupMockPaymentMethods(userId: String) {
        try {
            val methods = listOf(
                PaymentMethod(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    type = "CARD",
                    lastFourDigits = "4242",
                    cardBrand = "Visa",
                    isDefault = true
                ),
                PaymentMethod(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    type = "CARD",
                    lastFourDigits = "5555",
                    cardBrand = "Mastercard",
                    isDefault = false
                )
            )
            
            methods.forEach { method ->
                db.collection("paymentMethods")
                    .document(userId)
                    .collection("methods")
                    .document(method.id)
                    .set(method)
                    .await()
            }
        } catch (e: Exception) {
            // Already exists or error
        }
    }
}
