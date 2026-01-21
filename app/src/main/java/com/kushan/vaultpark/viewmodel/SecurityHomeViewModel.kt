package com.kushan.vaultpark.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.kushan.vaultpark.model.ParkingSession
import com.kushan.vaultpark.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class TodayStats(
    val totalScans: Int = 0,
    val activeCount: Int = 0,
    val entriesCount: Int = 0,
    val exitsCount: Int = 0
)

data class HourlyActivityData(
    val hour: Int = 0,
    val scans: Int = 0
)

data class SecurityHomeUiState(
    val guard: User? = null,
    val todayStats: TodayStats = TodayStats(),
    val activeSessionsCount: Int = 0,
    val recentScans: List<ParkingSession> = emptyList(),
    val hourlyActivityData: List<HourlyActivityData> = emptyList(),
    val assignedGate: String = "Main Entrance",
    val isShowScannerDialog: Boolean = false,
    val isLoadingStats: Boolean = true,
    val error: String? = null,
    val isRefreshing: Boolean = false
)

class SecurityHomeViewModel(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SecurityHomeUiState())
    val uiState: StateFlow<SecurityHomeUiState> = _uiState.asStateFlow()

    private var activeSesionsListener: ListenerRegistration? = null
    private var scansListener: ListenerRegistration? = null

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val guardId = auth.currentUser?.uid
            if (guardId != null) {
                loadGuardData(guardId)
                listenToActiveSessionsCount()
                fetchTodayStatistics(guardId)
                fetchRecentScans(guardId)
                fetchHourlyActivity(guardId)
            } else {
                _uiState.value = _uiState.value.copy(
                    error = "Guard not authenticated",
                    isLoadingStats = false
                )
            }
        }
    }

    private fun loadGuardData(guardId: String) {
        viewModelScope.launch {
            try {
                val doc = db.collection("users").document(guardId).get()
                    .addOnSuccessListener { snapshot ->
                        val guard = snapshot.toObject(User::class.java)?.copy(id = guardId)
                        _uiState.value = _uiState.value.copy(
                            guard = guard,
                            assignedGate = guard?.vehicleNumber ?: "Main Entrance" // Using vehicleNumber field as gate
                        )
                    }
                    .addOnFailureListener { error ->
                        _uiState.value = _uiState.value.copy(error = "Failed to load guard data: ${error.message}")
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to load guard data: ${e.message}")
            }
        }
    }

    fun listenToActiveSessionsCount() {
        activeSesionsListener?.remove()
        activeSesionsListener = db.collection("parkingSessions")
            .whereEqualTo("status", "ACTIVE")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.value = _uiState.value.copy(error = "Failed to listen to active sessions")
                    return@addSnapshotListener
                }

                val activeCount = snapshot?.size() ?: 0
                _uiState.value = _uiState.value.copy(
                    activeSessionsCount = activeCount,
                    todayStats = _uiState.value.todayStats.copy(activeCount = activeCount)
                )
            }
    }

    fun fetchTodayStatistics(guardId: String) {
        viewModelScope.launch {
            try {
                val startOfDay = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }.timeInMillis

                val endOfDay = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }.timeInMillis

                scansListener?.remove()
                scansListener = db.collection("parkingSessions")
                    .whereEqualTo("scannedByGuardId", guardId)
                    .whereGreaterThanOrEqualTo("entryTime", startOfDay)
                    .whereLessThanOrEqualTo("entryTime", endOfDay)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) return@addSnapshotListener

                        val sessions = snapshot?.toObjects(ParkingSession::class.java) ?: emptyList()
                        val entries = sessions.count { it.entryTime != 0L }
                        val exits = sessions.count { it.exitTime != null }

                        _uiState.value = _uiState.value.copy(
                            todayStats = TodayStats(
                                totalScans = entries + exits,
                                entriesCount = entries,
                                exitsCount = exits,
                                activeCount = _uiState.value.todayStats.activeCount
                            ),
                            isLoadingStats = false
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to fetch today statistics: ${e.message}",
                    isLoadingStats = false
                )
            }
        }
    }

    fun fetchRecentScans(guardId: String) {
        viewModelScope.launch {
            try {
                db.collection("parkingSessions")
                    .whereEqualTo("scannedByGuardId", guardId)
                    .orderBy("entryTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(5)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) return@addSnapshotListener

                        val recentScans = snapshot?.toObjects(ParkingSession::class.java) ?: emptyList()
                        _uiState.value = _uiState.value.copy(recentScans = recentScans)
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to fetch recent scans: ${e.message}")
            }
        }
    }

    fun fetchHourlyActivity(guardId: String) {
        viewModelScope.launch {
            try {
                val startOfDay = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }.timeInMillis

                val endOfDay = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }.timeInMillis

                db.collection("parkingSessions")
                    .whereEqualTo("scannedByGuardId", guardId)
                    .whereGreaterThanOrEqualTo("entryTime", startOfDay)
                    .whereLessThanOrEqualTo("entryTime", endOfDay)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val sessions = snapshot.toObjects(ParkingSession::class.java)
                        val hourlyData = mutableMapOf<Int, Int>()

                        // Initialize all hours with 0
                        for (hour in 0..23) {
                            hourlyData[hour] = 0
                        }

                        // Count scans per hour
                        sessions.forEach { session ->
                            val calendar = Calendar.getInstance()
                            calendar.timeInMillis = session.entryTime
                            val hour = calendar.get(Calendar.HOUR_OF_DAY)
                            hourlyData[hour] = (hourlyData[hour] ?: 0) + 1
                        }

                        // Get last 6 hours
                        val activityList = mutableListOf<HourlyActivityData>()
                        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                        for (i in 6 downTo 1) {
                            val hour = (currentHour - i + 24) % 24
                            activityList.add(
                                HourlyActivityData(
                                    hour = hour,
                                    scans = hourlyData[hour] ?: 0
                                )
                            )
                        }

                        _uiState.value = _uiState.value.copy(hourlyActivityData = activityList)
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to fetch hourly activity: ${e.message}")
            }
        }
    }

    fun showScannerDialog() {
        _uiState.value = _uiState.value.copy(isShowScannerDialog = true)
    }

    fun hideScannerDialog() {
        _uiState.value = _uiState.value.copy(isShowScannerDialog = false)
    }

    fun recordScan(sessionId: String) {
        viewModelScope.launch {
            try {
                val guardId = auth.currentUser?.uid ?: return@launch
                val guardName = _uiState.value.guard?.name ?: "Unknown"

                db.collection("parkingSessions")
                    .document(sessionId)
                    .update(
                        mapOf(
                            "scannedByGuardId" to guardId,
                            "guardName" to guardName,
                            "status" to "ACTIVE"
                        )
                    )
                    .addOnSuccessListener {
                        hideScannerDialog()
                        // Refresh stats
                        val guardId = auth.currentUser?.uid
                        if (guardId != null) {
                            fetchTodayStatistics(guardId)
                            fetchRecentScans(guardId)
                        }
                    }
                    .addOnFailureListener { error ->
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to record scan: ${error.message}"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Error recording scan: ${e.message}")
            }
        }
    }

    fun refreshAllData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            try {
                val guardId = auth.currentUser?.uid
                if (guardId != null) {
                    loadGuardData(guardId)
                    fetchTodayStatistics(guardId)
                    fetchRecentScans(guardId)
                    fetchHourlyActivity(guardId)
                    kotlinx.coroutines.delay(500)
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
        activeSesionsListener?.remove()
        scansListener?.remove()
    }
}
