package com.kushan.vaultpark.model

import java.time.LocalDateTime

data class ParkingSession(
    val id: String,
    val entryTime: LocalDateTime,
    val exitTime: LocalDateTime? = null,
    val duration: Long? = null,
    val cost: Double = 0.0
)

data class Invoice(
    val id: String,
    val month: String,
    val amount: Double,
    val status: String,
    val createdDate: LocalDateTime
)

data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val membershipType: String,
    val createdDate: LocalDateTime
)
