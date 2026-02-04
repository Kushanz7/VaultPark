package com.kushan.vaultpark.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.kushan.vaultpark.data.repository.FirestoreRepository
import com.kushan.vaultpark.model.ParkingSession
import com.kushan.vaultpark.model.User
import com.kushan.vaultpark.model.PersonalInsights
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class MonthlyStats(
    val sessionsCount: Int = 0,
    val totalHours: Double = 0.0,
    val totalAmount: Double = 0.0
)

// ✨ ENHANCED: UI State with new features
data class DriverHomeUiState(
    val user: User? = null,
    val activeSession: ParkingSession? = null,
    val monthlyStats: MonthlyStats = MonthlyStats(),
    val recentSessions: List<ParkingSession> = emptyList(),
    val upcomingBillingDate: String = "",
    val totalVisits: Int = 0,
    val memberSinceDate: String = "",
    val sessionDurationMinutes: Long = 0,
    val isShowQRDialog: Boolean = false,
    val qrCodeData: String = "",
    val qrCodeImageUrl: String? = null,
    val isLoadingStats: Boolean = true,
    val error: String? = null,
    val isRefreshing: Boolean = false,
    
    val lastThreeSessions: List<ParkingSession> = emptyList(),
    
    // ✨ NEW: Step 3 - Quick Actions Widget
    val quickStats: QuickStats = QuickStats(),
    
    // ✨ NEW: Personal Insights
    val personalInsights: PersonalInsights = PersonalInsights(),
    
    // ✨ NEW: Parking Lot Info
    val selectedParkingLotId: String = "",
    val selectedParkingLotName: String = ""
)

// ✨ NEW: Quick Stats for Home Screen Widget
data class QuickStats(
    val lastParkedDaysAgo: Int = 0,
    val averageParkingTime: String = "0h",
    val mostUsedGate: String = "N/A",
    val thisWeekVisits: Int = 0
)

class DriverHomeViewModel(
    private val firestoreRepository: FirestoreRepository = FirestoreRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DriverHomeUiState())
    val uiState: StateFlow<DriverHomeUiState> = _uiState.asStateFlow()

    private var activeSesionListener: ListenerRegistration? = null
    private var durationCounterJob: Job? = null

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                loadUserData(userId)
                listenToActiveSession(userId)
                fetchMonthlyStats(userId)
                fetchRecentSessions(userId)
                fetchMemberSinceDate(userId)
                // ✨ NEW: Load enhanced features
                loadQuickStats(userId)
                loadPersonalInsights(userId)
            } else {
                _uiState.value = _uiState.value.copy(
                    error = "User not authenticated",
                    isLoadingStats = false
                )
            }
        }
    }

    private fun loadUserData(userId: String) {
        viewModelScope.launch {
            try {
                val user = firestoreRepository.getUserById(userId)
                _uiState.value = _uiState.value.copy(user = user)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to load user: ${e.message}")
            }
        }
    }

    fun listenToActiveSession(userId: String) {
        activeSesionListener?.remove()
        activeSesionListener = db.collection("parkingSessions")
            .whereEqualTo("driverId", userId)
            .whereEqualTo("status", "ACTIVE")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.value = _uiState.value.copy(error = "Failed to listen to sessions")
                    return@addSnapshotListener
                }

                val activeSession = snapshot?.documents?.firstOrNull()
                    ?.toObject(ParkingSession::class.java)

                _uiState.value = _uiState.value.copy(activeSession = activeSession)

                if (activeSession != null) {
                    startDurationCounter(activeSession.entryTime)
                } else {
                    stopDurationCounter()
                }
            }
    }

    private fun startDurationCounter(entryTimeMillis: Long) {
        stopDurationCounter()
        durationCounterJob = viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                val durationMinutes = (now - entryTimeMillis) / (1000 * 60)
                _uiState.value = _uiState.value.copy(sessionDurationMinutes = durationMinutes)
                delay(60000) // Update every minute
            }
        }
    }

    private fun stopDurationCounter() {
        durationCounterJob?.cancel()
        durationCounterJob = null
    }

    fun fetchMonthlyStats(userId: String) {
        viewModelScope.launch {
            try {
                val calendar = Calendar.getInstance()
                val startOfMonth = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }.timeInMillis

                val endOfMonth = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }.timeInMillis

                db.collection("parkingSessions")
                    .whereEqualTo("driverId", userId)
                    .whereGreaterThanOrEqualTo("entryTime", startOfMonth)
                    .whereLessThanOrEqualTo("entryTime", endOfMonth)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) return@addSnapshotListener

                        val sessions = snapshot?.toObjects(ParkingSession::class.java) ?: emptyList()
                        val sessionsCount = sessions.size
                        
                        var totalHours = 0.0
                        sessions.forEach { session ->
                            val exitTime = session.exitTime ?: System.currentTimeMillis()
                            val durationHours = (exitTime - session.entryTime) / (1000.0 * 60 * 60)
                            totalHours += durationHours
                        }

                        val totalAmount = totalHours * 50.0

                        _uiState.value = _uiState.value.copy(
                            monthlyStats = MonthlyStats(
                                sessionsCount = sessionsCount,
                                totalHours = totalHours,
                                totalAmount = totalAmount
                            )
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to fetch monthly stats: ${e.message}")
            }
        }
    }

    fun fetchRecentSessions(userId: String) {
        viewModelScope.launch {
            try {
                db.collection("parkingSessions")
                    .whereEqualTo("driverId", userId)
                    .orderBy("entryTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(3)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) return@addSnapshotListener

                        val recentSessions = snapshot?.toObjects(ParkingSession::class.java) ?: emptyList()
                        _uiState.value = _uiState.value.copy(
                            recentSessions = recentSessions,
                            totalVisits = recentSessions.size,
                            lastThreeSessions = recentSessions
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to fetch recent sessions: ${e.message}")
            }
        }
    }

    private fun fetchMemberSinceDate(userId: String) {
        viewModelScope.launch {
            try {
                val user = firestoreRepository.getUserById(userId)
                if (user != null && user.createdAt != null) {
                    val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(user.createdAt)
                    _uiState.value = _uiState.value.copy(memberSinceDate = year)
                }
                _uiState.value = _uiState.value.copy(isLoadingStats = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to fetch member date: ${e.message}",
                    isLoadingStats = false
                )
            }
        }
    }

    // ============ ✨ NEW: STEP 3 - QUICK STATS WIDGET ============

    private fun loadQuickStats(userId: String) {
        viewModelScope.launch {
            try {
                val sessions = db.collection("parkingSessions")
                    .whereEqualTo("driverId", userId)
                    .orderBy("entryTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(50)
                    .get()
                    .await()
                    .toObjects(ParkingSession::class.java)

                if (sessions.isEmpty()) {
                    _uiState.value = _uiState.value.copy(quickStats = QuickStats())
                    return@launch
                }

                // Last parked days ago
                val lastSession = sessions.firstOrNull()
                val lastParkedDaysAgo = if (lastSession != null) {
                    val daysDiff = (System.currentTimeMillis() - lastSession.entryTime) / (1000 * 60 * 60 * 24)
                    daysDiff.toInt()
                } else 0

                // Average parking time
                val completedSessions = sessions.filter { it.exitTime != null }
                val avgDuration = if (completedSessions.isNotEmpty()) {
                    val totalMinutes = completedSessions.sumOf { 
                        ((it.exitTime ?: 0L) - it.entryTime) / (1000 * 60)
                    }
                    totalMinutes / completedSessions.size
                } else 0L
                
                val avgHours = avgDuration / 60
                val avgMins = avgDuration % 60
                val avgTimeStr = "${avgHours}h ${avgMins}m"

                // Most used gate
                val gateCounts = sessions.groupingBy { it.gateLocation }.eachCount()
                val mostUsedGate = gateCounts.maxByOrNull { it.value }?.key ?: "N/A"

                // This week visits
                val oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
                val thisWeekVisits = sessions.count { it.entryTime >= oneWeekAgo }

                _uiState.value = _uiState.value.copy(
                    quickStats = QuickStats(
                        lastParkedDaysAgo = lastParkedDaysAgo,
                        averageParkingTime = avgTimeStr,
                        mostUsedGate = mostUsedGate,
                        thisWeekVisits = thisWeekVisits
                    )
                )
            } catch (e: Exception) {
                // Silently fail for quick stats
            }
        }
    }

    private fun loadPersonalInsights(userId: String) {
        viewModelScope.launch {
            try {
                val sessions = db.collection("parkingSessions")
                    .whereEqualTo("driverId", userId)
                    .get()
                    .await()
                    .toObjects(ParkingSession::class.java)

                if (sessions.isEmpty()) return@launch

                // Most used gate
                val gateCounts = sessions.groupingBy { it.gateLocation }.eachCount()
                val mostUsedGateEntry = gateCounts.maxByOrNull { it.value }
                
                // Calculate insights...
                val insights = PersonalInsights(
                    mostUsedGate = mostUsedGateEntry?.key ?: "",
                    mostUsedGateCount = mostUsedGateEntry?.value ?: 0
                    // Add more calculations here
                )
                
                _uiState.value = _uiState.value.copy(personalInsights = insights)
            } catch (e: Exception) {
                // Silently fail
            }
        }
    }

    // ============ EXISTING METHODS ============

    fun generateQRCode(): String {
        val userId = auth.currentUser?.uid ?: return ""
        val user = _uiState.value.user ?: return ""
        val timestamp = System.currentTimeMillis()
        val parkingLotId = _uiState.value.selectedParkingLotId.ifEmpty { null }
        
        val qrString = com.kushan.vaultpark.utils.QRCodeUtils.generateQRCodeString(
            userId, 
            user.vehicleNumber, 
            timestamp,
            parkingLotId = parkingLotId
        )
        
        val bitmap = com.kushan.vaultpark.utils.QRCodeUtils.generateQRCodeBitmap(qrString, size = 512)
        var imageUrl: String? = null
        
        if (bitmap != null) {
            try {
                val cacheDir = com.kushan.vaultpark.VaultParkApplication.instance.cacheDir
                val file = File(cacheDir, "qr_code_${System.currentTimeMillis()}.png")
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
                imageUrl = "file://" + file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        _uiState.value = _uiState.value.copy(qrCodeData = qrString, qrCodeImageUrl = imageUrl)
        return qrString
    }

    fun showQRDialog() {
        generateQRCode()
        _uiState.value = _uiState.value.copy(isShowQRDialog = true)
        
        viewModelScope.launch {
            while (_uiState.value.isShowQRDialog) {
                delay(30000)
                if (_uiState.value.isShowQRDialog) {
                    generateQRCode()
                }
            }
        }
    }

    fun hideQRDialog() {
        _uiState.value = _uiState.value.copy(isShowQRDialog = false, qrCodeData = "")
    }
    
    fun setSelectedParkingLot(lotId: String, lotName: String) {
        _uiState.value = _uiState.value.copy(
            selectedParkingLotId = lotId,
            selectedParkingLotName = lotName
        )
    }

    fun refreshAllData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            try {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    loadUserData(userId)
                    fetchMonthlyStats(userId)
                    fetchRecentSessions(userId)
                    loadQuickStats(userId)
                    loadPersonalInsights(userId)
                    delay(500)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Refresh failed: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        activeSesionListener?.remove()
        stopDurationCounter()
    }
}