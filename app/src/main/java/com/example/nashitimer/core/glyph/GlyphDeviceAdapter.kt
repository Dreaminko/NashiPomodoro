package com.example.nashitimer.core.glyph

import android.os.Build
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlyphDeviceAdapter @Inject constructor() {
    val manufacturer: String
        get() = Build.MANUFACTURER.orEmpty()

    val brand: String
        get() = Build.BRAND.orEmpty()

    val model: String
        get() = Build.MODEL.orEmpty()

    val isNothingDevice: Boolean
        get() = manufacturer.contains("Nothing", ignoreCase = true) ||
            brand.contains("Nothing", ignoreCase = true) ||
            profile != GlyphDeviceProfile.UNKNOWN

    val profile: GlyphDeviceProfile
        get() = GlyphDeviceProfile.fromModel(model)

    val supportsGlyphBar: Boolean
        get() = isNothingDevice && profile.supportsGlyphBar

    val registrationTargets: List<String>
        get() = if (!supportsGlyphBar) {
            emptyList()
        } else {
            listOfNotNull(
                model.trim().takeIf(String::isNotEmpty),
                profile.sdkFamilyCode
            ).distinct()
        }
}
