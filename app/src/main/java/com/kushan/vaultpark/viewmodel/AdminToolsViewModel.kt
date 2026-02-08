package com.kushan.vaultpark.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kushan.vaultpark.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

// ============ UI STATE ============

data class AdminToolsUiState(
    // Manual Entry
    val recentDrivers: List<RecentDriver> = emptyList(),
    val savedTemplates: List<ManualEntryTemplate> = emptyList(),
    val driverSearchResults: List<User> = emptyList(),
    
    // Shift Reports
    val currentShiftReport: ShiftReport? = null,
    val pastReports: List<ShiftReport> = emptyList(),
    
    // Active Sessions & Quick Actions
    val activeSessions: List<ParkingSession> = emptyList(),
    val filteredSessions: List<ParkingSession> = emptyList(),
    val currentFilter: ActiveSessionFilter = ActiveSessionFilter.ALL,
    val selectedSessions: Set<String> = emptySet(),
    
    // Shift Notes
    val shiftNotes: List<ShiftNote> = emptyList(),
    val unreadNotesCount: Int = 0,
    
    // Loading states
    val isLoading: Boolean = false,
    val isSavingTemplate: Boolean = false,
    val isGeneratingReport: Boolean = false,
    val isPerformingBulkAction: Boolean = false,
    
    val error: String? = null,
    val successMessage: String? = null
)

class AdminToolsViewModel(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminToolsUiState())
    val uiState: StateFlow<AdminToolsUiState> = _uiState.asStateFlow()

    private val guardId: String?
        get() = auth.currentUser?.uid

    init {
        loadInitialData()
    }

    // ============ STEP 1: MANUAL ENTRY FUNCTIONS ============

    fun loadInitialData() {
        viewModelScope.launch {
            guardId?.let { id ->
                loadRecentDrivers()
                loadSavedTemplates(id)
                loadActiveSessions()
                loadShiftNotes()
            }
        }
    }

    /**
     * Search for drivers by name or vehicle number
     */
    fun searchDrivers(query: String) {
        if (query.length < 2) {
            _uiState.value = _uiState.value.copy(driverSearchResults = emptyList())
            return
        }

        viewModelScope.launch {
            try {
                // Search by name
                val nameResults = db.collection("users")
                    .whereEqualTo("role", UserRole.DRIVER.name)
                    .orderBy("name")
                    .startAt(query)
                    .endAt(query + "\uf8ff")
                    .limit(10)
                    .get()
                    .await()
                    .toObjects(User::class.java)

                // Search by vehicle number
                val vehicleResults = db.collection("users")
                    .whereEqualTo("role", UserRole.DRIVER.name)
                    .orderBy("vehicleNumber")
                    .startAt(query.uppercase())
                    .endAt(query.uppercase() + "\uf8ff")
                    .limit(10)
                    .get()
                    .await()
                    .toObjects(User::class.java)

                // Combine and remove duplicates
                val combined = (nameResults + vehicleResults).distinctBy { it.id }
                
                _uiState.value = _uiState.value.copy(driverSearchResults = combined)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Search failed: ${e.message}")
            }
        }
    }

    /**
     * Load recent drivers for quick selection
     */
    private fun loadRecentDrivers() {
        viewModelScope.launch {
            try {
                val sessions = db.collection("parkingSessions")
                    .orderBy("entryTime", Query.Direction.DESCENDING)
                    .limit(20)
                    .get()
                    .await()
                    .toObjects(ParkingSession::class.java)

                val recentDrivers = sessions
                    .distinctBy { it.driverId }
                    .take(10)
                    .map { session ->
                        RecentDriver(
                            userId = session.driverId,
                            name = session.driverName,
                            vehicleNumber = session.vehicleNumber,
                            lastVisit = session.entryTime
                        )
                    }

                _uiState.value = _uiState.value.copy(recentDrivers = recentDrivers)
            } catch (e: Exception) {
                // Silent fail for recent drivers
            }
        }
    }

    /**
     * Load saved manual entry templates
     */
    private fun loadSavedTemplates(guardId: String) {
        viewModelScope.launch {
            try {
                val templates = db.collection("manualEntryTemplates")
                    .whereEqualTo("guardId", guardId)
                    .orderBy("useCount", Query.Direction.DESCENDING)
                    .limit(10)
                    .get()
                    .await()
                    .toObjects(ManualEntryTemplate::class.java)

                _uiState.value = _uiState.value.copy(savedTemplates = templates)
            } catch (e: Exception) {
                // Silent fail
            }
        }
    }

    /**
     * Create manual parking session entry
     */
    fun createManualEntry(
        driverId: String,
        driverName: String,
        vehicleNumber: String,
        gateLocation: String,
        entryType: String, // "ENTRY" or "EXIT"
        notes: String = ""
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val guardData = guardId?.let { id ->
                    db.collection("users").document(id).get().await().toObject(User::class.java)
                }

                if (entryType == "ENTRY") {
                    // Create new entry session
                    val sessionId = db.collection("parkingSessions").document().id
                    val session = ParkingSession(
                        id = sessionId,
                        driverId = driverId,
                        driverName = driverName,
                        vehicleNumber = vehicleNumber,
                        entryTime = System.currentTimeMillis(),
                        gateLocation = gateLocation,
                        location = gateLocation, // Default to gate location for manual entry
                        scannedByGuardId = guardId,
                        guardName = guardData?.name ?: "Unknown Guard",
                        status = SessionStatus.ACTIVE.name,
                        qrCodeDataUsed = "MANUAL_ENTRY",
                        notes = notes
                    )

                    db.collection("parkingSessions").document(sessionId)
                        .set(session.toMap())
                        .await()

                    _uiState.value = _uiState.value.copy(
                        successMessage = "Entry recorded successfully",
                        isLoading = false
                    )
                } else {
                    // Find and complete active session
                    val activeSession = db.collection("parkingSessions")
                        .whereEqualTo("driverId", driverId)
                        .whereEqualTo("status", SessionStatus.ACTIVE.name)
                        .orderBy("entryTime", Query.Direction.DESCENDING)
                        .limit(1)
                        .get()
                        .await()
                        .toObjects(ParkingSession::class.java)
                        .firstOrNull()

                    if (activeSession != null) {
                        val exitTime = System.currentTimeMillis()
                        val duration = calculateDuration(activeSession.entryTime, exitTime)

                        db.collection("parkingSessions").document(activeSession.id)
                            .update(
                                mapOf(
                                    "exitTime" to exitTime,
                                    "status" to SessionStatus.COMPLETED.name,
                                    "duration" to duration,
                                    "notes" to if (notes.isNotEmpty()) "${activeSession.notes}\nExit: $notes" else activeSession.notes
                                )
                            )
                            .await()

                        _uiState.value = _uiState.value.copy(
                            successMessage = "Exit recorded successfully",
                            isLoading = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            error = "No active session found for this driver",
                            isLoading = false
                        )
                    }
                }

                loadActiveSessions()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to create manual entry: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    /**
     * Save driver as template for future quick entry
     */
    fun saveAsTemplate(
        driverName: String,
        vehicleNumber: String,
        defaultGate: String,
        notes: String
    ) {
        viewModelScope.launch {
            guardId?.let { id ->
                _uiState.value = _uiState.value.copy(isSavingTemplate = true)

                try {
                    val templateId = db.collection("manualEntryTemplates").document().id
                    val template = ManualEntryTemplate(
                        id = templateId,
                        guardId = id,
                        driverName = driverName,
                        vehicleNumber = vehicleNumber,
                        defaultGate = defaultGate,
                        notes = notes,
                        useCount = 0
                    )

                    db.collection("manualEntryTemplates").document(templateId)
                        .set(template.toMap())
                        .await()

                    loadSavedTemplates(id)
                    
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Template saved successfully",
                        isSavingTemplate = false
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to save template: ${e.message}",
                        isSavingTemplate = false
                    )
                }
            }
        }
    }

    // ============ STEP 2: DAILY SUMMARY REPORT FUNCTIONS ============

    /**
     * Generate end-of-shift summary report
     */
    fun generateShiftReport() {
        viewModelScope.launch {
            guardId?.let { id ->
                _uiState.value = _uiState.value.copy(isGeneratingReport = true)

                try {
                    val guardData = db.collection("users").document(id).get().await()
                        .toObject(User::class.java)

                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(Date())

                    // Get today's sessions scanned by this guard
                    val startOfDay = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                    }.timeInMillis

                    val sessions = db.collection("parkingSessions")
                        .whereEqualTo("scannedByGuardId", id)
                        .whereGreaterThanOrEqualTo("entryTime", startOfDay)
                        .get()
                        .await()
                        .toObjects(ParkingSession::class.java)

                    val entries = sessions.count { it.entryTime != 0L }
                    val exits = sessions.count { it.exitTime != null }
                    val manualEntries = sessions.count { it.qrCodeDataUsed == "MANUAL_ENTRY" }

                    val sessionSummaries = sessions.map { session ->
                        SessionSummary(
                            driverName = session.driverName,
                            vehicleNumber = session.vehicleNumber,
                            entryTime = session.entryTime,
                            exitTime = session.exitTime,
                            status = session.status
                        )
                    }

                    val reportId = db.collection("shiftReports").document().id
                    val report = ShiftReport(
                        id = reportId,
                        guardId = id,
                        guardName = guardData?.name ?: "Unknown",
                        shiftDate = today,
                        shiftStartTime = startOfDay,
                        shiftEndTime = System.currentTimeMillis(),
                        gateLocation = guardData?.vehicleNumber ?: "Main Entrance",
                        totalScans = entries + exits,
                        totalEntries = entries,
                        totalExits = exits,
                        manualEntries = manualEntries,
                        sessionIds = sessions.map { it.id },
                        sessionSummaries = sessionSummaries
                    )

                    db.collection("shiftReports").document(reportId)
                        .set(report.toMap())
                        .await()

                    _uiState.value = _uiState.value.copy(
                        currentShiftReport = report,
                        successMessage = "Shift report generated successfully",
                        isGeneratingReport = false
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to generate report: ${e.message}",
                        isGeneratingReport = false
                    )
                }
            }
        }
    }

    /**
     * Load past shift reports
     */
    fun loadPastReports() {
        viewModelScope.launch {
            guardId?.let { id ->
                try {
                    val reports = db.collection("shiftReports")
                        .whereEqualTo("guardId", id)
                        .orderBy("shiftStartTime", Query.Direction.DESCENDING)
                        .limit(30)
                        .get()
                        .await()
                        .toObjects(ShiftReport::class.java)

                    _uiState.value = _uiState.value.copy(pastReports = reports)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to load past reports: ${e.message}"
                    )
                }
            }
        }
    }

    // ============ STEP 3: ACTIVE SESSION QUICK ACTIONS ============

    /**
     * Load all active parking sessions
     */
    fun loadActiveSessions() {
        viewModelScope.launch {
            try {
                val sessions = db.collection("parkingSessions")
                    .whereEqualTo("status", SessionStatus.ACTIVE.name)
                    .orderBy("entryTime", Query.Direction.DESCENDING)
                    .get()
                    .await()
                    .toObjects(ParkingSession::class.java)

                _uiState.value = _uiState.value.copy(
                    activeSessions = sessions,
                    filteredSessions = applyFilter(sessions, _uiState.value.currentFilter)
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load sessions: ${e.message}"
                )
            }
        }
    }

    /**
     * Apply filter to active sessions
     */
    fun setFilter(filter: ActiveSessionFilter) {
        _uiState.value = _uiState.value.copy(
            currentFilter = filter,
            filteredSessions = applyFilter(_uiState.value.activeSessions, filter)
        )
    }

    private fun applyFilter(sessions: List<ParkingSession>, filter: ActiveSessionFilter): List<ParkingSession> {
        return when (filter) {
            ActiveSessionFilter.ALL -> sessions
            ActiveSessionFilter.LONG_STAY -> {
                val threeHoursAgo = System.currentTimeMillis() - (3 * 60 * 60 * 1000)
                sessions.filter { it.entryTime < threeHoursAgo }
            }
            ActiveSessionFilter.VIP -> sessions.filter { it.notes.contains("VIP", ignoreCase = true) }
            ActiveSessionFilter.FLAGGED -> sessions.filter { it.notes.contains("ISSUE", ignoreCase = true) }
            ActiveSessionFilter.MANUAL_ENTRY -> sessions.filter { it.qrCodeDataUsed == "MANUAL_ENTRY" }
        }
    }

    /**
     * Manually exit a session
     */
    fun manuallyExitSession(sessionId: String, reason: String = "") {
        viewModelScope.launch {
            try {
                val exitTime = System.currentTimeMillis()
                val session = db.collection("parkingSessions").document(sessionId).get().await()
                    .toObject(ParkingSession::class.java)

                if (session != null) {
                    val duration = calculateDuration(session.entryTime, exitTime)
                    
                    db.collection("parkingSessions").document(sessionId)
                        .update(
                            mapOf(
                                "exitTime" to exitTime,
                                "status" to SessionStatus.COMPLETED.name,
                                "duration" to duration,
                                "notes" to "${session.notes}\nManual Exit: $reason".trim()
                            )
                        )
                        .await()

                    loadActiveSessions()
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Session exited successfully"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to exit session: ${e.message}"
                )
            }
        }
    }

    /**
     * Extend time for a session (add grace period)
     */
    fun extendSession(sessionId: String, gracePeriodMinutes: Int, reason: String) {
        viewModelScope.launch {
            try {
                val session = db.collection("parkingSessions").document(sessionId).get().await()
                    .toObject(ParkingSession::class.java)

                if (session != null) {
                    db.collection("parkingSessions").document(sessionId)
                        .update(
                            mapOf(
                                "notes" to "${session.notes}\nExtended: +$gracePeriodMinutes min - $reason".trim()
                            )
                        )
                        .await()

                    loadActiveSessions()
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Session extended by $gracePeriodMinutes minutes"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to extend session: ${e.message}"
                )
            }
        }
    }

    /**
     * Add note to a session
     */
    fun addNoteToSession(sessionId: String, note: String, isVIP: Boolean = false) {
        viewModelScope.launch {
            try {
                val session = db.collection("parkingSessions").document(sessionId).get().await()
                    .toObject(ParkingSession::class.java)

                if (session != null) {
                    val prefix = if (isVIP) "VIP: " else ""
                    val newNote = if (session.notes.isEmpty()) "$prefix$note" else "${session.notes}\n$prefix$note"
                    
                    db.collection("parkingSessions").document(sessionId)
                        .update(mapOf("notes" to newNote))
                        .await()

                    loadActiveSessions()
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Note added successfully"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to add note: ${e.message}"
                )
            }
        }
    }

    /**
     * Perform bulk action on selected sessions
     */
    fun performBulkAction(action: BulkAction, sessionIds: Set<String>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPerformingBulkAction = true)

            try {
                when (action) {
                    BulkAction.EXIT_ALL -> {
                        sessionIds.forEach { sessionId ->
                            manuallyExitSession(sessionId, "Bulk exit at closing time")
                        }
                        _uiState.value = _uiState.value.copy(
                            successMessage = "${sessionIds.size} sessions exited",
                            selectedSessions = emptySet()
                        )
                    }
                    BulkAction.EXTEND_ALL -> {
                        sessionIds.forEach { sessionId ->
                            extendSession(sessionId, 30, "Bulk extension")
                        }
                        _uiState.value = _uiState.value.copy(
                            successMessage = "${sessionIds.size} sessions extended",
                            selectedSessions = emptySet()
                        )
                    }
                    BulkAction.EXPORT -> {
                        // Export functionality handled in UI layer
                        _uiState.value = _uiState.value.copy(
                            successMessage = "Export prepared"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Bulk action failed: ${e.message}"
                )
            } finally {
                _uiState.value = _uiState.value.copy(isPerformingBulkAction = false)
            }
        }
    }

    fun toggleSessionSelection(sessionId: String) {
        val current = _uiState.value.selectedSessions
        _uiState.value = _uiState.value.copy(
            selectedSessions = if (current.contains(sessionId)) {
                current - sessionId
            } else {
                current + sessionId
            }
        )
    }

    // ============ STEP 4: GUARD HANDOVER NOTES ============

    /**
     * Load shift notes
     */
    private fun loadShiftNotes() {
        viewModelScope.launch {
            try {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                
                val notes = db.collection("shiftNotes")
                    .whereGreaterThanOrEqualTo("shiftDate", getDateDaysAgo(7))
                    .orderBy("shiftDate", Query.Direction.DESCENDING)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(50)
                    .get()
                    .await()
                    .toObjects(ShiftNote::class.java)

                val unreadCount = guardId?.let { id ->
                    notes.count { !it.readBy.contains(id) }
                } ?: 0

                _uiState.value = _uiState.value.copy(
                    shiftNotes = notes,
                    unreadNotesCount = unreadCount
                )
            } catch (e: Exception) {
                // Silent fail
            }
        }
    }

    /**
     * Create a new shift handover note
     */
    fun createShiftNote(
        title: String,
        message: String,
        type: ShiftNoteType,
        priority: ShiftNotePriority,
        sessionId: String? = null,
        expiresInHours: Int? = null
    ) {
        viewModelScope.launch {
            guardId?.let { id ->
                try {
                    val guardData = db.collection("users").document(id).get().await()
                        .toObject(User::class.java)

                    val noteId = db.collection("shiftNotes").document().id
                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    
                    val expiresAt = expiresInHours?.let {
                        System.currentTimeMillis() + (it * 60 * 60 * 1000)
                    }

                    val note = ShiftNote(
                        id = noteId,
                        createdBy = id,
                        guardName = guardData?.name ?: "Unknown",
                        shiftDate = today,
                        type = type,
                        priority = priority,
                        title = title,
                        message = message,
                        sessionId = sessionId,
                        expiresAt = expiresAt
                    )

                    db.collection("shiftNotes").document(noteId)
                        .set(note.toMap())
                        .await()

                    loadShiftNotes()
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Shift note created successfully"
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to create note: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Mark shift note as read
     */
    fun markNoteAsRead(noteId: String) {
        viewModelScope.launch {
            guardId?.let { id ->
                try {
                    val note = db.collection("shiftNotes").document(noteId).get().await()
                        .toObject(ShiftNote::class.java)

                    if (note != null && !note.readBy.contains(id)) {
                        db.collection("shiftNotes").document(noteId)
                            .update("readBy", note.readBy + id)
                            .await()

                        loadShiftNotes()
                    }
                } catch (e: Exception) {
                    // Silent fail
                }
            }
        }
    }

    /**
     * Acknowledge shift note
     */
    fun acknowledgeNote(noteId: String) {
        viewModelScope.launch {
            guardId?.let { id ->
                try {
                    val note = db.collection("shiftNotes").document(noteId).get().await()
                        .toObject(ShiftNote::class.java)

                    if (note != null && !note.acknowledgedBy.contains(id)) {
                        db.collection("shiftNotes").document(noteId)
                            .update(
                                mapOf(
                                    "readBy" to (note.readBy + id).distinct(),
                                    "acknowledgedBy" to (note.acknowledgedBy + id).distinct()
                                )
                            )
                            .await()

                        loadShiftNotes()
                        _uiState.value = _uiState.value.copy(
                            successMessage = "Note acknowledged"
                        )
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to acknowledge note: ${e.message}"
                    )
                }
            }
        }
    }

    // ============ UTILITY FUNCTIONS ============

    private fun calculateDuration(entryTime: Long, exitTime: Long): String {
        val durationMillis = exitTime - entryTime
        val hours = (durationMillis / (1000 * 60 * 60)).toInt()
        val minutes = ((durationMillis % (1000 * 60 * 60)) / (1000 * 60)).toInt()
        return "${hours}h ${minutes}m"
    }

    private fun getDateDaysAgo(days: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}
