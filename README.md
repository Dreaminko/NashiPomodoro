# NashiPomodoro

[简体中文](README_ZH.md) | [English](README.md) | [日本語](README_JA.md)

NashiPomodoro is an Android Pomodoro timer designed for Nothing Phone. It combines focus timers, task management, and statistics with the Glyph Interface, using the lights on the back of the phone to visualize focus and break progress.

## Important: Disable the System Glyph Progress Feature

> [!IMPORTANT]
> Before using this app on a Nothing Phone, you must disable the built-in **Glyph Progress** feature in the Nothing OS system settings.
>
> The system progress feature occupies the Glyph Interface and conflicts with NashiPomodoro's light controls. This may prevent the app's progress lights from appearing, cause them to display incorrectly, or stop the timer and Glyph effects from working together properly.

Menu names may vary slightly between Nothing OS versions. Open the system **Glyph Interface** settings, find the built-in progress or progress tracking feature, disable it, and then launch NashiPomodoro.

## Features

- Configurable focus, short break, and long break durations
- Automatic Pomodoro phase transitions with a configurable long break interval
- Glyph lights display the remaining progress for focus sessions, short breaks, and long breaks

## Requirements

- Android 14 (API 34) or later
- A Nothing Phone with a supported Glyph light strip is recommended
- Notification, vibration, and Glyph Interface permissions

The basic Pomodoro features can run on other devices that meet the Android version requirement, but Glyph effects are enabled only on Nothing Phone models supported by the codebase.

## Development Environment

- Android Studio
- JDK 21
- Android SDK 36.1
- Gradle 9.5

## Permissions

- **Notifications**: Displays the foreground service notification while the timer is running.
- **Foreground service and wake lock**: Keeps the timer running reliably while the app is in the background.
- **Vibration**: Provides haptic feedback when a focus or break phase ends.
- **Glyph**: Controls Glyph Interface effects on supported Nothing Phone models.

## Translation

The project uses standard Android string resources for localization. See [TRANSLATING.md](TRANSLATING.md) to add or maintain a translation.
