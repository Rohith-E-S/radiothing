package com.radiothing.app.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.radiothing.data.preferences.SettingsDataStore
import com.radiothing.domain.repository.RecentlyPlayedRepository
import com.radiothing.player.PlayerManager
import com.radiothing.player.PlayerManagerImpl
import com.radiothing.player.haptics.HapticFeedbackManager
import com.radiothing.player.timer.SleepTimerManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
object PlayerModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideSettingsDataStore(dataStore: DataStore<Preferences>): SettingsDataStore = SettingsDataStore(dataStore)

    @Provides
    @Singleton
    fun provideSleepTimerManager(): SleepTimerManager = SleepTimerManager()

    @Provides
    @Singleton
    fun provideHapticFeedbackManager(app: Application): HapticFeedbackManager = HapticFeedbackManager(app)

    @Provides
    @Singleton
    fun providePlayerManager(
        app: Application,
        sleepTimerManager: SleepTimerManager,
        recentlyPlayedRepository: RecentlyPlayedRepository
    ): PlayerManager = PlayerManagerImpl(app, sleepTimerManager, recentlyPlayedRepository)
}
