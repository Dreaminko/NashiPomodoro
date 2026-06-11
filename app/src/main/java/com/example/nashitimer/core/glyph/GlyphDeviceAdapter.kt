package com.example.nashitimer.core.glyph

import android.os.Build
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlyphDeviceAdapter @Inject constructor() {
    val isNothingDevice: Boolean
        get() = Build.MANUFACTURER.equals("Nothing", ignoreCase = true) ||
            Build.BRAND.equals("Nothing", ignoreCase = true)

    val progressChannelName: String
        get() = when {
            Build.MODEL.contains("A063", ignoreCase = true) -> "D1"
            else -> "C1"
        }
}
