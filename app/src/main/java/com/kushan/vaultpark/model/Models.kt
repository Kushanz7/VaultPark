package com.kushan.vaultpark.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// User roles
enum class UserRole {
    DRIVER, SECURITY
}

// Session status
enum class SessionStatus {
    ACTIVE, COMPLETED
}

// ============ ENHANCED USER MODEL ============
@IgnoreExtraProperties
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: UserRole = UserRole.DRIVER,
    val vehicleNumber: String = "",
    val membershipType: String = "",
    @ServerTimestamp
    val createdAt: Date? = null,
    
    // ✨ NEW: Favorite Gates Feature
    val favoriteGate: String? = null,
    val favoriteGateNote: String? = null,
    val recentGates: List<String> = emptyList(), // Last 5 used gates
    
    // ✨ NEW: User Preferences
    val preferences: UserPreferences = UserPreferences()
) {
    @Exclude
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "name" to name,
        "email" to email,
        "phone" to phone,
        "role" to role.name,
        "vehicleNumber" to vehicleNumber,
        "membershipType" to membershipType,
        "favoriteGate" to favoriteGate,
        "favoriteGateNote" to favoriteGateNote,
        "recentGates" to recentGates,
        "preferences" to preferences.toMap()
    )
}

// UserPreferences is defined in ProfileModels.kt

// ============ ENHANCED PARKING SESSION MODEL ============
@IgnoreExtraProperties
data class ParkingSession(
    val id: String = "",
    val driverId: String = "",
    val driverName: String = "",
    val vehicleNumber: String = "",
    val parkingLotId: String = "", // NEW: Reference to parking lot
    val entryTime: Long = 0L,
    val exitTime: Long? = null,
    val gateLocation: String = "",
    val location: String = "", // NEW: Parking Lot Address/Name
    val scannedByGuardId: String? = null,
    val guardName: String? = null,
    val status: String = SessionStatus.ACTIVE.name,
    val qrCodeDataUsed: String = "",
    val duration: String = "",
    
    // ✨ NEW: Session Notes & Tags Feature
    val notes: String = "",
    val tags: List<String> = emptyList(), // Work, Personal, Meeting, Event, Other
    
    // NEW: Manual entry tracking
    val isManualEntry: Boolean = false,
    val manualEntryBy: String? = null,

    @ServerTimestamp
    val createdAt: Date? = null
) {
    @Exclude
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "driverId" to driverId,
        "driverName" to driverName,
        "vehicleNumber" to vehicleNumber,
        "parkingLotId" to parkingLotId,
        "entryTime" to entryTime,
        "exitTime" to exitTime,
        "gateLocation" to gateLocation,
        "location" to location,
        "scannedByGuardId" to scannedByGuardId,
        "guardName" to guardName,
        "status" to status,
        "qrCodeDataUsed" to qrCodeDataUsed,
        "duration" to duration,
        "notes" to notes,
        "tags" to tags,
        "isManualEntry" to isManualEntry,
        "manualEntryBy" to manualEntryBy
    )
}

// ✨ NEW: Session Tag Enum
enum class SessionTag(val displayName: String, val icon: String) {
    WORK("Work", "💼"),
    PERSONAL("Personal", "🏠"),
    MEETING("Meeting", "🤝"),
    EVENT("Event", "🎉"),
    GYM("Gym", "💪"),
    SHOPPING("Shopping", "🛍️"),
    HOSPITAL("Hospital", "🏥"),
    OTHER("Other", "📌");
    
    companion object {
        fun fromString(value: String): SessionTag {
            return values().find { it.name == value } ?: OTHER
        }
    }
}

// ============ GATE INFORMATION MODEL ============
@IgnoreExtraProperties
data class GateInfo(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val notes: String = "",
    val isActive: Boolean = true,
    val totalUsage: Int = 0
) {
    @Exclude
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "name" to name,
        "location" to location,
        "notes" to notes,
        "isActive" to isActive,
        "totalUsage" to totalUsage
    )
}

// ============ EXISTING MODELS (UNCHANGED) ============

data class Invoice(
    val id: String = "",
    val month: String = "",
    val amount: Double = 0.0,
    val status: String = "",
    val createdDate: String = ""
)

@IgnoreExtraProperties
data class InvoiceNew(
    val id: String = "",
    val driverId: String = "",
    val driverName: String = "",
    val month: Int = 1,
    val year: Int = 2026,
    val totalSessions: Int = 0,
    val totalHours: Double = 0.0,
    val totalAmount: Double = 0.0,
    val sessionIds: List<String> = emptyList(),
    val status: String = "PENDING",
    @ServerTimestamp
    val generatedAt: Date? = null,
    val paidAt: Date? = null,
    val paymentMethod: String? = null
) {
    @Exclude
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "driverId" to driverId,
        "driverName" to driverName,
        "month" to month,
        "year" to year,
        "totalSessions" to totalSessions,
        "totalHours" to totalHours,
        "totalAmount" to totalAmount,
        "sessionIds" to sessionIds,
        "status" to status,
        "paidAt" to paidAt,
        "paymentMethod" to paymentMethod
    )
}

@IgnoreExtraProperties
data class PricingTier(
    val membershipType: String = "",
    val hourlyRate: Double = 5.0,
    val dailyCap: Double = 40.0,
    val monthlyUnlimited: Double? = null
) {
    @Exclude
    fun toMap(): Map<String, Any?> = mapOf(
        "membershipType" to membershipType,
        "hourlyRate" to hourlyRate,
        "dailyCap" to dailyCap,
        "monthlyUnlimited" to monthlyUnlimited
    )
}

@IgnoreExtraProperties
data class PaymentMethod(
    val id: String = "",
    val userId: String = "",
    val type: String = "",
    val lastFourDigits: String = "",
    val cardBrand: String? = null,
    val isDefault: Boolean = false
) {
    @Exclude
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "userId" to userId,
        "type" to type,
        "lastFourDigits" to lastFourDigits,
        "cardBrand" to cardBrand,
        "isDefault" to isDefault
    )
}

data class QRCodeData(
    val code: String = "",
    val generatedAt: Long = 0,
    val expiresAt: Long = 0,
    val userId: String = "",
    val securityHash: String = ""
)

data class ParkingStatus(
    val isParked: Boolean = false,
    val parkedSince: String? = null,
    val location: String? = null
)

data class ReportStats(
    val totalScans: Int = 0,
    val totalEntries: Int = 0,
    val totalExits: Int = 0,
    val activeNow: Int = 0,
    val averageDuration: Double = 0.0,
    val busiestHour: Int = 0,
    val dateRange: String = ""
)

data class HourlyData(
    val hour: Int = 0,
    val scanCount: Int = 0
)

data class DailyTrendData(
    val date: Long = 0,
    val scanCount: Int = 0
)

data class TopDriver(
    val driverId: String = "",
    val driverName: String = "",
    val vehicleNumber: String = "",
    val visitCount: Int = 0,
    val totalHours: Double = 0.0
)

// ============ QUICK STATS MODEL ============
// ✨ NEW: Personal Insights Data
data class PersonalInsights(
    val mostUsedGate: String = "",
    val mostUsedGateCount: Int = 0,
    val averageDuration: Double = 0.0, // in hours
    val busiestDayOfWeek: String = "",
    val totalHoursThisMonth: Double = 0.0,
    val totalHoursLastMonth: Double = 0.0,
    val totalAmountThisMonth: Double = 0.0,
    val totalAmountLastMonth: Double = 0.0,
    val favoriteTag: String = "",
    val tagDistribution: Map<String, Int> = emptyMap()
)

// ============ PARKING LOT MODEL ============
// 🅿️ NEW: Parking Lot managed by Security Guards
@IgnoreExtraProperties
data class ParkingLot(
    val id: String = "",
    val securityGuardId: String = "", // One parking lot per security guard
    val securityGuardName: String = "",
    val name: String = "", // e.g., "Building A Parking", "Main Gate Lot"
    val location: String = "", // Address or description
    val totalSpaces: Int = 0, // Total parking spaces available
    val availableSpaces: Int = 0, // Available spaces now
    val hourlyRate: Double = 0.0, // Hourly parking rate in dollars
    val dailyCap: Double? = null, // Maximum daily charge
    val status: String = "ACTIVE", // ACTIVE, INACTIVE
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null,
    
    // ✨ NEW: Map & Details Support
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val facilities: List<String> = emptyList(), // e.g., ["24/7", "CCTV", "Covered", "EV Charging"]
    val imageUrl: String? = null,
    val rating: Double = 0.0
) {
    @Exclude
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "securityGuardId" to securityGuardId,
        "securityGuardName" to securityGuardName,
        "name" to name,
        "location" to location,
        "totalSpaces" to totalSpaces,
        "availableSpaces" to availableSpaces,
        "hourlyRate" to hourlyRate,
        "dailyCap" to dailyCap,
        "status" to status,
        "latitude" to latitude,
        "longitude" to longitude,
        "facilities" to facilities,
        "imageUrl" to imageUrl,
        "rating" to rating
    )
}
