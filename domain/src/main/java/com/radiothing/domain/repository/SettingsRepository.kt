package com.radiothing.domain.repository

import com.radiothing.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<AppSettings>
    suspend fun updateBufferSize(size: Int)
}
