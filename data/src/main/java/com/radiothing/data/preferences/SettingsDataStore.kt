package com.radiothing.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import com.radiothing.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class SettingsDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val USE_ASCII_NOTIFICATION = booleanPreferencesKey("use_ascii_notification")
        val BUFFER_SIZE = intPreferencesKey("buffer_size")
    }

    /**
     * A corrupt/unreadable preferences file surfaces as IOException on
     * dataStore.data — without handling it would kill every collector of this
     * flow (settings screen, playback service) until app restart. Fall back to
     * defaults, matching the documented DataStore error strategy.
     */
    val settings: Flow<AppSettings> = dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { preferences ->
            AppSettings(
                useAsciiNotification = preferences[USE_ASCII_NOTIFICATION] ?: false,
                bufferSize = preferences[BUFFER_SIZE] ?: 5000,
            )
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
