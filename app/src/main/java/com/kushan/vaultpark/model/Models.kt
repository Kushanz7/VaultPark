package com.kushan.vaultpark.model

data class ParkingSession(
    val id: String,
    val entryTime: String,
    val exitTime: String? = null,
    val duration: Long? = null,
    val cost: Double = 0.0
)

data class Invoice(
    val id: String,
    val month: String,
    val amount: Double,
    val status: String,
    val createdDate: String
)

data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val vehicleNumber: String,
    val membershipType: String,
    val createdDate: String
)

data class QRCodeData(
    val code: String,
    val generatedAt: Long,
    val expiresAt: Long,
    val userId: String,
    val securityHash: String
)

data class ParkingStatus(
    val isParked: Boolean,
    val parkedSince: String? = null,
    val location: String? = null)