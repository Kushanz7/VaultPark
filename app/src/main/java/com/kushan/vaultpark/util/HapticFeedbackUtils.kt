package com.kushan.vaultpark.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.ContextCompat

/**
 * Haptic feedback utilities
 */
object HapticFeedback {

    /**
     * Light tap feedback (button press, toggle)
     */
    fun performLightTap(context: Context) {
        performVibration(context, longArrayOf(0, 20), -1)
    }

    /**
     * Medium tap feedback (important actions)
     */
    fun performMediumTap(context: Context) {
        performVibration(context, longArrayOf(0, 30), -1)
    }

    /**
     * Success vibration pattern (QR scan success, payment confirmation)
     */
    fun performSuccess(context: Context) {
        performVibration(context, longArrayOf(0, 50, 30, 50), -1)
    }

    /**
     * Error vibration pattern (failed action, error state)
     */
    fun performError(context: Context) {
        performVibration(context, longArrayOf(0, 100, 50, 100), -1)
    }

    /**
     * Warning vibration pattern (cautious action)
     */
    fun performWarning(context: Context) {
        performVibration(context, longArrayOf(0, 40, 20, 40, 20, 40), -1)
    }

    /**
     * Custom vibration pattern
     * @param pattern Array of vibration on/off times in milliseconds
     * @param repeat -1 for no repeat
     */
    private fun performVibration(
        context: Context,
        pattern: LongArray,
        repeat: Int = -1
    ) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE)
                        as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator
                val effect = VibrationEffect.createWaveform(pattern, repeat)
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                val effect = VibrationEffect.createWaveform(pattern, repeat)
                @Suppress("DEPRECATION")
                vibrator?.vibrate(effect)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
