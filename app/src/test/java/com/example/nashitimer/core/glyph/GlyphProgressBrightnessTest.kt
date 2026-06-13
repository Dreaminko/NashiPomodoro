package com.example.nashitimer.core.glyph

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GlyphProgressBrightnessTest {
    @Test
    fun calculate_keepsAllLedsFullyLitAtStart() {
        val brightness = GlyphProgressBrightness.calculate(1f, ledCount = 6)

        assertArrayEquals(IntArray(6) { GlyphProgressBrightness.MAX_BRIGHTNESS }, brightness)
    }

    @Test
    fun calculate_turnsAllLedsOffAtCompletion() {
        val brightness = GlyphProgressBrightness.calculate(0f, ledCount = 6)

        assertArrayEquals(IntArray(6), brightness)
    }

    @Test
    fun calculate_usesGradedBrightnessAtProgressEdge() {
        val brightness = GlyphProgressBrightness.calculate(0.425f, ledCount = 10)

        assertArrayEquals(
            intArrayOf(4_000, 4_000, 4_000, 4_000, 1_000, 0, 0, 0, 0, 0),
            brightness
        )
    }

    @Test
    fun calculate_preservesSmallBrightnessChanges() {
        val earlier = GlyphProgressBrightness.calculate(0.425f, ledCount = 10)
        val later = GlyphProgressBrightness.calculate(0.424f, ledCount = 10)

        assertTrue(earlier[4] > later[4])
        assertTrue(earlier[4] - later[4] in 39..41)
    }

    @Test
    fun calculate_brightnessDecreasesMonotonicallyAcrossLeds() {
        val brightness = GlyphProgressBrightness.calculate(0.63f, ledCount = 24)

        for (index in 0 until brightness.lastIndex) {
            assertTrue(brightness[index] >= brightness[index + 1])
        }
        assertTrue(brightness.all { it in 0..4_000 })
    }

    @Test
    fun calculate_clampsInvalidInputs() {
        assertEquals(4_000, GlyphProgressBrightness.calculate(2f, 1).single())
        assertEquals(0, GlyphProgressBrightness.calculate(-1f, 1).single())
        assertTrue(GlyphProgressBrightness.calculate(0.5f, 0).isEmpty())
    }
}
