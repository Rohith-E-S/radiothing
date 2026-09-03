package com.radiothing.domain.repository

import com.radiothing.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<AppSettings>
    suspend fun updateCrossfadeDuration(duration: Int)
    suspend fun updateUseAsciiNotification(useAscii: Boolean)
    suspend fun updateBufferSize(size: Int)
    suspend fun updateEnableCache(enabled: Boolean)
    suspend fun updateEnablePreWarm(enabled: Boolean)
}
