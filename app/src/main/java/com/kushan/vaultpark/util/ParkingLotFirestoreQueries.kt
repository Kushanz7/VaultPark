package com.kushan.vaultpark.util

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.kushan.vaultpark.model.ParkingLot
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Firestore queries for parking lot functionality
 * Managed by security guards
 */
object ParkingLotFirestoreQueries {
    
    private val db = FirebaseFirestore.getInstance()
    private const val TAG = "ParkingLotFirestoreQueries"
    private const val PARKING_LOTS_COLLECTION = "parkingLots"
    
    /**
     * Create a new parking lot for a security guard
     * Each security guard can only have one parking lot
     */
    suspend fun createParkingLot(
        securityGuardId: String,
        securityGuardName: String,
        name: String,
        location: String,
        latitude: Double,
        longitude: Double,
        totalSpaces: Int,
        hourlyRate: Double,
        dailyCap: Double? = null
    ): Result<ParkingLot> {
        return try {
            // Check if parking lot already exists for this guard
            val existing = db.collection(PARKING_LOTS_COLLECTION)
                .whereEqualTo("securityGuardId", securityGuardId)
                .limit(1)
                .get()
                .await()
            
            if (existing.documents.isNotEmpty()) {
                return Result.failure(Exception("Security guard already has a parking lot. Only one parking lot per guard allowed."))
            }
            
            val parkingLot = ParkingLot(
                id = UUID.randomUUID().toString(),
                securityGuardId = securityGuardId,
                securityGuardName = securityGuardName,
                name = name,
                location = location,
                latitude = latitude,
                longitude = longitude,
                totalSpaces = totalSpaces,
                availableSpaces = totalSpaces,
                hourlyRate = hourlyRate,
                dailyCap = dailyCap,
                status = "ACTIVE"
            )
            
            db.collection(PARKING_LOTS_COLLECTION)
                .document(parkingLot.id)
                .set(parkingLot)
                .await()
            
            Log.d(TAG, "Parking lot created: ${parkingLot.id}")
            Result.success(parkingLot)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating parking lot", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get parking lot for a specific security guard
     */
    suspend fun getParkingLotByGuardId(securityGuardId: String): ParkingLot? {
        return try {
            val snapshot = db.collection(PARKING_LOTS_COLLECTION)
                .whereEqualTo("securityGuardId", securityGuardId)
                .limit(1)
                .get()
                .await()
            
            snapshot.documents.firstOrNull()?.toObject(ParkingLot::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching parking lot for guard", e)
            null
        }
    }
    
    /**
     * Get parking lot by ID
     */
    suspend fun getParkingLotById(parkingLotId: String): ParkingLot? {
        return try {
            db.collection(PARKING_LOTS_COLLECTION)
                .document(parkingLotId)
                .get()
                .await()
                .toObject(ParkingLot::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching parking lot by ID", e)
            null
        }
    }
    
    /**
     * Update parking lot details
     */
    suspend fun updateParkingLot(
        parkingLotId: String,
        name: String? = null,
        location: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        totalSpaces: Int? = null,
        hourlyRate: Double? = null,
        dailyCap: Double? = null,
        status: String? = null
    ): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any?>()
            name?.let { updates["name"] = it }
            location?.let { updates["location"] = it }
            latitude?.let { updates["latitude"] = it }
            longitude?.let { updates["longitude"] = it }
            totalSpaces?.let { updates["totalSpaces"] = it }
            hourlyRate?.let { updates["hourlyRate"] = it }
            dailyCap?.let { updates["dailyCap"] = it }
            status?.let { updates["status"] = it }
            
            if (updates.isNotEmpty()) {
                db.collection(PARKING_LOTS_COLLECTION)
                    .document(parkingLotId)
                    .update(updates)
                    .await()
                Log.d(TAG, "Parking lot updated: $parkingLotId")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating parking lot", e)
            Result.failure(e)
        }
    }
    
    /**
     * Update available spaces (when a car parks or exits)
     */
    suspend fun updateAvailableSpaces(
        parkingLotId: String,
        change: Int // Positive to reduce (car entering), negative to increase (car exiting)
    ): Result<Unit> {
        return try {
            db.runTransaction { transaction ->
                val lotRef = db.collection(PARKING_LOTS_COLLECTION).document(parkingLotId)
                val lot = transaction.get(lotRef).toObject(ParkingLot::class.java)
                    ?: throw Exception("Parking lot not found")
                
                val newAvailable = (lot.availableSpaces - change).coerceAtLeast(0)
                transaction.update(lotRef, "availableSpaces", newAvailable)
            }.await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating available spaces", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get all active parking lots (for drivers to see available lots)
     */
    suspend fun getAllActiveParkingLots(): List<ParkingLot> {
        return try {
            db.collection(PARKING_LOTS_COLLECTION)
                .whereEqualTo("status", "ACTIVE")
                .get()
                .await()
                .toObjects(ParkingLot::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching active parking lots", e)
            emptyList()
        }
    }
    
    /**
     * Deactivate a parking lot
     */
    suspend fun deactivateParkingLot(parkingLotId: String): Result<Unit> {
        return try {
            db.collection(PARKING_LOTS_COLLECTION)
                .document(parkingLotId)
                .update("status", "INACTIVE")
                .await()
            Log.d(TAG, "Parking lot deactivated: $parkingLotId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deactivating parking lot", e)
            Result.failure(e)
        }
    }
    
    /**
     * Delete a parking lot (only if no active sessions)
     */
    suspend fun deleteParkingLot(parkingLotId: String): Result<Unit> {
        return try {
            // Check if there are any active sessions in this lot
            val activeSessions = db.collection("parkingSessions")
                .whereEqualTo("parkingLotId", parkingLotId)
                .whereEqualTo("status", "ACTIVE")
                .get()
                .await()
            
            if (activeSessions.documents.isNotEmpty()) {
                return Result.failure(Exception("Cannot delete parking lot with active sessions"))
            }
            
            db.collection(PARKING_LOTS_COLLECTION)
                .document(parkingLotId)
                .delete()
                .await()
            
            Log.d(TAG, "Parking lot deleted: $parkingLotId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting parking lot", e)
            Result.failure(e)
        }
    }
}
