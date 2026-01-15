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