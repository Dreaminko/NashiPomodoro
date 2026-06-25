package com.dreaminko.nashipomodoro.core.glyph

import com.dreaminko.nashipomodoro.domain.model.GlyphChannel
import com.dreaminko.nashipomodoro.domain.model.GlyphProgressDirection

enum class GlyphProgressSource {
    FOCUS,
    SHORT_BREAK,
    LONG_BREAK
}

sealed interface GlyphEffect {
    data class FocusProgress(
        val remainingMs: Long,
        val totalMs: Long,
        val channel: GlyphChannel = GlyphChannel.AUTO,
        val direction: GlyphProgressDirection = GlyphProgressDirection.FORWARD,
        val source: GlyphProgressSource = GlyphProgressSource.FOCUS,
        val animate: Boolean = true
    ) : GlyphEffect {
        constructor(remainingFraction: Float) : this(
            remainingMs = (remainingFraction.coerceIn(0f, 1f) * STATIC_TOTAL_MS).toLong(),
            totalMs = STATIC_TOTAL_MS,
            channel = GlyphChannel.AUTO,
            direction = GlyphProgressDirection.FORWARD,
            source = GlyphProgressSource.FOCUS,
            animate = false
        )

        val remainingFraction: Float
            get() = if (totalMs <= 0L) {
                0f
            } else {
                (remainingMs.toDouble() / totalMs).toFloat().coerceIn(0f, 1f)
            }

        private companion object {
            const val STATIC_TOTAL_MS = 1_000L
        }
    }

    data object CompleteFlash : GlyphEffect
    data object Off : GlyphEffect
}
