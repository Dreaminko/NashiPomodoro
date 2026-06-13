package com.example.nashitimer.core.glyph

enum class GlyphProgressChannel {
    A,
    C,
    D
}

enum class GlyphDeviceProfile(
    val modelCode: String?,
    val progressChannel: GlyphProgressChannel?,
    val progressLedIndices: List<Int>,
    val supportsGlyphBar: Boolean,
    val sdkFamilyCode: String? = modelCode
) {
    DEVICE_20111("A063", GlyphProgressChannel.D, (7..14).toList(), true),
    DEVICE_22111("A065", GlyphProgressChannel.C, (3..18).toList(), true),
    DEVICE_22111_INDIA("AIN065", GlyphProgressChannel.C, (3..18).toList(), true),
    DEVICE_23111("A142", GlyphProgressChannel.C, (0..23).toList(), true),
    DEVICE_23113("A142P", GlyphProgressChannel.C, (0..23).toList(), true),
    DEVICE_24111("A059", GlyphProgressChannel.C, (0..19).toList(), true),
    DEVICE_24111_PRO(
        "A059P",
        GlyphProgressChannel.C,
        (0..19).toList(),
        true,
        sdkFamilyCode = "A059"
    ),
    DEVICE_25111("A069", GlyphProgressChannel.A, (0..5).toList(), true),
    MATRIX_23112("A024", null, emptyList(), false),
    MATRIX_25111P("A069P", null, emptyList(), false),
    UNKNOWN(null, null, emptyList(), false);

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
