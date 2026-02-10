package com.kushan.vaultpark.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.kushan.vaultpark.R
import org.osmdroid.views.overlay.Marker

/**
 * Utility object for creating custom map markers with different colors
 */
object CustomMarkerUtils {
    
    /**
     * Creates a marker icon with the specified color
     * @param context Android context
     * @param color Color in ARGB format
     * @return Drawable for the marker
     */
    fun createColoredMarker(context: Context, color: Int): Drawable {
        // Create a simple circular marker
        val size = 48 // Size in pixels
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        val paint = Paint().apply {
            isAntiAlias = true
            this.color = color
            style = Paint.Style.FILL
        }
        
        // Draw outer circle (border)
        val borderPaint = Paint().apply {
            isAntiAlias = true
            this.color = android.graphics.Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, borderPaint)
        
        // Draw inner circle (main color)
        canvas.drawCircle(size / 2f, size / 2f, (size / 2f) - 4, paint)
        
        // Draw center dot
        val centerPaint = Paint().apply {
            isAntiAlias = true
            this.color = android.graphics.Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawCircle(size / 2f, size / 2f, 8f, centerPaint)
        
        return BitmapDrawable(context.resources, bitmap)
    }
    
    /**
     * Creates a marker for the user's current location (Blue/Cyan)
     */
    fun createUserLocationMarker(context: Context): Drawable {
        // Cyan/Blue color for user location
        val userLocationColor = android.graphics.Color.rgb(0, 191, 255) // Deep Sky Blue
        return createColoredMarker(context, userLocationColor)
    }
    
    /**
     * Creates a marker for parking lots (Green/Lime)
     * @param availableSpaces Number of available spaces (for future use - could vary color)
     */
    fun createParkingLotMarker(context: Context, availableSpaces: Int = 0): Drawable {
        // Green/Lime color for parking lots
        val parkingLotColor = if (availableSpaces > 0) {
            android.graphics.Color.rgb(50, 205, 50) // Lime Green
        } else {
            android.graphics.Color.rgb(255, 69, 0) // Red-Orange for full
        }
        return createColoredMarker(context, parkingLotColor)
    }
    
    /**
     * Creates a pin-style marker with the specified color
     */
    fun createPinMarker(context: Context, color: Int): Drawable {
        val size = 64
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        val paint = Paint().apply {
            isAntiAlias = true
            this.color = color
            style = Paint.Style.FILL
        }
        
        // Draw pin shape (circle on top, triangle below)
        val circleCenterY = size / 3f
        val circleRadius = size / 4f
        
        // Draw white border
        val borderPaint = Paint().apply {
            isAntiAlias = true
            this.color = android.graphics.Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawCircle(size / 2f, circleCenterY, circleRadius + 3, borderPaint)
        
        // Draw colored circle
        canvas.drawCircle(size / 2f, circleCenterY, circleRadius, paint)
        
        // Draw triangle (pin point)
        val path = android.graphics.Path().apply {
            moveTo(size / 2f - circleRadius, circleCenterY + circleRadius - 3)
            lineTo(size / 2f, size.toFloat() - 4)
            lineTo(size / 2f + circleRadius, circleCenterY + circleRadius - 3)
            close()
        }
        canvas.drawPath(path, paint)
        
        return BitmapDrawable(context.resources, bitmap)
    }
    
    /**
     * Creates a pin marker for user location
     */
    fun createUserLocationPin(context: Context): Drawable {
        val userLocationColor = android.graphics.Color.rgb(0, 191, 255)
        return createPinMarker(context, userLocationColor)
    }
    
    /**
     * Creates a pin marker for parking lots
     */
    fun createParkingLotPin(context: Context, availableSpaces: Int = 0): Drawable {
        val parkingLotColor = if (availableSpaces > 0) {
            android.graphics.Color.rgb(50, 205, 50)
        } else {
            android.graphics.Color.rgb(255, 69, 0)
        }
        return createPinMarker(context, parkingLotColor)
    }
}
