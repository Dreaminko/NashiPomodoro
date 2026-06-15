package com.dreaminko.nashipomodoro.core.update

data class AppUpdate(
    val versionName: String,
    val releaseName: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val fileName: String
)

sealed interface UpdateCheckResult {
    data class Available(val update: AppUpdate) : UpdateCheckResult
    data object UpToDate : UpdateCheckResult
}
