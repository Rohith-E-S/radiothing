package com.radiothing.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.radiothing.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val CROSSFADE_DURATION = intPreferencesKey("crossfade_duration")
        val USE_ASCII_NOTIFICATION = booleanPreferencesKey("use_ascii_notification")
        val BUFFER_SIZE = intPreferencesKey("buffer_size")
    }

    val settings: Flow<AppSettings> = dataStore.data.map { preferences ->
        AppSettings(
            crossfadeDuration = preferences[CROSSFADE_DURATION] ?: 3,
            useAsciiNotification = preferences[USE_ASCII_NOTIFICATION] ?: false,
            bufferSize = preferences[BUFFER_SIZE] ?: 5000
        )
    }

    suspend fun updateCrossfadeDuration(duration: Int) {
        dataStore.edit { preferences ->
            preferences[CROSSFADE_DURATION] = duration
        }
    }

    suspend fun updateUseAsciiNotification(useAscii: Boolean) {
        dataStore.edit { preferences ->
            preferences[USE_ASCII_NOTIFICATION] = useAscii
        }
    }

    suspend fun updateBufferSize(size: Int) {
        dataStore.edit { preferences ->
            preferences[BUFFER_SIZE] = size
        }
    }
}
