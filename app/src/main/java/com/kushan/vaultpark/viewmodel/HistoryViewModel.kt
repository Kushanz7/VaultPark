package com.kushan.vaultpark.viewmodel

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
 * HistoryViewModel
 * Manages driver parking history viewing with filtering, pagination, and statistics
 */
class HistoryViewModel(
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    // State flows
    private val _parkingSessions = MutableStateFlow<List<ParkingSession>>(emptyList())
    val parkingSessions: StateFlow<List<ParkingSession>> = _parkingSessions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private val _selectedFilter = MutableStateFlow(DateFilter.ALL)
    val selectedFilter: StateFlow<DateFilter> = _selectedFilter.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _totalSessions = MutableStateFlow(0)
    val totalSessions: StateFlow<Int> = _totalSessions.asStateFlow()

    private val _totalHours = MutableStateFlow(0L)
    val totalHours: StateFlow<Long> = _totalHours.asStateFlow()

    private val _thisMonthAmount = MutableStateFlow(0.0)
    val thisMonthAmount: StateFlow<Double> = _thisMonthAmount.asStateFlow()

    private var currentPage = 0
    private val pageSize = 20
    private var customDateRange: Pair<Long, Long>? = null
    private var lastLoadedSessionId: String? = null

    enum class DateFilter {
        ALL, THIS_MONTH, LAST_MONTH, CUSTOM_RANGE
    }

    /**
     * Fetch initial parking sessions for the driver
     */
    fun fetchParkingSessions(driverId: String, filter: DateFilter = DateFilter.ALL) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                _selectedFilter.value = filter
                currentPage = 0
                lastLoadedSessionId = null

                val (startTime, endTime) = getFilterDateRange(filter)

                val sessions = firestoreRepository.getParkingSessionsByDriver(
                    driverId = driverId,
                    status = "COMPLETED",
                    limit = pageSize,
                    startTime = startTime,
                    endTime = endTime
                )

                _parkingSessions.value = sessions
                _hasMore.value = sessions.size >= pageSize
                currentPage = 1

                // Update statistics
                updateStatistics(driverId, filter)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to fetch sessions"
                _parkingSessions.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load more sessions for pagination
     */
    fun loadMoreSessions(driverId: String) {
        if (_isLoading.value || !_hasMore.value) return

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val (startTime, endTime) = getFilterDateRange(_selectedFilter.value)

                val sessions = firestoreRepository.getParkingSessionsByDriver(
                    driverId = driverId,
                    status = "COMPLETED",
                    limit = pageSize,
                    offset = currentPage * pageSize,
                    startTime = startTime,
                    endTime = endTime
                )

                if (sessions.isEmpty()) {
                    _hasMore.value = false
                } else {
                    val currentSessions = _parkingSessions.value.toMutableList()
                    currentSessions.addAll(sessions)
                    _parkingSessions.value = currentSessions
                    _hasMore.value = sessions.size >= pageSize
                    currentPage++
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load more sessions"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Set custom date range filter
     */
    fun setCustomDateRange(startTime: Long, endTime: Long) {
        customDateRange = startTime to endTime
        _selectedFilter.value = DateFilter.CUSTOM_RANGE
    }

    /**
     * Update statistics based on selected filter
     */
    private fun updateStatistics(driverId: String, filter: DateFilter) {
        viewModelScope.launch {
            try {
                val (startTime, endTime) = getFilterDateRange(filter)

                // Get all sessions for statistics (not paginated)
                val allSessions = firestoreRepository.getParkingSessionsByDriver(
                    driverId = driverId,
                    status = "COMPLETED",
                    limit = 1000,
                    startTime = startTime,
                    endTime = endTime
                )

                _totalSessions.value = allSessions.size

                // Calculate total hours
                val totalMinutes = allSessions.sumOf { session ->
                    if (session.exitTime != null && session.exitTime > 0) {
                        (session.exitTime - session.entryTime) / 60000
                    } else {
                        0L
                    }
                }
                _totalHours.value = totalMinutes / 60

                // Calculate this month amount
                val thisMonthCalendar = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }
                val monthStartTime = thisMonthCalendar.timeInMillis

                val thisMonthSessions = allSessions.filter { session ->
                    session.entryTime >= monthStartTime
                }
                _thisMonthAmount.value = calculateBillingAmount(thisMonthSessions)
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
                // Get from 1 year ago
                calendar.apply { add(Calendar.YEAR, -1) }
                Pair(calendar.timeInMillis, now)
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
            DateFilter.LAST_MONTH -> {
                calendar.apply {
                    add(Calendar.MONTH, -1)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }
                val lastMonthStart = calendar.timeInMillis

                val nextMonth = Calendar.getInstance().apply {
                    timeInMillis = lastMonthStart
                    add(Calendar.MONTH, 1)
                }
                Pair(lastMonthStart, nextMonth.timeInMillis)
            }
            DateFilter.CUSTOM_RANGE -> {
                customDateRange ?: Pair(now - 30L * 24 * 60 * 60 * 1000, now)
            }
        }
    }

    /**
     * Calculate total parking hours for a period
     */
    fun calculateTotalHoursForPeriod(sessions: List<ParkingSession>): Long {
        val totalMinutes = sessions.sumOf { session ->
            if (session.exitTime != null && session.exitTime > 0) {
                (session.exitTime - session.entryTime) / 60000
            } else {
                0L
            }
        }
        return totalMinutes / 60
    }

    /**
     * Calculate billing amount for sessions
     * Placeholder: $5 per hour
     */
    private fun calculateBillingAmount(sessions: List<ParkingSession>): Double {
        val hourlyRate = 5.0
        val totalHours = calculateTotalHoursForPeriod(sessions).toDouble()
        return totalHours * hourlyRate
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
}
