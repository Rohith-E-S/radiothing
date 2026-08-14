package com.radiothing.player.haptics

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import javax.inject.Inject

class HapticFeedbackManager @Inject constructor(
    private val application: Application
) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private fun vibrate(effect: VibrationEffect) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(effect)
        }
    }

    fun tick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrate(VibrationEffect.createOneShot(5, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    fun click() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    fun heavyClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrate(VibrationEffect.createOneShot(15, 255))
        }
    }

    fun doublePulse() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 8, 50, 8)
            val amplitudes = intArrayOf(0, 255, 0, 255)
            vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        }
    }

    fun directionalUp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 5, 20, 8, 20, 12)
            vibrate(VibrationEffect.createWaveform(timings, -1))
        }
    }

    fun directionalDown() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 12, 20, 8, 20, 5)
            vibrate(VibrationEffect.createWaveform(timings, -1))
        }
    }

    fun success() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 20, 50, 20, 50, 40)
            val amplitudes = intArrayOf(0, 100, 0, 100, 0, 255)
            vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        }
    }

    fun error() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 30, 30, 30)
            val amplitudes = intArrayOf(0, 255, 0, 255)
            vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        }
    }

    fun bufferingPulse() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrate(VibrationEffect.createOneShot(20, 50))
        }
    }

    fun swipeSweep() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 5, 10, 10, 10, 15)
            val amplitudes = intArrayOf(0, 50, 0, 100, 0, 150)
            vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        }
    }
}
