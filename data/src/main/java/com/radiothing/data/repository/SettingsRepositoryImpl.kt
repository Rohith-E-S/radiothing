package com.radiothing.data.repository

import com.radiothing.data.preferences.SettingsDataStore
import com.radiothing.domain.model.AppSettings
import com.radiothing.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: SettingsDataStore
) : SettingsRepository {

    override fun getSettings(): Flow<AppSettings> {
        return dataStore.settings
    }


    override suspend fun updateUseAsciiNotification(useAscii: Boolean) {
        dataStore.updateUseAsciiNotification(useAscii)
    }

    override suspend fun updateBufferSize(size: Int) {
        dataStore.updateBufferSize(size)
    }
}
