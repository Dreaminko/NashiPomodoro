package com.example.nashitimer.core.glyph

import android.content.ComponentName
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.nothing.ketchum.GlyphException
import com.nothing.ketchum.GlyphFrame
import com.nothing.ketchum.GlyphManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

data class GlyphDebugState(
    val serviceConnected: Boolean = false,
    val registered: Boolean = false,
    val sessionOpen: Boolean = false,
    val registrationTarget: String? = null,
    val lastEffect: String? = null,
    val lastError: String? = null
)

@Singleton
class GlyphController @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val adapter: GlyphDeviceAdapter
) {
    private val handler = Handler(Looper.getMainLooper())
    private var manager: GlyphManager? = null
    private var sessionOpen = false
    private var pendingEffect: GlyphEffect? = null
    private var lastEffectKey: String? = null
    private val _debugState = MutableStateFlow(GlyphDebugState())
    val debugState: StateFlow<GlyphDebugState> = _debugState.asStateFlow()

    private val callback = object : GlyphManager.Callback {
        override fun onServiceConnected(componentName: ComponentName) {
            val glyphManager = manager ?: return
            _debugState.value = _debugState.value.copy(serviceConnected = true, lastError = null)
            runCatching {
                val registrationTargets = adapter.registrationTargets
                val registeredDevice = registrationTargets.firstOrNull { device ->
                    _debugState.value = _debugState.value.copy(registrationTarget = device)
                    glyphManager.register(device)
                }
                val registered = registeredDevice != null
                _debugState.value = _debugState.value.copy(
                    registered = registered,
                    registrationTarget = registeredDevice ?: registrationTargets.lastOrNull()
                )
                if (registeredDevice == null) {
                    recordError("Glyph registration rejected for targets=$registrationTargets")
                    return
                }
                glyphManager.openSession()
                sessionOpen = true
                _debugState.value = _debugState.value.copy(sessionOpen = true)
                Log.i(
                    TAG,
                    "Glyph session opened for device=$registeredDevice, profile=${adapter.profile}"
                )
                pendingEffect?.also {
                    pendingEffect = null
                    applyEffect(it, force = true)
                }
            }.onFailure { logFailure("opening Glyph session", it) }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            sessionOpen = false
            _debugState.value = _debugState.value.copy(
                serviceConnected = false,
                registered = false,
                sessionOpen = false
            )
            Log.w(TAG, "Glyph service disconnected: $componentName")
        }
    }

    fun init() {
        if (!adapter.supportsGlyphBar) {
            Log.i(
                TAG,
                "Glyph bar disabled for model=${android.os.Build.MODEL}, profile=${adapter.profile}"
            )
            return
        }
        if (manager != null) return
        runCatching {
            manager = GlyphManager.getInstance(context.applicationContext)
            manager?.init(callback)
            Log.i(TAG, "Binding to Glyph service")
        }.onFailure { logFailure("initializing Glyph SDK", it) }
    }

    fun show(effect: GlyphEffect) {
        if (!adapter.supportsGlyphBar) return
        init()
        if (!sessionOpen) {
            pendingEffect = effect
            return
        }
        applyEffect(effect)
    }

    fun release() {
        handler.removeCallbacksAndMessages(null)
        runCatching {
            if (sessionOpen) manager?.closeSession()
        }.onFailure { logFailure("closing Glyph session", it) }
        sessionOpen = false
        _debugState.value = _debugState.value.copy(
            serviceConnected = false,
            registered = false,
            sessionOpen = false
        )
        runCatching { manager?.unInit() }
            .onFailure { logFailure("unbinding Glyph service", it) }
        manager = null
        pendingEffect = null
        lastEffectKey = null
    }

    private fun applyEffect(effect: GlyphEffect, force: Boolean = false) {
        val glyphManager = manager ?: return
        val key = effectKey(effect)
        if (!force && key == lastEffectKey) return
        lastEffectKey = key
        _debugState.value = _debugState.value.copy(lastEffect = key, lastError = null)

        runCatching {
            when (effect) {
                GlyphEffect.Off -> glyphManager.turnOff()
                GlyphEffect.CompleteFlash -> flashAll(glyphManager)
                is GlyphEffect.FocusProgress -> showFocus(glyphManager, effect.remainingFraction)
                GlyphEffect.ShortBreak -> animateBreak(glyphManager, 4_000)
                GlyphEffect.LongBreak -> animateBreak(glyphManager, 6_000)
            }
        }.onFailure { logFailure("applying $effect", it) }
    }

    private fun showFocus(glyphManager: GlyphManager, remainingFraction: Float) {
        val builder = glyphManager.getGlyphFrameBuilder()
            .buildPeriod(2_000)
            .buildCycles(1)
        when (adapter.profile.progressChannel) {
            GlyphProgressChannel.A -> builder.buildChannelA()
            GlyphProgressChannel.C -> builder.buildChannelC()
            GlyphProgressChannel.D -> builder.buildChannelD()
            null -> return
        }
        val frame = builder.build()
        val percent = (remainingFraction.coerceIn(0f, 1f) * 100).roundToInt()
        glyphManager.displayProgressAndToggle(frame, percent, false)
    }

    private fun animateBreak(glyphManager: GlyphManager, periodMs: Int) {
        val frame = allChannelsBuilder(glyphManager)
            .buildPeriod(periodMs)
            .buildInterval(250)
            .buildCycles(1_000)
            .build()
        glyphManager.animate(frame)
    }

    private fun flashAll(glyphManager: GlyphManager) {
        handler.removeCallbacksAndMessages(null)
        val frame = allChannelsFrame(glyphManager)
        glyphManager.turnOff()
        handler.post { runCatching { glyphManager.toggle(frame) } }
        handler.postDelayed({ runCatching { glyphManager.turnOff() } }, 300)
        handler.postDelayed({ runCatching { glyphManager.toggle(frame) } }, 600)
        handler.postDelayed({ runCatching { glyphManager.turnOff() } }, 900)
    }

    private fun allChannelsFrame(glyphManager: GlyphManager): GlyphFrame =
        allChannelsBuilder(glyphManager).build()

    private fun allChannelsBuilder(glyphManager: GlyphManager): GlyphFrame.Builder =
        glyphManager.getGlyphFrameBuilder()
            .buildChannelA()
            .buildChannelB()
            .buildChannelC()
            .buildChannelD()
            .buildChannelE()

    private fun effectKey(effect: GlyphEffect): String = when (effect) {
        is GlyphEffect.FocusProgress ->
            "focus:${(effect.remainingFraction.coerceIn(0f, 1f) * 100).roundToInt()}"
        GlyphEffect.ShortBreak -> "short-break"
        GlyphEffect.LongBreak -> "long-break"
        GlyphEffect.CompleteFlash -> "complete:${System.nanoTime()}"
        GlyphEffect.Off -> "off"
    }

    private fun logFailure(action: String, error: Throwable) {
        val cause = if (error is GlyphException) error else error.cause ?: error
        recordError("Failed while $action: ${cause.message}", cause)
    }

    private fun recordError(message: String, error: Throwable? = null) {
        _debugState.value = _debugState.value.copy(lastError = message)
        Log.e(TAG, message, error)
    }

    private companion object {
        const val TAG = "GlyphController"
    }
}
