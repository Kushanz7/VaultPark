package com.kushan.vaultpark.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kushan.vaultpark.model.ParkingSession
import com.kushan.vaultpark.model.SessionTag
import com.kushan.vaultpark.model.PersonalInsights
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

// ✨ ENHANCED: History UI State with new features
data class HistoryUiState(
    val parkingSessions: List<ParkingSession> = emptyList(),
    val filteredSessions: List<ParkingSession> = emptyList(),
    val isLoading: Boolean = false,
    val hasMore: Boolean = true,
    val selectedFilter: HistoryViewModel.DateFilter = HistoryViewModel.DateFilter.ALL,
    val totalSessions: Int = 0,
    val totalHours: Double = 0.0,
    val thisMonthAmount: Double = 0.0,
    val errorMessage: String? = null,
    
    // ✨ NEW: Step 2 - Session Notes & Tags
    val selectedTags: Set<SessionTag> = emptySet(),
    val tagDistribution: Map<String, Int> = emptyMap(),
    val showAddNotesDialog: Boolean = false,
    val sessionToEdit: ParkingSession? = null,
    
    // ✨ NEW: Step 4 - Export & Share
    val showExportDialog: Boolean = false,
    val isExporting: Boolean = false,
    
    // ✨ NEW: Personal Insights
    val personalInsights: PersonalInsights = PersonalInsights()
)

class HistoryViewModel : ViewModel() {
    
    private val db = FirebaseFirestore.getInstance()
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()
    
    // Pagination
    private var lastVisible: com.google.firebase.firestore.DocumentSnapshot? = null
    private val pageSize = 20
    
    enum class DateFilter {
        ALL, THIS_MONTH, LAST_MONTH
    }
    
    // ============ EXISTING METHODS (Enhanced) ============
    
    fun fetchParkingSessions(userId: String, filter: DateFilter = DateFilter.ALL) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                selectedFilter = filter,
                errorMessage = null
            )
            
            try {
                var query = db.collection("parkingSessions")
                    .whereEqualTo("driverId", userId)
                
                // Apply date filter
                when (filter) {
                    DateFilter.THIS_MONTH -> {
                        val startOfMonth = getStartOfMonth()
                        query = query.whereGreaterThanOrEqualTo("entryTime", startOfMonth)
                    }
                    DateFilter.LAST_MONTH -> {
                        val (startOfLastMonth, endOfLastMonth) = getLastMonthRange()
                        query = query
                            .whereGreaterThanOrEqualTo("entryTime", startOfLastMonth)
                            .whereLessThan("entryTime", endOfLastMonth)
                    }
                    DateFilter.ALL -> { /* No additional filter */ }
                }
                
                val snapshot = query
                    .orderBy("entryTime", Query.Direction.DESCENDING)
                    .limit(pageSize.toLong())
                    .get()
                    .await()
                
                val sessions = snapshot.toObjects(ParkingSession::class.java)
                lastVisible = snapshot.documents.lastOrNull()
                
                // Calculate stats
                calculateStats(sessions)
                
                // Calculate tag distribution
                calculateTagDistribution(sessions)
                
                // Calculate personal insights
                calculatePersonalInsights(sessions)
                
                // Apply tag filter if any
                val filteredSessions = applyTagFilter(sessions)
                
                _uiState.value = _uiState.value.copy(
                    parkingSessions = sessions,
                    filteredSessions = filteredSessions,
                    isLoading = false,
                    hasMore = sessions.size >= pageSize
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load sessions: ${e.message}"
                )
            }
        }
    }
    
    fun loadMoreSessions(userId: String) {
        if (!_uiState.value.hasMore || _uiState.value.isLoading || lastVisible == null) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                var query = db.collection("parkingSessions")
                    .whereEqualTo("driverId", userId)
                
                // Apply same filter as initial load
                when (_uiState.value.selectedFilter) {
                    DateFilter.THIS_MONTH -> {
                        query = query.whereGreaterThanOrEqualTo("entryTime", getStartOfMonth())
                    }
                    DateFilter.LAST_MONTH -> {
                        val (startOfLastMonth, endOfLastMonth) = getLastMonthRange()
                        query = query
                            .whereGreaterThanOrEqualTo("entryTime", startOfLastMonth)
                            .whereLessThan("entryTime", endOfLastMonth)
                    }
                    DateFilter.ALL -> { /* No filter */ }
                }
                
                val snapshot = query
                    .orderBy("entryTime", Query.Direction.DESCENDING)
                    .startAfter(lastVisible!!)
                    .limit(pageSize.toLong())
                    .get()
                    .await()
                
                val newSessions = snapshot.toObjects(ParkingSession::class.java)
                lastVisible = snapshot.documents.lastOrNull()
                
                val allSessions = _uiState.value.parkingSessions + newSessions
                calculateStats(allSessions)
                calculateTagDistribution(allSessions)
                
                val filteredSessions = applyTagFilter(allSessions)
                
                _uiState.value = _uiState.value.copy(
                    parkingSessions = allSessions,
                    filteredSessions = filteredSessions,
                    isLoading = false,
                    hasMore = newSessions.size >= pageSize
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load more: ${e.message}"
                )
            }
        }
    }
    
    private fun calculateStats(sessions: List<ParkingSession>) {
        val totalSessions = sessions.size
        
        var totalHours = 0.0
        sessions.forEach { session ->
            if (session.exitTime != null) {
                val durationHours = (session.exitTime - session.entryTime) / (1000.0 * 60 * 60)
                totalHours += durationHours
            }
        }
        
        // Calculate this month's amount
        val startOfMonth = getStartOfMonth()
        val thisMonthSessions = sessions.filter { it.entryTime >= startOfMonth }
        var thisMonthHours = 0.0
        thisMonthSessions.forEach { session ->
            if (session.exitTime != null) {
                val durationHours = (session.exitTime - session.entryTime) / (1000.0 * 60 * 60)
                thisMonthHours += durationHours
            }
        }
        val thisMonthAmount = thisMonthHours * 50.0 // $50 per hour
        
        _uiState.value = _uiState.value.copy(
            totalSessions = totalSessions,
            totalHours = totalHours,
            thisMonthAmount = thisMonthAmount
        )
    }
    
    // ============ ✨ NEW: STEP 2 - SESSION NOTES & TAGS ============
    
    fun addSessionNotes(sessionId: String, notes: String, tags: List<String>) {
        viewModelScope.launch {
            try {
                db.collection("parkingSessions")
                    .document(sessionId)
                    .update(
                        mapOf(
                            "notes" to notes,
                            "tags" to tags
                        )
                    )
                    .await()
                
                // Update local state
                val updatedSessions = _uiState.value.parkingSessions.map { session ->
                    if (session.id == sessionId) {
                        session.copy(notes = notes, tags = tags)
                    } else {
                        session
                    }
                }
                
                calculateTagDistribution(updatedSessions)
                val filteredSessions = applyTagFilter(updatedSessions)
                
                _uiState.value = _uiState.value.copy(
                    parkingSessions = updatedSessions,
                    filteredSessions = filteredSessions,
                    showAddNotesDialog = false,
                    sessionToEdit = null
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to add notes: ${e.message}"
                )
            }
        }
    }
    
    fun showAddNotesDialog(session: ParkingSession) {
        _uiState.value = _uiState.value.copy(
            showAddNotesDialog = true,
            sessionToEdit = session
        )
    }
    
    fun hideAddNotesDialog() {
        _uiState.value = _uiState.value.copy(
            showAddNotesDialog = false,
            sessionToEdit = null
        )
    }
    
    fun toggleTagFilter(tag: SessionTag) {
        val currentTags = _uiState.value.selectedTags.toMutableSet()
        
        if (currentTags.contains(tag)) {
            currentTags.remove(tag)
        } else {
            currentTags.add(tag)
        }
        
        _uiState.value = _uiState.value.copy(selectedTags = currentTags)
        
        // Re-apply filter
        val filteredSessions = applyTagFilter(_uiState.value.parkingSessions)
        _uiState.value = _uiState.value.copy(filteredSessions = filteredSessions)
    }
    
    fun clearTagFilters() {
        _uiState.value = _uiState.value.copy(
            selectedTags = emptySet(),
            filteredSessions = _uiState.value.parkingSessions
        )
    }
    
    private fun applyTagFilter(sessions: List<ParkingSession>): List<ParkingSession> {
        val selectedTags = _uiState.value.selectedTags
        
        return if (selectedTags.isEmpty()) {
            sessions
        } else {
            sessions.filter { session ->
                session.tags.any { tagName ->
                    selectedTags.any { it.name == tagName }
                }
            }
        }
    }
    
    private fun calculateTagDistribution(sessions: List<ParkingSession>) {
        val distribution = mutableMapOf<String, Int>()
        
        sessions.forEach { session ->
            session.tags.forEach { tag ->
                distribution[tag] = (distribution[tag] ?: 0) + 1
            }
        }
        
        _uiState.value = _uiState.value.copy(tagDistribution = distribution)
    }
    
    // ============ ✨ NEW: STEP 4 - EXPORT & SHARE ============
    
    fun showExportDialog() {
        _uiState.value = _uiState.value.copy(showExportDialog = true)
    }
    
    fun hideExportDialog() {
        _uiState.value = _uiState.value.copy(showExportDialog = false)
    }
    
    fun exportSessions() {
        _uiState.value = _uiState.value.copy(isExporting = true)
        // Export logic handled in UI component
    }
    
    fun exportComplete() {
        _uiState.value = _uiState.value.copy(
            isExporting = false,
            showExportDialog = false
        )
    }
    
    // ============ PERSONAL INSIGHTS ============
    
    private fun calculatePersonalInsights(sessions: List<ParkingSession>) {
        if (sessions.isEmpty()) return
        
        // Most used gate
        val gateCounts = sessions.groupingBy { it.gateLocation }.eachCount()
        val mostUsedGateEntry = gateCounts.maxByOrNull { it.value }
        
        // Average duration (completed sessions only)
        val completedSessions = sessions.filter { it.exitTime != null }
        val averageDuration = if (completedSessions.isNotEmpty()) {
            completedSessions.map { (it.exitTime!! - it.entryTime) / (1000.0 * 60 * 60) }.average()
        } else 0.0
        
        // Busiest day of week
        val cal = Calendar.getInstance()
        val dayOfWeekCounts = sessions.groupingBy { 
            cal.timeInMillis = it.entryTime
            cal.get(Calendar.DAY_OF_WEEK)
        }.eachCount()
        val busiestDay = dayOfWeekCounts.maxByOrNull { it.value }?.key?.let {
            when (it) {
                Calendar.SUNDAY -> "Sunday"
                Calendar.MONDAY -> "Monday"
                Calendar.TUESDAY -> "Tuesday"
                Calendar.WEDNESDAY -> "Wednesday"
                Calendar.THURSDAY -> "Thursday"
                Calendar.FRIDAY -> "Friday"
                Calendar.SATURDAY -> "Saturday"
                else -> "N/A"
            }
        } ?: "N/A"
        
        // Monthly stats
        val thisMonthStart = getStartOfMonth()
        val lastMonthRange = getLastMonthRange()
        
        val thisMonthSessions = sessions.filter { it.entryTime >= thisMonthStart }
        val lastMonthSessions = sessions.filter { 
            it.entryTime >= lastMonthRange.first && it.entryTime < lastMonthRange.second 
        }
        
        val thisMonthHours = thisMonthSessions
            .filter { it.exitTime != null }
            .sumOf { (it.exitTime!! - it.entryTime) / (1000.0 * 60 * 60) }
        
        val lastMonthHours = lastMonthSessions
            .filter { it.exitTime != null }
            .sumOf { (it.exitTime!! - it.entryTime) / (1000.0 * 60 * 60) }
        
        // Favorite tag
        val tagCounts = sessions.flatMap { it.tags }.groupingBy { it }.eachCount()
        val favoriteTag = tagCounts.maxByOrNull { it.value }?.key ?: ""
        
        val insights = PersonalInsights(
            mostUsedGate = mostUsedGateEntry?.key ?: "N/A",
            mostUsedGateCount = mostUsedGateEntry?.value ?: 0,
            averageDuration = averageDuration,
            busiestDayOfWeek = busiestDay,
            totalHoursThisMonth = thisMonthHours,
            totalHoursLastMonth = lastMonthHours,
            totalAmountThisMonth = thisMonthHours * 50.0,
            totalAmountLastMonth = lastMonthHours * 50.0,
            favoriteTag = favoriteTag,
            tagDistribution = tagCounts
        )
        
        _uiState.value = _uiState.value.copy(personalInsights = insights)
    }
    
    // ============ UTILITY FUNCTIONS ============
    
    private fun getStartOfMonth(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    
    private fun getLastMonthRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        
        // Start of last month
        cal.add(Calendar.MONTH, -1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfLastMonth = cal.timeInMillis
        
        // End of last month (start of this month)
        cal.add(Calendar.MONTH, 1)
        val endOfLastMonth = cal.timeInMillis
        
        return Pair(startOfLastMonth, endOfLastMonth)
    }
    
    // Legacy properties for compatibility
    val parkingSessions: StateFlow<List<ParkingSession>> = 
        MutableStateFlow(_uiState.value.parkingSessions).asStateFlow()
    val isLoading: StateFlow<Boolean> = 
        MutableStateFlow(_uiState.value.isLoading).asStateFlow()
    val hasMore: StateFlow<Boolean> = 
        MutableStateFlow(_uiState.value.hasMore).asStateFlow()
    val selectedFilter: StateFlow<DateFilter> = 
        MutableStateFlow(_uiState.value.selectedFilter).asStateFlow()
    val totalSessions: StateFlow<Int> = 
        MutableStateFlow(_uiState.value.totalSessions).asStateFlow()
    val totalHours: StateFlow<Double> = 
        MutableStateFlow(_uiState.value.totalHours).asStateFlow()
    val thisMonthAmount: StateFlow<Double> = 
        MutableStateFlow(_uiState.value.thisMonthAmount).asStateFlow()
    val errorMessage: StateFlow<String?> = 
        MutableStateFlow(_uiState.value.errorMessage).asStateFlow()
}