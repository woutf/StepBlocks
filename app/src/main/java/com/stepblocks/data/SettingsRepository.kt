package com.stepblocks.data

import kotlinx.coroutines.flow.Flow

class SettingsRepository(private val settingsDao: SettingsDao) {
    val settings: Flow<Settings?> = settingsDao.getSettings()

    suspend fun updateSettings(settings: Settings) {
        settingsDao.insert(settings)
    }
}
