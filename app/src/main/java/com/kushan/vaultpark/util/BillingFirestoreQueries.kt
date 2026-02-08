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
            when {
                e.message?.contains("index", ignoreCase = true) == true -> {
                    Log.e(TAG, "⚠️ Firestore index required! Run: ./gradlew deployFirestoreIndexes", e)
                    Log.e(TAG, "Or create index at Firebase Console: invoices (driverId ASC, generatedAt DESC)")
                }
                else -> Log.e(TAG, "Error fetching invoice history", e)
            }
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
     * Fetch invoice by ID
     */
    suspend fun fetchInvoiceById(invoiceId: String): InvoiceNew? {
        return try {
            db.collection("invoices")
                .document(invoiceId)
                .get()
                .await()
                .toObject(InvoiceNew::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching invoice by ID", e)
            null
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

    /**
     * Fetch all overdue invoices for Admin
     */
    suspend fun fetchOverdueInvoices(): List<InvoiceNew> {
        return try {
            val snapshot = db.collection("invoices")
                .whereEqualTo("isOverdue", true)
                .whereEqualTo("status", "PENDING") // Ensure it's still unpaid
                .orderBy("daysOverdue", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull {
                it.toObject(InvoiceNew::class.java)
            }
        } catch (e: Exception) {
            when {
                e.message?.contains("index", ignoreCase = true) == true -> {
                    Log.e(TAG, "⚠️ Firestore index required! Run: ./gradlew deployFirestoreIndexes", e)
                    Log.e(TAG, "Or create index at Firebase Console: invoices (isOverdue ASC, status ASC, daysOverdue DESC)")
                }
                else -> Log.e(TAG, "Error fetching overdue invoices", e)
            }
            emptyList()
        }
    }

    /**
     * Update invoice with new completed session
     * Creates invoice if it doesn't exist, or updates existing invoice
     */
    suspend fun updateInvoiceWithSession(
        session: ParkingSession,
        driverName: String,
        pricingTier: PricingTier?
    ): Result<Unit> {
        return try {
            if (session.exitTime == null) {
                Log.w(TAG, "Session ${session.id} has no exit time, skipping invoice update")
                return Result.success(Unit)
            }

            // Get the month and year from entry time
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = session.entryTime
            val month = calendar.get(Calendar.MONTH) + 1
            val year = calendar.get(Calendar.YEAR)

            // Fetch or create invoice for the month
            var invoice = fetchInvoiceForMonth(session.driverId, month, year)
            
            if (invoice == null) {
                // Create new invoice
                val invoiceId = "INV-${session.driverId}-$year-${String.format("%02d", month)}"
                invoice = InvoiceNew(
                    id = invoiceId,
                    driverId = session.driverId,
                    driverName = driverName,
                    month = month,
                    year = year,
                    totalSessions = 0,
                    totalHours = 0.0,
                    totalAmount = 0.0,
                    sessionIds = emptyList(),
                    status = "PENDING",
                    dueDate = BillingCalculationUtils.getMonthDueDate(month, year)
                )
            }

            // Check if session already added
            if (invoice.sessionIds.contains(session.id)) {
                Log.d(TAG, "Session ${session.id} already in invoice ${invoice.id}")
                return Result.success(Unit)
            }

            // Calculate session duration in hours
            val durationMs = session.exitTime - session.entryTime
            val durationHours = durationMs / (1000.0 * 60 * 60)

            // Calculate session cost
            val hourlyRate = pricingTier?.hourlyRate ?: 5.0
            val dailyCap = pricingTier?.dailyCap ?: Double.MAX_VALUE
            val baseCost = durationHours * hourlyRate
            val sessionCost = kotlin.math.min(baseCost, dailyCap)

            // Update invoice with new session
            val updatedInvoice = invoice.copy(
                totalSessions = invoice.totalSessions + 1,
                totalHours = invoice.totalHours + durationHours,
                totalAmount = invoice.totalAmount + sessionCost,
                sessionIds = invoice.sessionIds + session.id
            )

            // Save updated invoice
            saveInvoice(updatedInvoice)
            
            Log.d(TAG, "✅ Invoice ${updatedInvoice.id} updated with session ${session.id}. Total: $${updatedInvoice.totalAmount}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating invoice with session", e)
            Result.failure(e)
        }
    }
}
