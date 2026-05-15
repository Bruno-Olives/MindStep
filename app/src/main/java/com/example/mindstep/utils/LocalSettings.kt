package com.example.mindstep.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.compositionLocalOf

val LocalReduceAnimations = compositionLocalOf { false }
val LocalHapticEnabled = compositionLocalOf { true }

/**
 * Helper for reliable haptic feedback using the Android Vibrator API.
 * Uses createOneShot() for maximum device compatibility.
 */
object HapticHelper {

    private fun getVibrator(context: Context): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    /** Light tick — for slider changes, small interactions */
    fun tick(context: Context) {
        vibrate(context, 15L, 80)
    }

    /** Medium click — for button presses, confirmations */
    fun click(context: Context) {
        vibrate(context, 30L, 150)
    }

    /** Heavy click — for save, delete, important actions */
    fun heavyClick(context: Context) {
        vibrate(context, 50L, 255)
    }

    private fun vibrate(context: Context, durationMs: Long, amplitude: Int) {
        try {
            val vibrator = getVibrator(context)
            if (!vibrator.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(durationMs, amplitude)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        } catch (e: Exception) {
            // Silently ignore if vibration fails
        }
    }
}
