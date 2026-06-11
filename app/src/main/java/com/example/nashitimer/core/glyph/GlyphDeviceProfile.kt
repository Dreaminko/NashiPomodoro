package com.example.nashitimer.core.glyph

enum class GlyphProgressChannel {
    A,
    C,
    D
}

enum class GlyphDeviceProfile(
    val modelCode: String?,
    val progressChannel: GlyphProgressChannel?,
    val supportsGlyphBar: Boolean,
    val sdkFamilyCode: String? = modelCode
) {
    DEVICE_20111("A063", GlyphProgressChannel.D, true),
    DEVICE_22111("A065", GlyphProgressChannel.C, true),
    DEVICE_22111_INDIA("AIN065", GlyphProgressChannel.C, true),
    DEVICE_23111("A142", GlyphProgressChannel.C, true),
    DEVICE_23113("A142P", GlyphProgressChannel.C, true),
    DEVICE_24111("A059", GlyphProgressChannel.C, true),
    DEVICE_24111_PRO("A059P", GlyphProgressChannel.C, true, sdkFamilyCode = "A059"),
    DEVICE_25111("A069", GlyphProgressChannel.A, true),
    MATRIX_23112("A024", null, false),
    MATRIX_25111P("A069P", null, false),
    UNKNOWN(null, null, false);

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
