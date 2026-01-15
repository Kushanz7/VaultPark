package com.kushan.vaultpark.data

import com.kushan.vaultpark.model.ParkingSession
import com.kushan.vaultpark.model.Invoice
import com.kushan.vaultpark.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Repository interface for data operations
 * This acts as the single source of truth for data
 */
interface ParkingRepository {
    fun getParkingSessions(): Flow<List<ParkingSession>>
    fun getInvoices(): Flow<List<Invoice>>
    fun getUser(): Flow<User?>
    suspend fun startParkingSession(): ParkingSession
    suspend fun endParkingSession(sessionId: String): ParkingSession?
}

/**
 * Implementation of ParkingRepository
 * In a production app, this would handle database and API calls
 */
class DefaultParkingRepository : ParkingRepository {
    override fun getParkingSessions(): Flow<List<ParkingSession>> {
        // TODO: Implement actual data fetching from database or API
        return flowOf(emptyList())
    }

    override fun getInvoices(): Flow<List<Invoice>> {
        // TODO: Implement actual invoice fetching
        return flowOf(emptyList())
    }

    override fun getUser(): Flow<User?> {
        // TODO: Implement actual user fetching
        return flowOf(null)
    }

    override suspend fun startParkingSession(): ParkingSession {
        // TODO: Implement session creation
        return ParkingSession(
            id = "1",
            entryTime = java.util.Date()
        )
    }

    override suspend fun endParkingSession(sessionId: String): ParkingSession? {
        // TODO: Implement session ending
        return null
    }
}
