package com.example.nashitimer.core.glyph

import kotlin.math.roundToInt

internal object GlyphProgressBrightness {
    const val MAX_BRIGHTNESS = 4_000

    fun calculate(remainingFraction: Float, ledCount: Int): IntArray {
        if (ledCount <= 0) return IntArray(0)

        val litLength = remainingFraction.coerceIn(0f, 1f) * ledCount
        return IntArray(ledCount) { ledIndex ->
            val coverage = (litLength - ledIndex).coerceIn(0f, 1f)
            (coverage * MAX_BRIGHTNESS).roundToInt()
        }
    }
}
