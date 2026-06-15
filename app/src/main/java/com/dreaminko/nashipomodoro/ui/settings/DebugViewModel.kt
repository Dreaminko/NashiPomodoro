package com.dreaminko.nashipomodoro.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dreaminko.nashipomodoro.core.glyph.GlyphController
import com.dreaminko.nashipomodoro.core.glyph.GlyphDebugState
import com.dreaminko.nashipomodoro.core.glyph.GlyphDeviceAdapter
import com.dreaminko.nashipomodoro.core.glyph.GlyphEffect
import com.dreaminko.nashipomodoro.data.repository.SettingsRepository
import com.dreaminko.nashipomodoro.domain.model.AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DebugUiState(
    val settings: AppSettings = AppSettings(),
    val manufacturer: String = "",
    val brand: String = "",
    val model: String = "",
    val profile: String = "",
    val progressChannel: String = "",
    val supportsGlyphBar: Boolean = false,
    val glyph: GlyphDebugState = GlyphDebugState()
)

@HiltViewModel
class DebugViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val glyphController: GlyphController,
    adapter: GlyphDeviceAdapter
) : ViewModel() {
    val uiState: StateFlow<DebugUiState> = combine(
        settingsRepository.settings,
        glyphController.debugState
    ) { settings, glyph ->
        DebugUiState(
            settings = settings,
            manufacturer = adapter.manufacturer,
            brand = adapter.brand,
            model = adapter.model,
            profile = adapter.profile.name,
            progressChannel = adapter.profile.progressChannel?.name ?: "N/A",
            supportsGlyphBar = adapter.supportsGlyphBar,
            glyph = glyph
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, DebugUiState())

    init {
        glyphController.init()
    }

    fun setDebugMode(enabled: Boolean) = updateSettings {
        copy(debugModeEnabled = enabled)
    }

    fun setDebugFocusDurationSeconds(seconds: Int) = updateSettings {
        copy(debugFocusDurationSec = seconds.coerceIn(1, 3_600))
    }

    fun showFullGlyph() = glyphController.show(GlyphEffect.FocusProgress(1f))

    fun showHalfGlyph() = glyphController.show(GlyphEffect.FocusProgress(0.5f))

    fun turnOffGlyph() = glyphController.show(GlyphEffect.Off)

    private fun updateSettings(block: AppSettings.() -> AppSettings) {
        viewModelScope.launch { settingsRepository.update(block) }
    }
}
