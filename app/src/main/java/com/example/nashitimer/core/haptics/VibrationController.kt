package com.example.nashitimer.core.haptics

import android.content.Context
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VibrationController @Inject constructor(
    @ApplicationContext context: Context
) {
    private val vibrator: Vibrator =
        context.getSystemService(VibratorManager::class.java).defaultVibrator

    fun previewIntensity(amplitude: Int) {
        vibrate(
            durationMs = PREVIEW_DURATION_MS,
            amplitude = amplitude,
            attributes = TOUCH_ATTRIBUTES,
            cancelCurrent = true
        )
    }

    fun notifyTimerCompletion(amplitude: Int) {
        vibrate(
            durationMs = COMPLETION_DURATION_MS,
            amplitude = amplitude,
            attributes = ALARM_ATTRIBUTES,
            cancelCurrent = false
        )
    }

    private fun vibrate(
        durationMs: Long,
        amplitude: Int,
        attributes: VibrationAttributes,
        cancelCurrent: Boolean
    ) {
        if (!vibrator.hasVibrator()) return

        if (cancelCurrent) {
            vibrator.cancel()
        }
        val effectAmplitude = if (vibrator.hasAmplitudeControl()) {
            amplitude.coerceIn(1, 255)
        } else {
            VibrationEffect.DEFAULT_AMPLITUDE
        }
        vibrator.vibrate(
            VibrationEffect.createOneShot(durationMs, effectAmplitude),
            attributes
        )
    }

    private companion object {
        const val PREVIEW_DURATION_MS = 80L
        const val COMPLETION_DURATION_MS = 220L

        val TOUCH_ATTRIBUTES: VibrationAttributes =
            VibrationAttributes.createForUsage(VibrationAttributes.USAGE_TOUCH)
        val ALARM_ATTRIBUTES: VibrationAttributes =
            VibrationAttributes.createForUsage(VibrationAttributes.USAGE_ALARM)
    }
}
