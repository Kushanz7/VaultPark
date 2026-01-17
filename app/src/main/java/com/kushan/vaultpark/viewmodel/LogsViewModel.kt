package com.kushan.vaultpark.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushan.vaultpark.data.repository.FirestoreRepository
import com.kushan.vaultpark.model.ParkingSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

/**
 * LogsViewModel
 * Manages security guard scan logs viewing with filtering, pagination, and statistics
 */
class LogsViewModel(
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    companion object {
        private const val TAG = "LogsViewModel"
    }

    // State flows
    private val _scanLogs = MutableStateFlow<List<ParkingSession>>(emptyList())
    val scanLogs: StateFlow<List<ParkingSession>> = _scanLogs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private val _selectedDateFilter = MutableStateFlow(DateFilter.ALL)
    val selectedDateFilter: StateFlow<DateFilter> = _selectedDateFilter.asStateFlow()

    private val _selectedScanTypeFilter = MutableStateFlow(ScanTypeFilter.ALL_SCANS)
    val selectedScanTypeFilter: StateFlow<ScanTypeFilter> = _selectedScanTypeFilter.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _todayScansCount = MutableStateFlow(0)
    val todayScansCount: StateFlow<Int> = _todayScansCount.asStateFlow()

    private val _entriesCount = MutableStateFlow(0)
    val entriesCount: StateFlow<Int> = _entriesCount.asStateFlow()

    private val _exitsCount = MutableStateFlow(0)
    val exitsCount: StateFlow<Int> = _exitsCount.asStateFlow()

    private val _activeNowCount = MutableStateFlow(0)
    val activeNowCount: StateFlow<Int> = _activeNowCount.asStateFlow()

    private var currentPage = 0
    private val pageSize = 30

    enum class DateFilter {
        ALL, TODAY, THIS_WEEK, THIS_MONTH
    }

    enum class ScanTypeFilter {
        ALL_SCANS, ENTRY_ONLY, EXIT_ONLY
    }

    /**
     * Fetch scan logs for the security guard
     */
    fun fetchScanLogs(guardId: String, dateFilter: DateFilter = DateFilter.ALL) {
        viewModelScope.launch {
            try {
                android.util.Log.d("LogsViewModel", "Fetching logs for guard: $guardId with filter: $dateFilter")
                _isLoading.value = true
                _errorMessage.value = null
                _selectedDateFilter.value = dateFilter
                currentPage = 0

                val (startTime, endTime) = getFilterDateRange(dateFilter)
                android.util.Log.d("LogsViewModel", "Date range: start=$startTime, end=$endTime")

                val logs = firestoreRepository.getParkingSessionsByGuard(
                    guardId = guardId,
                    limit = pageSize,
                    startTime = startTime,
                    endTime = endTime
                )

                android.util.Log.d("LogsViewModel", "Fetched ${logs.size} logs")
                _scanLogs.value = logs
                _hasMore.value = logs.size >= pageSize
                currentPage = 1

                // Update statistics
                updateStatistics(guardId)
            } catch (e: Exception) {
                android.util.Log.e("LogsViewModel", "Error fetching logs", e)
                _errorMessage.value = e.message ?: "Failed to fetch logs"
                _scanLogs.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load more logs for pagination
     */
    fun loadMoreLogs(guardId: String) {
        if (_isLoading.value || !_hasMore.value) return

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val (startTime, endTime) = getFilterDateRange(_selectedDateFilter.value)

                val logs = firestoreRepository.getParkingSessionsByGuard(
                    guardId = guardId,
                    limit = pageSize,
                    offset = currentPage * pageSize,
                    startTime = startTime,
                    endTime = endTime
                )

                if (logs.isEmpty()) {
                    _hasMore.value = false
                } else {
                    val currentLogs = _scanLogs.value.toMutableList()
                    currentLogs.addAll(logs)
                    _scanLogs.value = currentLogs
                    _hasMore.value = logs.size >= pageSize
                    currentPage++
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load more logs"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Filter by scan type (entry/exit)
     */
    fun filterByScanType(filter: ScanTypeFilter) {
        _selectedScanTypeFilter.value = filter
    }

    /**
     * Get filtered logs based on scan type
     */
    fun getFilteredLogs(): List<ParkingSession> {
        val logs = _scanLogs.value
        android.util.Log.d("LogsViewModel", "getFilteredLogs: Total logs=${logs.size}, filter=${_selectedScanTypeFilter.value}")

        val filtered = when (_selectedScanTypeFilter.value) {
            ScanTypeFilter.ENTRY_ONLY -> {
                // Sessions with entry time (entry scans)
                logs.filter { it.entryTime > 0 }
            }
            ScanTypeFilter.EXIT_ONLY -> {
                // Sessions with exit time (exit scans)
                logs.filter { it.exitTime != null && it.exitTime > 0 }
            }
            ScanTypeFilter.ALL_SCANS -> logs
        }
        
        android.util.Log.d("LogsViewModel", "getFilteredLogs: Filtered logs=${filtered.size}")
        return filtered
    }

    /**
     * Update statistics for dashboard
     */
    private fun updateStatistics(guardId: String) {
        viewModelScope.launch {
            try {
                // Today's scans
                val todayStart = getTodayStartTime()
                val todayEnd = System.currentTimeMillis()

                val todayLogs = firestoreRepository.getParkingSessionsByGuard(
                    guardId = guardId,
                    limit = 1000,
                    startTime = todayStart,
                    endTime = todayEnd
                )

                _todayScansCount.value = todayLogs.size

                // Count entries and exits
                val entries = todayLogs.count { it.entryTime > 0 }
                val exits = todayLogs.count { it.exitTime != null && it.exitTime > 0 }

                _entriesCount.value = entries
                _exitsCount.value = exits

                // Count active sessions (sessions with entry but no exit)
                val active = todayLogs.count { it.exitTime == null || it.exitTime == 0L }
                _activeNowCount.value = active
            } catch (e: Exception) {
                // Handle error silently for statistics
            }
        }
    }

    /**
     * Calculate date range based on filter
     */
    private fun getFilterDateRange(filter: DateFilter): Pair<Long, Long> {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        return when (filter) {
            DateFilter.ALL -> {
                // Get from 3 months ago
                calendar.apply { add(Calendar.MONTH, -3) }
                Pair(calendar.timeInMillis, now)
            }
            DateFilter.TODAY -> {
                val todayStart = getTodayStartTime()
                Pair(todayStart, now)
            }
            DateFilter.THIS_WEEK -> {
                calendar.apply {
                    set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }
                val weekStart = calendar.timeInMillis
                Pair(weekStart, now)
            }
            DateFilter.THIS_MONTH -> {
                calendar.apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }
                val monthStart = calendar.timeInMillis
                Pair(monthStart, now)
            }
        }
    }

    /**
     * Get today's start time (00:00:00)
     */
    private fun getTodayStartTime(): Long {
        val calendar = Calendar.getInstance()
        calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    /**
     * Calculate parking duration for a session
     */
    fun calculateDuration(session: ParkingSession): String {
        return if (session.exitTime != null && session.exitTime > 0) {
            val durationMs = session.exitTime - session.entryTime
            val hours = durationMs / (1000 * 60 * 60)
            val minutes = (durationMs % (1000 * 60 * 60)) / (1000 * 60)
            when {
                hours > 0 -> "${hours}h ${minutes}m"
                else -> "${minutes}m"
            }
        } else {
            "Ongoing"
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
}
