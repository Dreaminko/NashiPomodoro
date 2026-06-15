package com.dreaminko.nashipomodoro.core.glyph

import android.content.ComponentName
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import com.dreaminko.nashipomodoro.domain.model.GlyphChannel
import com.dreaminko.nashipomodoro.domain.model.GlyphProgressDirection
import com.nothing.ketchum.GlyphException
import com.nothing.ketchum.GlyphFrame
import com.nothing.ketchum.GlyphManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

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
    private var progressAnchorRemainingMs = 0L
    private var progressAnchorElapsedMs = 0L
    private var progressTotalMs = 0L
    private var progressChannel = GlyphChannel.AUTO
    private var progressDirection = GlyphProgressDirection.FORWARD
    private var progressSource = GlyphProgressSource.FOCUS
    private var nextProgressFrameElapsedMs = 0L
    private var lastProgressFrame: IntArray? = null
    private val _debugState = MutableStateFlow(GlyphDebugState())
    val debugState: StateFlow<GlyphDebugState> = _debugState.asStateFlow()
    private val progressFrameRunnable = object : Runnable {
        override fun run() {
            if (!sessionOpen || progressTotalMs <= 0L) return
            val elapsedMs = SystemClock.elapsedRealtime() - progressAnchorElapsedMs
            val remainingMs = (progressAnchorRemainingMs - elapsedMs).coerceAtLeast(0L)
            manager?.let { glyphManager ->
                runCatching {
                    displayFocusFrame(
                        glyphManager,
                        remainingMs,
                        progressTotalMs,
                        progressChannel,
                        progressDirection
                    )
                }.onFailure {
                    stopProgressAnimation()
                    logFailure("updating focus progress", it)
                }
            }
            if (remainingMs > 0L && progressTotalMs > 0L) {
                nextProgressFrameElapsedMs += PROGRESS_FRAME_INTERVAL_MS
                val now = SystemClock.uptimeMillis()
                if (nextProgressFrameElapsedMs <= now) {
                    nextProgressFrameElapsedMs = now + PROGRESS_FRAME_INTERVAL_MS
                }
                handler.postAtTime(this, nextProgressFrameElapsedMs)
            }
        }
    }

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
            stopProgressAnimation()
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
        if (Looper.myLooper() != Looper.getMainLooper()) {
            handler.post(::init)
            return
        }
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
        if (Looper.myLooper() != Looper.getMainLooper()) {
            handler.post { show(effect) }
            return
        }
        if (!adapter.supportsGlyphBar) return
        init()
        if (!sessionOpen) {
            pendingEffect = effect
            return
        }
        applyEffect(effect)
    }

    fun release() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            handler.post(::release)
            return
        }
        handler.removeCallbacksAndMessages(null)
        stopProgressAnimation()
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
                GlyphEffect.Off -> {
                    stopProgressAnimation()
                    glyphManager.turnOff()
                }
                GlyphEffect.CompleteFlash -> {
                    stopProgressAnimation()
                    flashAll(glyphManager)
                }
                is GlyphEffect.FocusProgress -> showFocus(glyphManager, effect)
                GlyphEffect.ShortBreak -> {
                    stopProgressAnimation()
                    animateBreak(glyphManager, 4_000)
                }
                GlyphEffect.LongBreak -> {
                    stopProgressAnimation()
                    animateBreak(glyphManager, 6_000)
                }
            }
        }.onFailure { logFailure("applying $effect", it) }
    }

    private fun showFocus(glyphManager: GlyphManager, effect: GlyphEffect.FocusProgress) {
        if (!effect.animate) {
            stopProgressAnimation()
            displayFocusFrame(
                glyphManager,
                effect.remainingMs,
                effect.totalMs,
                effect.channel,
                effect.direction
            )
            return
        }

        val totalMs = effect.totalMs.coerceAtLeast(0L)
        if (totalMs <= 0L) {
            stopProgressAnimation()
            return
        }

        val now = SystemClock.elapsedRealtime()
        val requestedRemainingMs = effect.remainingMs.coerceIn(0L, totalMs)
        val animationRunning =
            progressTotalMs == totalMs &&
                progressChannel == effect.channel &&
                progressDirection == effect.direction &&
                progressSource == effect.source
        progressAnchorRemainingMs = if (animationRunning) {
            minOf(requestedRemainingMs, interpolatedRemainingMs(now))
        } else {
            requestedRemainingMs
        }
        progressAnchorElapsedMs = now
        progressTotalMs = totalMs
        progressChannel = effect.channel
        progressDirection = effect.direction
        progressSource = effect.source

        if (!animationRunning) {
            lastProgressFrame = null
            nextProgressFrameElapsedMs = SystemClock.uptimeMillis()
            progressFrameRunnable.run()
        }
    }

    private fun interpolatedRemainingMs(nowElapsedMs: Long): Long {
        val elapsedMs = (nowElapsedMs - progressAnchorElapsedMs).coerceAtLeast(0L)
        return (progressAnchorRemainingMs - elapsedMs).coerceAtLeast(0L)
    }

    private fun displayFocusFrame(
        glyphManager: GlyphManager,
        remainingMs: Long,
        totalMs: Long,
        channel: GlyphChannel,
        direction: GlyphProgressDirection
    ) {
        val ledIndices = orderedGlyphLedIndices(
            adapter.profile.progressLedIndices(channel),
            direction
        )
        if (ledIndices.isEmpty() || totalMs <= 0L) return

        val brightness = GlyphProgressBrightness.calculate(
            remainingFraction = (remainingMs.toDouble() / totalMs).toFloat(),
            ledCount = ledIndices.size
        )
        if (!GlyphProgressBrightness.shouldUpdate(lastProgressFrame, brightness)) return
        lastProgressFrame = brightness

        val builder = glyphManager.getGlyphFrameBuilder()
        ledIndices.forEachIndexed { position, channelIndex ->
            builder.buildChannel(channelIndex, brightness[position])
        }
        glyphManager.setFrameColors(builder.build().channel)
    }

    private fun stopProgressAnimation() {
        handler.removeCallbacks(progressFrameRunnable)
        progressTotalMs = 0L
        progressChannel = GlyphChannel.AUTO
        progressDirection = GlyphProgressDirection.FORWARD
        progressSource = GlyphProgressSource.FOCUS
        nextProgressFrameElapsedMs = 0L
        lastProgressFrame = null
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
        is GlyphEffect.FocusProgress -> {
            "progress:${effect.source}:${effect.remainingMs}:${effect.totalMs}:" +
                "${effect.channel}:${effect.direction}:${effect.animate}"
        }
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
        const val PROGRESS_FRAME_INTERVAL_MS = 250L
    }
}
