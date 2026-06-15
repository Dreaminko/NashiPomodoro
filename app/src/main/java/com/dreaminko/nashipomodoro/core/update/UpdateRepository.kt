package com.dreaminko.nashipomodoro.core.update

import com.dreaminko.nashipomodoro.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateRepository @Inject constructor() {
    suspend fun checkForUpdate(): UpdateCheckResult = withContext(Dispatchers.IO) {
        val connection = (URL(LATEST_RELEASE_URL).openConnection() as HttpURLConnection).apply {
            connectTimeout = CONNECTION_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            requestMethod = "GET"
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            setRequestProperty("User-Agent", "NashiPomodoro/${BuildConfig.VERSION_NAME}")
        }

        try {
            if (connection.responseCode !in 200..299) {
                throw UpdateCheckException("GitHub returned HTTP ${connection.responseCode}")
            }

            val release = connection.inputStream.bufferedReader().use { reader ->
                JSONObject(reader.readText())
            }
            val update = release.toAppUpdate()
            if (VersionComparator.isNewer(update.versionName, BuildConfig.VERSION_NAME)) {
                UpdateCheckResult.Available(update)
            } else {
                UpdateCheckResult.UpToDate
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun JSONObject.toAppUpdate(): AppUpdate {
        val tagName = getString("tag_name")
        val assets = getJSONArray("assets")
        val apkAsset = (0 until assets.length())
            .map(assets::getJSONObject)
            .filter { asset ->
                asset.optString("name").endsWith(".apk", ignoreCase = true)
            }
            .maxByOrNull { asset ->
                if (asset.optString("name").startsWith("NashiPomodoro", ignoreCase = true)) {
                    1
                } else {
                    0
                }
            }
            ?: throw UpdateCheckException("The latest release does not contain an APK")

        return AppUpdate(
            versionName = tagName.removePrefix("v").removePrefix("V"),
            releaseName = optString("name").ifBlank { tagName },
            releaseNotes = optString("body"),
            downloadUrl = apkAsset.getString("browser_download_url"),
            fileName = apkAsset.getString("name")
        )
    }

    private companion object {
        const val LATEST_RELEASE_URL =
            "https://api.github.com/repos/Dreaminko/NashiPomodoro/releases/latest"
        const val CONNECTION_TIMEOUT_MS = 15_000
        const val READ_TIMEOUT_MS = 20_000
    }
}

class UpdateCheckException(message: String) : Exception(message)
