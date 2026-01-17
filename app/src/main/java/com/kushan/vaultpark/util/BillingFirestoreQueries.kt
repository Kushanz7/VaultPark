package com.kushan.vaultpark.util

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kushan.vaultpark.model.InvoiceNew
import com.kushan.vaultpark.model.PaymentMethod
import com.kushan.vaultpark.model.PricingTier
import com.kushan.vaultpark.model.ParkingSession
import kotlinx.coroutines.tasks.await
import java.util.Calendar

/**
 * Firestore queries for billing functionality
 */
object BillingFirestoreQueries {
    
    private val db = FirebaseFirestore.getInstance()
    private const val TAG = "BillingFirestoreQueries"
    
    /**
     * Fetch invoice for a specific month and year
     */
    suspend fun fetchInvoiceForMonth(
        driverId: String,
        month: Int,
        year: Int
    ): InvoiceNew? {
        return try {
            val snapshot = db.collection("invoices")
                .whereEqualTo("driverId", driverId)
                .whereEqualTo("month", month)
                .whereEqualTo("year", year)
                .get()
                .await()
            
            if (snapshot.documents.isEmpty()) null
            else snapshot.documents.first().toObject(InvoiceNew::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching invoice for $month/$year", e)
            null
        }
    }
    
    /**
     * Fetch invoice history (last 6 months)
     */
    suspend fun fetchInvoiceHistory(driverId: String, months: Int = 6): List<InvoiceNew> {
        return try {
            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH) + 1
            val currentYear = calendar.get(Calendar.YEAR)
            
            val snapshot = db.collection("invoices")
                .whereEqualTo("driverId", driverId)
                .orderBy("generatedAt", Query.Direction.DESCENDING)
                .limit(months.toLong())
                .get()
                .await()
            
            snapshot.documents.mapNotNull { 
                it.toObject(InvoiceNew::class.java)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching invoice history", e)
            emptyList()
        }
    }
    
    /**
     * Create or update an invoice
     */
    suspend fun saveInvoice(invoice: InvoiceNew): Result<Unit> {
        return try {
            db.collection("invoices")
                .document(invoice.id)
                .set(invoice)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving invoice", e)
            Result.failure(e)
        }
    }
    
    /**
     * Update invoice payment status
     */
    suspend fun updateInvoicePaymentStatus(
        invoiceId: String,
        status: String,
        paymentMethod: String? = null
    ): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "status" to status,
                "paidAt" to com.google.firebase.Timestamp.now()
            )
            if (paymentMethod != null) {
                updates["paymentMethod"] = paymentMethod
            }
            
            db.collection("invoices")
                .document(invoiceId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating invoice payment status", e)
            Result.failure(e)
        }
    }
    
    /**
     * Fetch parking sessions for a specific month and year
     */
    suspend fun fetchSessionsForMonth(
        driverId: String,
        month: Int,
        year: Int
    ): List<ParkingSession> {
        return try {
            val calendar = Calendar.getInstance()
            calendar.set(year, month - 1, 1, 0, 0, 0)
            val monthStart = calendar.timeInMillis
            
            calendar.set(year, month, 1, 0, 0, 0)
            val monthEnd = calendar.timeInMillis
            
            val snapshot = db.collection("parkingSessions")
                .whereEqualTo("driverId", driverId)
                .whereEqualTo("status", "COMPLETED")
                .whereGreaterThanOrEqualTo("entryTime", monthStart)
                .whereLessThan("entryTime", monthEnd)
                .orderBy("entryTime", Query.Direction.DESCENDING)
                .get()
                .await()
            
            snapshot.documents.mapNotNull {
                it.toObject(ParkingSession::class.java)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching sessions for month", e)
            emptyList()
        }
    }
    
    /**
     * Fetch pricing tier for a membership type
     */
    suspend fun fetchPricingTier(membershipType: String): PricingTier? {
        return try {
            val snapshot = db.collection("pricingTiers")
                .document(membershipType.toLowerCase())
                .get()
                .await()
            
            if (snapshot.exists()) {
                snapshot.toObject(PricingTier::class.java)
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching pricing tier", e)
            null
        }
    }
    
    /**
     * Fetch all payment methods for a user
     */
    suspend fun fetchPaymentMethods(userId: String): List<PaymentMethod> {
        return try {
            val snapshot = db.collection("paymentMethods")
                .document(userId)
                .collection("methods")
                .get()
                .await()
            
            snapshot.documents.mapNotNull {
                it.toObject(PaymentMethod::class.java)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching payment methods", e)
            emptyList()
        }
    }
    
    /**
     * Save or update a payment method
     */
    suspend fun savePaymentMethod(
        userId: String,
        paymentMethod: PaymentMethod
    ): Result<Unit> {
        return try {
            db.collection("paymentMethods")
                .document(userId)
                .collection("methods")
                .document(paymentMethod.id)
                .set(paymentMethod)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving payment method", e)
            Result.failure(e)
        }
    }
    
    /**
     * Delete a payment method
     */
    suspend fun deletePaymentMethod(userId: String, methodId: String): Result<Unit> {
        return try {
            db.collection("paymentMethods")
                .document(userId)
                .collection("methods")
                .document(methodId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting payment method", e)
            Result.failure(e)
        }
    }
}
