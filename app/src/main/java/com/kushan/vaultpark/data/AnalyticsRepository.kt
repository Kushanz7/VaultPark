package com.kushan.vaultpark.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kushan.vaultpark.model.ParkingSession
import kotlinx.coroutines.tasks.await

class AnalyticsRepository(private val firestore: FirebaseFirestore) {
    
    /**
     * Fetch parking sessions for a specific date range
     */
    suspend fun fetchSessionsForDateRange(
        startTime: Long,
        endTime: Long,
        guardId: String? = null
    ): List<ParkingSession> {
        return try {
            val query = firestore.collection("parkingSessions")
                .whereGreaterThanOrEqualTo("entryTime", startTime)
                .whereLessThanOrEqualTo("entryTime", endTime)
                .orderBy("entryTime", Query.Direction.DESCENDING)
            
            val filteredQuery = if (guardId != null) {
                query.whereEqualTo("scannedByGuardId", guardId)
            } else {
                query
            }
            
            filteredQuery.get().await().toObjects(ParkingSession::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Get count of active parking sessions
     */
    suspend fun getActiveSessionsCount(): Int {
        return try {
            firestore.collection("parkingSessions")
                .whereEqualTo("status", "ACTIVE")
                .get()
                .await()
                .size()
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * Fetch all sessions (for reports)
     */
    suspend fun fetchAllSessions(): List<ParkingSession> {
        return try {
            firestore.collection("parkingSessions")
                .orderBy("entryTime", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(ParkingSession::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Fetch sessions by driver ID
     */
    suspend fun fetchSessionsByDriver(driverId: String): List<ParkingSession> {
        return try {
            firestore.collection("parkingSessions")
                .whereEqualTo("driverId", driverId)
                .orderBy("entryTime", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(ParkingSession::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
