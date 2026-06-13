package com.example.nashitimer.core.glyph

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GlyphDeviceProfileTest {
    @Test
    fun fromModel_selectsProgressChannelForEachGlyphLayout() {
        assertProfile("A063", GlyphDeviceProfile.DEVICE_20111, GlyphProgressChannel.D)
        assertProfile("A065", GlyphDeviceProfile.DEVICE_22111, GlyphProgressChannel.C)
        assertProfile("AIN065", GlyphDeviceProfile.DEVICE_22111_INDIA, GlyphProgressChannel.C)
        assertProfile("A142", GlyphDeviceProfile.DEVICE_23111, GlyphProgressChannel.C)
        assertProfile("A142P", GlyphDeviceProfile.DEVICE_23113, GlyphProgressChannel.C)
        assertProfile("A059", GlyphDeviceProfile.DEVICE_24111, GlyphProgressChannel.C)
        assertProfile("A059P", GlyphDeviceProfile.DEVICE_24111_PRO, GlyphProgressChannel.C)
        assertProfile("A069", GlyphDeviceProfile.DEVICE_25111, GlyphProgressChannel.A)
    }

    @Test
    fun fromModel_acceptsRegionalSuffixes() {
        val profile = GlyphDeviceProfile.fromModel("a059p europe")

        assertEquals(GlyphDeviceProfile.DEVICE_24111_PRO, profile)
        assertEquals("A059", profile.sdkFamilyCode)
        assertEquals((0..19).toList(), profile.progressLedIndices)
    }

    @Test
    fun matrixAndUnknownModels_doNotUseGlyphBarApi() {
        assertFalse(GlyphDeviceProfile.fromModel("A024").supportsGlyphBar)
        assertFalse(GlyphDeviceProfile.fromModel("A069P").supportsGlyphBar)
        assertEquals(GlyphDeviceProfile.UNKNOWN, GlyphDeviceProfile.fromModel("future-model"))
    }

    private fun assertProfile(
        model: String,
        expectedProfile: GlyphDeviceProfile,
        expectedChannel: GlyphProgressChannel
    ) {
        val profile = GlyphDeviceProfile.fromModel(model)
        assertEquals(expectedProfile, profile)
        assertEquals(expectedChannel, profile.progressChannel)
        assertTrue(profile.progressLedIndices.isNotEmpty())
        assertTrue(profile.supportsGlyphBar)
    }
}
