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
    val createdAt: Date? = null
) {
    @Exclude
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "name" to name,
        "email" to email,
        "phone" to phone,
        "role" to role.name,
        "vehicleNumber" to vehicleNumber,
        "membershipType" to membershipType
    )
}

@IgnoreExtraProperties
data class ParkingSession(
    val id: String = "",
    val driverId: String = "",
    val driverName: String = "",
    val vehicleNumber: String = "",
    val entryTime: Long = 0L,
    val exitTime: Long? = null,
    val gateLocation: String = "",
    val scannedByGuardId: String? = null,
    val guardName: String? = null,
    val status: String = SessionStatus.ACTIVE.name,
    val qrCodeDataUsed: String = "",
    val duration: String = ""
) {
    @Exclude
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "driverId" to driverId,
        "driverName" to driverName,
        "vehicleNumber" to vehicleNumber,
        "entryTime" to entryTime,
        "exitTime" to exitTime,
        "gateLocation" to gateLocation,
        "scannedByGuardId" to scannedByGuardId,
        "guardName" to guardName,
        "status" to status,
        "qrCodeDataUsed" to qrCodeDataUsed,
        "duration" to duration
    )
}

data class Invoice(
    val id: String = "",
    val month: String = "",
    val amount: Double = 0.0,
    val status: String = "",
    val createdDate: String = ""
)

// ============ BILLING MODELS ============

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
    val status: String = "PENDING", // PENDING, PAID, OVERDUE
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
    val membershipType: String = "", // Gold, Platinum
    val hourlyRate: Double = 5.0,
    val dailyCap: Double = 40.0,
    val monthlyUnlimited: Double? = null // Only for Platinum
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
    val type: String = "", // CARD, BANK, WALLET
    val lastFourDigits: String = "",
    val cardBrand: String? = null, // Visa, Mastercard, AmEx
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

// ============ ANALYTICS & REPORTS MODELS ============

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
    val date: Long = 0, // Timestamp
    val scanCount: Int = 0
)

data class TopDriver(
    val driverId: String = "",
    val driverName: String = "",
    val vehicleNumber: String = "",
    val visitCount: Int = 0,
    val totalHours: Double = 0.0
)