package com.example.nashitimer.core.glyph

import android.content.ComponentName
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.nothing.ketchum.Common
import com.nothing.ketchum.Glyph
import com.nothing.ketchum.GlyphException
import com.nothing.ketchum.GlyphFrame
import com.nothing.ketchum.GlyphManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

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

    private val callback = object : GlyphManager.Callback {
        override fun onServiceConnected(componentName: ComponentName) {
            val glyphManager = manager ?: return
            runCatching {
                val device = currentDevice()
                val registered = if (device == null) glyphManager.register() else glyphManager.register(device)
                if (!registered) {
                    Log.e(TAG, "Glyph registration rejected for device=${device ?: "auto"}")
                    return
                }
                glyphManager.openSession()
                sessionOpen = true
                Log.i(TAG, "Glyph session opened for device=${device ?: "auto"}")
                pendingEffect?.also {
                    pendingEffect = null
                    applyEffect(it, force = true)
                }
            }.onFailure { logFailure("opening Glyph session", it) }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            sessionOpen = false
            Log.w(TAG, "Glyph service disconnected: $componentName")
        }
    }

    fun init() {
        if (!adapter.isNothingDevice) {
            Log.i(TAG, "Glyph disabled on ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
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
        if (!adapter.isNothingDevice) return
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

        runCatching {
            when (effect) {
                GlyphEffect.Off -> glyphManager.turnOff()
                GlyphEffect.CompleteFlash -> flashAll(glyphManager)
                is GlyphEffect.FocusProgress -> showFocus(glyphManager, effect.progress)
                GlyphEffect.ShortBreak -> animateBreak(glyphManager, 4_000)
                GlyphEffect.LongBreak -> animateBreak(glyphManager, 6_000)
            }
        }.onFailure { logFailure("applying $effect", it) }
    }

    private fun showFocus(glyphManager: GlyphManager, progress: Float) {
        val frame = glyphManager.getGlyphFrameBuilder()
            .buildChannelA()
            .buildChannelB()
            .buildPeriod(2_000)
            .buildCycles(1)
            .build()
        val percent = (progress.coerceIn(0f, 1f) * 100).roundToInt()
        glyphManager.displayProgressAndToggle(frame, percent, false)
    }

    private fun animateBreak(glyphManager: GlyphManager, periodMs: Int) {
        val frame = glyphManager.getGlyphFrameBuilder()
            .buildChannelA()
            .buildChannelB()
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
        glyphManager.getGlyphFrameBuilder()
            .buildChannelA()
            .buildChannelB()
            .buildChannelC()
            .buildChannelD()
            .buildChannelE()
            .build()

    private fun effectKey(effect: GlyphEffect): String = when (effect) {
        is GlyphEffect.FocusProgress -> "focus:${(effect.progress.coerceIn(0f, 1f) * 100).roundToInt()}"
        GlyphEffect.ShortBreak -> "short-break"
        GlyphEffect.LongBreak -> "long-break"
        GlyphEffect.CompleteFlash -> "complete:${System.nanoTime()}"
        GlyphEffect.Off -> "off"
    }

    private fun currentDevice(): String? = when {
        Common.is20111() -> Glyph.DEVICE_20111
        Common.is22111() -> Glyph.DEVICE_22111
        Common.is23111() -> Glyph.DEVICE_23111
        Common.is23113() -> Glyph.DEVICE_23113
        Common.is24111() -> Glyph.DEVICE_24111
        Common.is25111() -> Glyph.DEVICE_25111
        else -> null
    }

    private fun logFailure(action: String, error: Throwable) {
        val cause = if (error is GlyphException) error else error.cause ?: error
        Log.e(TAG, "Failed while $action: ${cause.message}", cause)
    }

    private companion object {
        const val TAG = "GlyphController"
    }
}
