package com.dreaminko.nashipomodoro.core.glyph

import com.dreaminko.nashipomodoro.domain.model.GlyphProgressDirection

internal fun orderedGlyphLedIndices(
    ledIndices: List<Int>,
    direction: GlyphProgressDirection
): List<Int> = when (direction) {
    GlyphProgressDirection.FORWARD -> ledIndices
    GlyphProgressDirection.REVERSE -> ledIndices.asReversed()
}
