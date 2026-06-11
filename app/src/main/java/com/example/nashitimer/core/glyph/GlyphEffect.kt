package com.example.nashitimer.core.glyph

sealed interface GlyphEffect {
    data class FocusProgress(val remainingFraction: Float) : GlyphEffect
    data object ShortBreak : GlyphEffect
    data object LongBreak : GlyphEffect
    data object CompleteFlash : GlyphEffect
    data object Off : GlyphEffect
}
