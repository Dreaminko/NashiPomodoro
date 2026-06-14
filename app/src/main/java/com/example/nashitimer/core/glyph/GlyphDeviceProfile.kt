package com.example.nashitimer.core.glyph

import com.example.nashitimer.domain.model.GlyphChannel

enum class GlyphDeviceProfile(
    val modelCode: String?,
    val defaultProgressChannel: GlyphChannel?,
    val channelLedIndices: Map<GlyphChannel, List<Int>>,
    val supportsGlyphBar: Boolean,
    val sdkFamilyCode: String? = modelCode
) {
    DEVICE_20111(
        "A063",
        GlyphChannel.D,
        channelMap(
            GlyphChannel.A to listOf(0),
            GlyphChannel.B to listOf(1),
            GlyphChannel.C to (2..5).toList(),
            GlyphChannel.D to (7..14).toList(),
            GlyphChannel.E to listOf(6)
        ),
        true
    ),
    DEVICE_22111("A065", GlyphChannel.C, phone2Channels(), true),
    DEVICE_22111_INDIA("AIN065", GlyphChannel.C, phone2Channels(), true),
    DEVICE_23111("A142", GlyphChannel.C, phone2aChannels(), true),
    DEVICE_23113("A142P", GlyphChannel.C, phone2aChannels(), true),
    DEVICE_24111("A059", GlyphChannel.C, phone3aChannels(), true),
    DEVICE_24111_PRO(
        "A059P",
        GlyphChannel.C,
        phone3aChannels(),
        true,
        sdkFamilyCode = "A059"
    ),
    DEVICE_25111(
        "A069",
        GlyphChannel.A,
        channelMap(GlyphChannel.A to (0..5).toList()),
        true
    ),
    MATRIX_23112("A024", null, emptyMap(), false),
    MATRIX_25111P("A069P", null, emptyMap(), false),
    UNKNOWN(null, null, emptyMap(), false);

    val progressChannel: GlyphChannel?
        get() = defaultProgressChannel

    val progressLedIndices: List<Int>
        get() = defaultProgressChannel?.let(channelLedIndices::get).orEmpty()

    val availableProgressChannels: List<GlyphChannel>
        get() = channelLedIndices.keys.toList()

    fun progressLedIndices(channel: GlyphChannel): List<Int> {
        val resolved = channel
            .takeIf { it != GlyphChannel.AUTO && it in channelLedIndices }
            ?: defaultProgressChannel
        return resolved?.let(channelLedIndices::get).orEmpty()
    }

    companion object {
        fun fromModel(model: String): GlyphDeviceProfile {
            val normalized = model.trim().uppercase()
            val knownProfiles = entries.filter { it.modelCode != null }
            return knownProfiles.firstOrNull { it.modelCode == normalized }
                ?: knownProfiles
                    .sortedByDescending { it.modelCode?.length ?: 0 }
                    .firstOrNull { profile ->
                        profile.modelCode?.let { normalized.startsWith(it) } == true
                    }
                ?: UNKNOWN
        }
    }
}

private fun channelMap(
    vararg entries: Pair<GlyphChannel, List<Int>>
): Map<GlyphChannel, List<Int>> = linkedMapOf(*entries)

private fun phone2Channels() = channelMap(
    GlyphChannel.A to (0..1).toList(),
    GlyphChannel.B to listOf(2),
    GlyphChannel.C to (3..23).toList(),
    GlyphChannel.D to (25..32).toList(),
    GlyphChannel.E to listOf(24)
)

private fun phone2aChannels() = channelMap(
    GlyphChannel.A to listOf(25),
    GlyphChannel.B to listOf(24),
    GlyphChannel.C to (0..23).toList()
)

private fun phone3aChannels() = channelMap(
    GlyphChannel.A to (20..30).toList(),
    GlyphChannel.B to (31..35).toList(),
    GlyphChannel.C to (0..19).toList()
)
