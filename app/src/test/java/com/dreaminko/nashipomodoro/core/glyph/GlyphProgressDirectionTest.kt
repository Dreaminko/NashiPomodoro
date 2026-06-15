package com.dreaminko.nashipomodoro.core.glyph

import com.dreaminko.nashipomodoro.domain.model.GlyphProgressDirection
import org.junit.Assert.assertEquals
import org.junit.Test

class GlyphProgressDirectionTest {
    @Test
    fun forward_preservesSdkLedOrder() {
        val indices = listOf(3, 4, 5, 6)

        assertEquals(
            indices,
            orderedGlyphLedIndices(indices, GlyphProgressDirection.FORWARD)
        )
    }

    @Test
    fun reverse_invertsSdkLedOrder() {
        assertEquals(
            listOf(6, 5, 4, 3),
            orderedGlyphLedIndices(
                listOf(3, 4, 5, 6),
                GlyphProgressDirection.REVERSE
            )
        )
    }
}
