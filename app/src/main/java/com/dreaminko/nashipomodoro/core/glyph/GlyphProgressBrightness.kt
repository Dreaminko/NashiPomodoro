package com.dreaminko.nashipomodoro.core.glyph

import kotlin.math.roundToInt
import kotlin.math.abs

internal object GlyphProgressBrightness {
    const val MAX_BRIGHTNESS = 4_000
    const val MIN_STABLE_BRIGHTNESS = 800
    private const val MIN_UPDATE_DELTA = 32

    fun calculate(remainingFraction: Float, ledCount: Int): IntArray {
        if (ledCount <= 0) return IntArray(0)

        val litLength = remainingFraction.coerceIn(0f, 1f) * ledCount
        return IntArray(ledCount) { ledIndex ->
            val coverage = (litLength - ledIndex).coerceIn(0f, 1f)
            if (coverage <= 0f) {
                0
            } else {
                (smoothstep(coverage) * MAX_BRIGHTNESS)
                    .roundToInt()
                    .coerceAtLeast(MIN_STABLE_BRIGHTNESS)
            }
        }
    }

    fun shouldUpdate(previous: IntArray?, next: IntArray): Boolean {
        if (previous == null || previous.size != next.size) return true
        return next.indices.any { index ->
            val wasLit = previous[index] > 0
            val isLit = next[index] > 0
            wasLit != isLit || abs(previous[index] - next[index]) >= MIN_UPDATE_DELTA
        }
    }

    private fun smoothstep(value: Float): Float = value * value * (3f - 2f * value)
}
