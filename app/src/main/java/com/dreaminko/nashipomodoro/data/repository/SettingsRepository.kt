package com.dreaminko.nashipomodoro.data.repository

import com.dreaminko.nashipomodoro.data.local.SettingsStore
import com.dreaminko.nashipomodoro.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val store: SettingsStore
) {
    val settings: Flow<AppSettings> = store.settings
    suspend fun update(transform: AppSettings.() -> AppSettings) = store.update(transform)
}
