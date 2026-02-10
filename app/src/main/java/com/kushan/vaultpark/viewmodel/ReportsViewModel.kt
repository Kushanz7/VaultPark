package com.kushan.vaultpark.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kushan.vaultpark.data.AnalyticsRepository
import com.kushan.vaultpark.model.DailyTrendData
import com.kushan.vaultpark.model.HourlyData
import com.kushan.vaultpark.model.ReportStats
import com.kushan.vaultpark.model.TopDriver
import com.kushan.vaultpark.util.AnalyticsUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class DateRangeFilter {
    TODAY, THIS_WEEK, THIS_MONTH, CUSTOM
}

data class CustomDateRange(
    val startTime: Long = 0,
    val endTime: Long = 0
)

data class ReportsUiState(
    val stats: ReportStats = ReportStats(),
    val hourlyData: List<HourlyData> = emptyList(),
    val dailyTrendData: List<DailyTrendData> = emptyList(),
    val topDrivers: List<TopDriver> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ReportsViewModel : ViewModel() {
    
    private val repository = AnalyticsRepository(FirebaseFirestore.getInstance())
    private val auth = FirebaseAuth.getInstance()
    
    private val _selectedDateRange = MutableStateFlow(DateRangeFilter.TODAY)
    val selectedDateRange: StateFlow<DateRangeFilter> = _selectedDateRange
    
    private val _customDateRange = MutableStateFlow(CustomDateRange())
    val customDateRange: StateFlow<CustomDateRange> = _customDateRange
    
    private val _reportStats = MutableStateFlow(ReportStats())
    val reportStats: StateFlow<ReportStats> = _reportStats
    
    private val _hourlyData = MutableStateFlow<List<HourlyData>>(emptyList())
    val hourlyData: StateFlow<List<HourlyData>> = _hourlyData
    
    private val _dailyTrendData = MutableStateFlow<List<DailyTrendData>>(emptyList())
    val dailyTrendData: StateFlow<List<DailyTrendData>> = _dailyTrendData
    
    private val _topDrivers = MutableStateFlow<List<TopDriver>>(emptyList())
    val topDrivers: StateFlow<List<TopDriver>> = _topDrivers
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    private val _activeNow = MutableStateFlow(0)
    val activeNow: StateFlow<Int> = _activeNow
    
    init {
        fetchReportData(DateRangeFilter.TODAY)
        // Refresh active count every 30 seconds
        viewModelScope.launch {
            while (true) {
                val count = repository.getActiveSessionsCount()
                _activeNow.value = count
                kotlinx.coroutines.delay(30000) // 30 seconds
            }
        }
    }
    
    fun selectDateRange(filter: DateRangeFilter) {
        _selectedDateRange.value = filter
        if (filter != DateRangeFilter.CUSTOM) {
            fetchReportData(filter)
        }
    }
    
    fun setCustomDateRange(startTime: Long, endTime: Long) {
        _customDateRange.value = CustomDateRange(startTime, endTime)
        _selectedDateRange.value = DateRangeFilter.CUSTOM
        fetchReportDataCustom(startTime, endTime)
    }
    
    fun fetchReportData(dateRange: DateRangeFilter) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val (startTime, endTime) = when (dateRange) {
                    DateRangeFilter.TODAY -> AnalyticsUtils.getTodayDateRange()
                    DateRangeFilter.THIS_WEEK -> AnalyticsUtils.getThisWeekDateRange()
                    DateRangeFilter.THIS_MONTH -> AnalyticsUtils.getThisMonthDateRange()
                    DateRangeFilter.CUSTOM -> {
                        val customRange = _customDateRange.value
                        Pair(customRange.startTime, customRange.endTime)
                    }
                }
                
                val guardId = auth.currentUser?.uid
                val sessions = repository.fetchSessionsForDateRange(startTime, endTime, guardId)
                
                calculateAndUpdateMetrics(sessions, startTime, endTime)
                
            } catch (e: Exception) {
                _error.value = "Failed to load analytics: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun fetchReportDataCustom(startTime: Long, endTime: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val guardId = auth.currentUser?.uid
                val sessions = repository.fetchSessionsForDateRange(startTime, endTime, guardId)
                
                calculateAndUpdateMetrics(sessions, startTime, endTime)
                
            } catch (e: Exception) {
                _error.value = "Failed to load analytics: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun calculateAndUpdateMetrics(
        sessions: List<ParkingSession>,
        startTime: Long,
        endTime: Long
    ) {
        // Calculate statistics
        val stats = AnalyticsUtils.calculateStatistics(sessions, startTime, endTime)
        _reportStats.value = stats
        
        // Aggregate by hour
        var hourly = AnalyticsUtils.aggregateByHour(sessions)
        if (hourly.isEmpty()) {
            // Generate zero data for last 24 hours
            val zeroHourly = mutableListOf<HourlyData>()
            val cal = java.util.Calendar.getInstance()
            val currentHour = cal.get(java.util.Calendar.HOUR_OF_DAY)
            for (i in 0..23) {
                // We want to show from 24 hours ago to now, or just 0-23 sorted?
                // Usually charts expect chronological order.
                // Let's generate for 0..23 to match typical daily view, or based on the range.
                // If the range is TODAY, we should definitely show 0..currentHour or 0..23.
                // The issue description says "show Is there is no stat... show 0".
                // Simple approach: 0..23 hours
                 zeroHourly.add(HourlyData(i, 0))
            }
            hourly = zeroHourly
        }
        _hourlyData.value = hourly
        
        // Aggregate daily trend
        var daily = AnalyticsUtils.aggregateDailyTrend(sessions)
        if (daily.isEmpty()) {
            // Generate zero data for last 7 days
            val zeroDaily = mutableListOf<DailyTrendData>()
            val cal = java.util.Calendar.getInstance()
            // Reset to clean date
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            
            // Go back 6 days (total 7 days including today)
            cal.add(java.util.Calendar.DAY_OF_YEAR, -6)
            
            for (i in 0..6) {
                zeroDaily.add(DailyTrendData(
                    date = cal.timeInMillis,
                    scanCount = 0
                ))
                cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
            daily = zeroDaily
        }
        _dailyTrendData.value = daily
        
        // Calculate top drivers
        val drivers = AnalyticsUtils.calculateTopDrivers(sessions)
        _topDrivers.value = drivers
    }
    
    fun refreshData() {
        val currentFilter = _selectedDateRange.value
        if (currentFilter == DateRangeFilter.CUSTOM) {
            val customRange = _customDateRange.value
            fetchReportDataCustom(customRange.startTime, customRange.endTime)
        } else {
            fetchReportData(currentFilter)
        }
    }
    
    fun exportReport(): String {
        // Placeholder for PDF export
        return "Report export feature coming soon"
    }
}

// Import for ParkingSession
private typealias ParkingSession = com.kushan.vaultpark.model.ParkingSession
