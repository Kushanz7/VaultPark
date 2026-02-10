package com.kushan.vaultpark.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// ============ STEP 1: MANUAL ENTRY MODELS ============

/**
 * Manual Entry Template for frequently visiting drivers
 * Stored in Firestore under /manualEntryTemplates/{templateId}
 */
@IgnoreExtraProperties
data class ManualEntryTemplate(
    val id: String = "",
    val guardId: String = "",
    val driverName: String = "",
    val vehicleNumber: String = "",
    val defaultGate: String = "",
    val notes: String = "",
    val useCount: Int = 0,
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val lastUsedAt: Date? = null
) {
    @Exclude
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "guardId" to guardId,
        "driverName" to driverName,
        "vehicleNumber" to vehicleNumber,
        "defaultGate" to defaultGate,
        "notes" to notes,
        "useCount" to useCount,
        "lastUsedAt" to lastUsedAt
    )
}

/**
 * Recent driver for quick selection in manual entry
 */
data class RecentDriver(
    val userId: String = "",
    val name: String = "",
    val vehicleNumber: String = "",
    val lastVisit: Long = 0L
)

// ============ STEP 2: DAILY SUMMARY MODELS ============

/**
 * Daily Summary Report for security guards
 * Stored in Firestore under /shiftReports/{reportId}
 */
@IgnoreExtraProperties
data class ShiftReport(
    val id: String = "",
    val guardId: String = "",
    val guardName: String = "",
    val shiftDate: String = "", // YYYY-MM-DD format
    val shiftStartTime: Long = 0L,
    val shiftEndTime: Long? = null,
    val gateLocation: String = "",
    
    // Statistics
    val totalScans: Int = 0,
    val totalEntries: Int = 0,
    val totalExits: Int = 0,
    val manualEntries: Int = 0,
    
    // Session details
    val sessionIds: List<String> = emptyList(),
    val sessionSummaries: List<SessionSummary> = emptyList(),
    
    // Issues and notes
    val issues: List<String> = emptyList(),
    val notes: String = "",
    
    @ServerTimestamp
    val generatedAt: Date? = null
) {
    @Exclude
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "guardId" to guardId,
        "guardName" to guardName,
        "shiftDate" to shiftDate,
        "shiftStartTime" to shiftStartTime,
        "shiftEndTime" to shiftEndTime,
        "gateLocation" to gateLocation,
        "totalScans" to totalScans,
        "totalEntries" to totalEntries,
        "totalExits" to totalExits,
        "manualEntries" to manualEntries,
        "sessionIds" to sessionIds,
        "sessionSummaries" to sessionSummaries.map { it.toMap() },
        "issues" to issues,
        "notes" to notes
    )
}

/**
 * Brief session summary for shift reports
 */
@IgnoreExtraProperties
data class SessionSummary(
    val driverName: String = "",
    val vehicleNumber: String = "",
    val entryTime: Long = 0L,
    val exitTime: Long? = null,
    val status: String = ""
) {
    @Exclude
    fun toMap(): Map<String, Any?> = mapOf(
        "driverName" to driverName,
        "vehicleNumber" to vehicleNumber,
        "entryTime" to entryTime,
        "exitTime" to exitTime,
        "status" to status
    )
}

// ============ STEP 3: ACTIVE SESSION QUICK ACTIONS ============

/**
 * Session quick action types
 */
enum class SessionAction(val displayName: String) {
    MANUAL_EXIT("Manual Exit"),
    EXTEND_TIME("Extend Time"),
    ADD_NOTE("Add Note"),
    MARK_VIP("Mark as VIP"),
    FLAG_ISSUE("Flag Issue")
}

/**
 * Session extension record
 * Stored as a field in ParkingSession or separate collection
 */
@IgnoreExtraProperties
data class SessionExtension(
    val extendedBy: String = "", // Guard ID
    val extendedAt: Long = 0L,
    val gracePeriodMinutes: Int = 0,
    val reason: String = ""
) {
    @Exclude
    fun toMap(): Map<String, Any?> = mapOf(
        "extendedBy" to extendedBy,
        "extendedAt" to extendedAt,
        "gracePeriodMinutes" to gracePeriodMinutes,
        "reason" to reason
    )
}

/**
 * Session note added by guard
 */
@IgnoreExtraProperties
data class SessionNote(
    val id: String = "",
    val sessionId: String = "",
    val guardId: String = "",
    val guardName: String = "",
    val note: String = "",
    val isVIP: Boolean = false,
    val isIssue: Boolean = false,
    @ServerTimestamp
    val addedAt: Date? = null
) {
    @Exclude
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "sessionId" to sessionId,
        "guardId" to guardId,
        "guardName" to guardName,
        "note" to note,
        "isVIP" to isVIP,
        "isIssue" to isIssue
    )
}

// ============ STEP 4: GUARD HANDOVER NOTES ============

/**
 * Shift handover note for communication between guards
 * Stored in Firestore under /shiftNotes/{noteId}
 */
@IgnoreExtraProperties
data class ShiftNote(
    val id: String = "",
    val createdBy: String = "", // Guard ID
    val guardName: String = "",
    val shiftDate: String = "", // YYYY-MM-DD
    val type: ShiftNoteType = ShiftNoteType.GENERAL,
    val priority: ShiftNotePriority = ShiftNotePriority.NORMAL,
    
    // Content
    val title: String = "",
    val message: String = "",
    val sessionId: String? = null, // Link to specific session if relevant
    
    // Read tracking
    val readBy: List<String> = emptyList(), // List of guard IDs who read this
    val acknowledgedBy: List<String> = emptyList(),
    
    @ServerTimestamp
    val createdAt: Date? = null,
    val expiresAt: Long? = null // Optional expiry for time-sensitive notes
) {
    @Exclude
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "createdBy" to createdBy,
        "guardName" to guardName,
        "shiftDate" to shiftDate,
        "type" to type.name,
        "priority" to priority.name,
        "title" to title,
        "message" to message,
        "sessionId" to sessionId,
        "readBy" to readBy,
        "acknowledgedBy" to acknowledgedBy,
        "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
        "expiresAt" to expiresAt
    )
}

/**
 * Types of shift notes
 */
enum class ShiftNoteType(val displayName: String, val icon: String) {
    GENERAL("General", "📝"),
    VIP_ARRIVAL("VIP Arrival", "⭐"),
    MAINTENANCE("Maintenance", "🔧"),
    SECURITY_ALERT("Security Alert", "🚨"),
    POLICY_UPDATE("Policy Update", "📋"),
    HANDOVER("Shift Handover", "🔄")
}

/**
 * Priority levels for shift notes
 */
enum class ShiftNotePriority(val displayName: String, val colorHex: String) {
    LOW("Low", "#4CAF50"),
    NORMAL("Normal", "#2196F3"),
    HIGH("High", "#FF9800"),
    URGENT("Urgent", "#F44336")
}

// ============ ENHANCED PARKING SESSION MODEL (Additional Fields) ============

/**
 * Extension to existing ParkingSession model for admin tools
 * These fields should be added to your existing ParkingSession model
 */
data class ParkingSessionEnhanced(
    // ... all existing ParkingSession fields ...
    
    // Manual entry tracking
    val isManualEntry: Boolean = false,
    val manualEntryBy: String? = null, // Guard ID who created manual entry
    
    // Quick actions
    val extensions: List<SessionExtension> = emptyList(),
    val adminNotes: List<SessionNote> = emptyList(),
    val isVIP: Boolean = false,
    val hasFlaggedIssue: Boolean = false,
    
    // Grace period
    val gracePeriodMinutes: Int = 0,
    val gracePeriodReason: String? = null
)

// ============ UI STATE MODELS ============

/**
 * Filter options for active sessions
 */
enum class ActiveSessionFilter(val displayName: String) {
    ALL("All Sessions"),
    LONG_STAY("Parked >3 Hours"),
    VIP("VIP Sessions"),
    FLAGGED("Flagged Issues"),
    MANUAL_ENTRY("Manual Entries")
}

/**
 * Bulk action types
 */
enum class BulkAction(val displayName: String) {
    EXIT_ALL("Exit All"),
    EXTEND_ALL("Extend All"),
    EXPORT("Export List")
}
