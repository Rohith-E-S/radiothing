package com.radiothing.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.radiothing.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class SettingsDataStoreTest {

    private fun dataStoreWith(dataFlow: Flow<Preferences>): DataStore<Preferences> {
        return object : DataStore<Preferences> {
            override val data: Flow<Preferences> = dataFlow
            override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
                return transform(emptyPreferences())
            }
        }
    }

    @Test
    fun `corrupt preferences file falls back to defaults instead of killing the flow`() = runTest {
        val store = SettingsDataStore(
            dataStoreWith(flow { throw IOException("corrupt preferences file") })
        )

        val settings = store.settings.first()

        assertEquals(AppSettings(), settings)
        assertEquals(3, settings.crossfadeDuration)
        assertEquals(5000, settings.bufferSize)
        assertTrue(settings.enableCache)
    }

    @Test
    fun `non-io exceptions still propagate to the collector`() = runTest {
        val store = SettingsDataStore(
            dataStoreWith(flow { throw IllegalStateException("boom") })
        )

        val result = runCatching { store.settings.first() }

        assertTrue(result.isFailure)
    }
}
