package com.kushan.vaultpark.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.kushan.vaultpark.model.InvoiceNew
import com.kushan.vaultpark.model.PaymentMethod
import com.kushan.vaultpark.model.PricingTier
import com.kushan.vaultpark.util.BillingCalculationUtils
import com.kushan.vaultpark.util.BillingFirestoreQueries
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.UUID

data class BillingUiState(
    val currentInvoice: InvoiceNew? = null,
    val invoiceHistory: List<InvoiceNew> = emptyList(),
    val userPricingTier: PricingTier? = null,
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val selectedInvoice: InvoiceNew? = null,
    val isLoading: Boolean = false,
    val isPaymentProcessing: Boolean = false,
    val error: String? = null,
    val paymentSuccess: Boolean = false,
    val notificationSent: Boolean = false
)

class BillingViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BillingUiState())
    val uiState: StateFlow<BillingUiState> = _uiState.asStateFlow()
    
    private val TAG = "BillingViewModel"
    
    init {
        loadBillingData()
    }
    
    /**
     * Load all billing related data
     */
    private fun loadBillingData() {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                // Fetch current month invoice or create one
                val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                
                var invoice = BillingFirestoreQueries.fetchInvoiceForMonth(
                    userId, currentMonth, currentYear
                )
                
                // If no invoice exists, generate one
                if (invoice == null) {
                    invoice = generateInvoiceForMonth(userId, currentMonth, currentYear)
                } else {
                    // Check for overdue status updates
                    val updatedInvoice = BillingCalculationUtils.checkOverdueStatus(invoice)
                    if (updatedInvoice != null) {
                        BillingFirestoreQueries.saveInvoice(updatedInvoice)
                        invoice = updatedInvoice
                    }
                }
                
                // Fetch invoice history
                val history = BillingFirestoreQueries.fetchInvoiceHistory(userId, 6)
                
                // Fetch user's membership type and pricing tier
                val membershipType = getMembershipType(userId)
                val pricingTier = membershipType?.let {
                    BillingFirestoreQueries.fetchPricingTier(it)
                } ?: getDefaultPricingTier()
                
                // Fetch payment methods
                val methods = BillingFirestoreQueries.fetchPaymentMethods(userId)
                
                _uiState.value = _uiState.value.copy(
                    currentInvoice = invoice,
                    invoiceHistory = history,
                    userPricingTier = pricingTier,
                    paymentMethods = methods,
                    isLoading = false
                )
                
                // Check and send notifications if needed
                if (invoice != null) {
                    checkAndSendNotifications(invoice, userId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading billing data", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load billing data: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Fetch current month invoice or generate if doesn't exist
     */
    fun fetchCurrentMonthInvoice() {
        val userId = auth.currentUser?.uid ?: return
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        
        viewModelScope.launch {
            try {
                var invoice = BillingFirestoreQueries.fetchInvoiceForMonth(
                    userId, currentMonth, currentYear
                )
                
                if (invoice == null) {
                    invoice = generateInvoiceForMonth(userId, currentMonth, currentYear)
                } else {
                     // Check for overdue status updates
                    val updatedInvoice = BillingCalculationUtils.checkOverdueStatus(invoice)
                    if (updatedInvoice != null) {
                        BillingFirestoreQueries.saveInvoice(updatedInvoice)
                        invoice = updatedInvoice
                    }
                }
                
                _uiState.value = _uiState.value.copy(currentInvoice = invoice)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching current month invoice", e)
                _uiState.value = _uiState.value.copy(
                    error = "Failed to fetch invoice: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Fetch invoice history (last 6 months)
     */
    fun fetchInvoiceHistory() {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val history = BillingFirestoreQueries.fetchInvoiceHistory(userId, 6)
                _uiState.value = _uiState.value.copy(
                    invoiceHistory = history,
                    isLoading = false
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching invoice history", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to fetch history: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Generate invoice for a specific month
     */
    private suspend fun generateInvoiceForMonth(
        driverId: String,
        month: Int,
        year: Int
    ): InvoiceNew {
        val pricingTier = _uiState.value.userPricingTier 
            ?: BillingFirestoreQueries.fetchPricingTier("gold")
            ?: getDefaultPricingTier()
        
        // Fetch sessions for the month
        val sessions = BillingFirestoreQueries.fetchSessionsForMonth(driverId, month, year)
        
        // Calculate billing
        val totalAmount = BillingCalculationUtils.calculateMonthlyBill(sessions, pricingTier)
        val totalHours = BillingCalculationUtils.calculateTotalHours(sessions)
        
        // Get driver name from auth
        val driverName = auth.currentUser?.displayName ?: "Driver"
        
        // Create invoice
        val invoice = InvoiceNew(
            id = UUID.randomUUID().toString(),
            driverId = driverId,
            driverName = driverName,
            month = month,
            year = year,
            totalSessions = sessions.size,
            totalHours = totalHours,
            totalAmount = totalAmount,
            sessionIds = sessions.map { it.id },
            status = "PENDING",
            dueDate = BillingCalculationUtils.getMonthDueDate(month, year)
        )
        
        // Save to Firestore
        BillingFirestoreQueries.saveInvoice(invoice)
        
        return invoice
    }
    
    /**
     * Process payment for an invoice
     */
    fun processPayment(invoiceId: String, amount: Double, methodId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isPaymentProcessing = true, error = null)
                
                // Simulate payment processing delay
                delay(2000)
                
                // Update invoice status in Firestore
                val result = BillingFirestoreQueries.updateInvoicePaymentStatus(
                    invoiceId,
                    "PAID",
                    methodId
                )
                
                if (result.isSuccess) {
                    // Update local state
                    val updatedInvoice = _uiState.value.currentInvoice?.copy(
                        status = "PAID",
                        paymentMethod = methodId
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        currentInvoice = updatedInvoice,
                        isPaymentProcessing = false,
                        paymentSuccess = true
                    )
                    
                    // Clear success message after 3 seconds
                    delay(3000)
                    _uiState.value = _uiState.value.copy(paymentSuccess = false)
                } else {
                    throw Exception("Payment update failed")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing payment", e)
                _uiState.value = _uiState.value.copy(
                    isPaymentProcessing = false,
                    error = "Payment failed: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Fetch payment methods
     */
    fun fetchPaymentMethods() {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            try {
                val methods = BillingFirestoreQueries.fetchPaymentMethods(userId)
                _uiState.value = _uiState.value.copy(paymentMethods = methods)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching payment methods", e)
                _uiState.value = _uiState.value.copy(
                    error = "Failed to fetch payment methods: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Fetch invoice by ID from Firestore
     */
    fun fetchInvoiceById(invoiceId: String) {
        viewModelScope.launch {
            try {
                val invoice = BillingFirestoreQueries.fetchInvoiceById(invoiceId)
                _uiState.value = _uiState.value.copy(selectedInvoice = invoice)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to fetch invoice: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Select an invoice for details view
     */
    fun selectInvoice(invoice: InvoiceNew) {
        _uiState.value = _uiState.value.copy(selectedInvoice = invoice)
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Get membership type from user (mock - in real app fetch from Firestore)
     */
    private suspend fun getMembershipType(userId: String): String? {
        return try {
            // In a real implementation, fetch this from Firestore user document
            // For now, return default
            "gold"
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Check and send notifications based on invoice status
     */
    private fun checkAndSendNotifications(invoice: InvoiceNew, userId: String) {
        if (invoice.status == "PAID" || invoice.dueDate == null) return
        
        viewModelScope.launch {
            val today = LocalDate.now()
            val dueDate = invoice.dueDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            val daysUntilDue = ChronoUnit.DAYS.between(today, dueDate).toInt()
            val daysOverdue = ChronoUnit.DAYS.between(dueDate, today).toInt()
            
            // 1. 3 days before due date
            if (daysUntilDue == 3) {
                 com.kushan.vaultpark.notifications.NotificationHelper.sendBillingReminder(
                     userId, invoice.totalAmount, "${invoice.month}/${invoice.year}", invoice.id
                 )
            }
            // 2. On due date
            else if (daysUntilDue == 0) {
                 com.kushan.vaultpark.notifications.NotificationHelper.sendBillingReminder(
                     userId, invoice.totalAmount, "DUE TODAY", invoice.id
                 )
            }
            // 3. Overdue (Daily reminder)
            else if (daysOverdue > 0) {
                 com.kushan.vaultpark.notifications.NotificationHelper.sendOverdueNotification(
                     userId, invoice.totalAmount + invoice.overdueAmount, daysOverdue, invoice.id
                 )
            }
        }
    }
    
    /**
     * Get default pricing tier (Gold)
     */
    private fun getDefaultPricingTier(): PricingTier {
        return PricingTier(
            membershipType = "Gold",
            hourlyRate = 5.0,
            dailyCap = 40.0,
            monthlyUnlimited = null
        )
    }
}
