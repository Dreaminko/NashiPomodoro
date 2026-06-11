package com.example.nashitimer.domain.model

enum class TimerPhase(val label: String) {
    IDLE("Ready"),
    FOCUS("Focus"),
    SHORT_BREAK("Short break"),
    LONG_BREAK("Long break"),
    PAUSED("Paused")
}
