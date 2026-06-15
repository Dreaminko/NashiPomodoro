package com.dreaminko.nashipomodoro.core.glyph

import com.dreaminko.nashipomodoro.domain.model.GlyphChannel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GlyphDeviceProfileTest {
    @Test
    fun fromModel_selectsProgressChannelForEachGlyphLayout() {
        assertProfile("A063", GlyphDeviceProfile.DEVICE_20111, GlyphChannel.D)
        assertProfile("A065", GlyphDeviceProfile.DEVICE_22111, GlyphChannel.C)
        assertProfile("AIN065", GlyphDeviceProfile.DEVICE_22111_INDIA, GlyphChannel.C)
        assertProfile("A142", GlyphDeviceProfile.DEVICE_23111, GlyphChannel.C)
        assertProfile("A142P", GlyphDeviceProfile.DEVICE_23113, GlyphChannel.C)
        assertProfile("A059", GlyphDeviceProfile.DEVICE_24111, GlyphChannel.C)
        assertProfile("A059P", GlyphDeviceProfile.DEVICE_24111_PRO, GlyphChannel.C)
        assertProfile("A069", GlyphDeviceProfile.DEVICE_25111, GlyphChannel.A)
    }

    @Test
    fun fromModel_acceptsRegionalSuffixes() {
        val profile = GlyphDeviceProfile.fromModel("a059p europe")

        assertEquals(GlyphDeviceProfile.DEVICE_24111_PRO, profile)
        assertEquals("A059", profile.sdkFamilyCode)
        assertEquals((0..19).toList(), profile.progressLedIndices)
    }

    @Test
    fun channelMappings_matchSdkGlyphIndices() {
        assertEquals((7..14).toList(), GlyphDeviceProfile.DEVICE_20111.progressLedIndices)
        assertEquals(
            (2..5).toList(),
            GlyphDeviceProfile.DEVICE_20111.progressLedIndices(GlyphChannel.C)
        )
        assertEquals(
            (25..32).toList(),
            GlyphDeviceProfile.DEVICE_22111.progressLedIndices(GlyphChannel.D)
        )
        assertEquals(
            listOf(25),
            GlyphDeviceProfile.DEVICE_23111.progressLedIndices(GlyphChannel.A)
        )
        assertEquals(
            (31..35).toList(),
            GlyphDeviceProfile.DEVICE_24111.progressLedIndices(GlyphChannel.B)
        )
    }

    @Test
    fun unsupportedChannel_fallsBackToDeviceDefault() {
        val profile = GlyphDeviceProfile.DEVICE_25111

        assertEquals((0..5).toList(), profile.progressLedIndices(GlyphChannel.AUTO))
        assertEquals((0..5).toList(), profile.progressLedIndices(GlyphChannel.E))
    }

    @Test
    fun supportedProfiles_mapEverySdkLedIndexExactlyOnce() {
        val expectedLedCounts = mapOf(
            GlyphDeviceProfile.DEVICE_20111 to 15,
            GlyphDeviceProfile.DEVICE_22111 to 33,
            GlyphDeviceProfile.DEVICE_22111_INDIA to 33,
            GlyphDeviceProfile.DEVICE_23111 to 26,
            GlyphDeviceProfile.DEVICE_23113 to 26,
            GlyphDeviceProfile.DEVICE_24111 to 36,
            GlyphDeviceProfile.DEVICE_24111_PRO to 36,
            GlyphDeviceProfile.DEVICE_25111 to 6
        )

        expectedLedCounts.forEach { (profile, ledCount) ->
            val indices = profile.channelLedIndices.values.flatten()
            assertEquals(indices.distinct(), indices)
            assertEquals((0 until ledCount).toList(), indices.sorted())
        }
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
        expectedChannel: GlyphChannel
    ) {
        val profile = GlyphDeviceProfile.fromModel(model)
        assertEquals(expectedProfile, profile)
        assertEquals(expectedChannel, profile.progressChannel)
        assertTrue(profile.progressLedIndices.isNotEmpty())
        assertTrue(profile.supportsGlyphBar)
    }
}
