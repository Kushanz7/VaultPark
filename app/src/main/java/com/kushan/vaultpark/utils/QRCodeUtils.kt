package com.kushan.vaultpark.utils

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.util.UUID
import kotlin.math.abs

object QRCodeUtils {
    
    /**
     * Generate a secure QR code string with format: VAULTPARK|[userId]|[timestamp]|[vehicleNumber]|[parkingLotId]|[hash]
     */
    fun generateQRCodeString(
        userId: String, 
        vehicleNumber: String, 
        timestamp: Long = System.currentTimeMillis(),
        gateHint: String? = null,
        parkingLotId: String? = null
    ): String {
        val gateInfo = if (gateHint != null) "|$gateHint" else ""
        val lotInfo = if (parkingLotId != null) "|$parkingLotId" else ""
        val dataToHash = "VAULTPARK|$userId|$timestamp|$vehicleNumber$gateInfo$lotInfo"
        val hash = generateSecurityHash(dataToHash)
        return "$dataToHash|$hash"
    }
    
    /**
     * Generate security hash using SHA-256
     */
    private fun generateSecurityHash(data: String): String {
        return try {
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(data.toByteArray())
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            "invalid_hash"
        }
    }
    
    /**
     * Convert QR code string to bitmap
     */
    fun generateQRCodeBitmap(
        content: String,
        size: Int = 512,
        errorCorrectionLevel: String = "H"
    ): Bitmap? {
        return try {
            val hints = mapOf(
                EncodeHintType.ERROR_CORRECTION to com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.valueOf(errorCorrectionLevel),
                EncodeHintType.MARGIN to 1
            )
            
            val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Validate QR code format
     */
    fun validateQRCodeFormat(qrCode: String): Boolean {
        val parts = qrCode.split("|")
        return parts.size == 5 && parts[0] == "VAULTPARK"
    }
    
    /**
     * Extract user ID from QR code string
     */
    fun extractUserIdFromQR(qrCode: String): String? {
        return if (validateQRCodeFormat(qrCode)) {
            qrCode.split("|")[1]
        } else {
            null
        }
    }
    
    /**
     * Extract timestamp from QR code string
     */
    fun extractTimestampFromQR(qrCode: String): Long? {
        return if (validateQRCodeFormat(qrCode)) {
            qrCode.split("|")[2].toLongOrNull()
        } else {
            null
        }
    }
}
