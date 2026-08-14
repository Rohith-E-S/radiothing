package com.radiothing.app.di

import com.radiothing.data.repository.FavoriteRepositoryImpl
import com.radiothing.data.repository.PlaylistRepositoryImpl
import com.radiothing.data.repository.RecentlyPlayedRepositoryImpl
import com.radiothing.data.repository.SettingsRepositoryImpl
import com.radiothing.data.repository.StationRepositoryImpl
import com.radiothing.domain.repository.FavoriteRepository
import com.radiothing.domain.repository.PlaylistRepository
import com.radiothing.domain.repository.RecentlyPlayedRepository
import com.radiothing.domain.repository.SettingsRepository
import com.radiothing.domain.repository.StationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindStationRepository(impl: StationRepositoryImpl): StationRepository

    @Binds
    @Singleton
    abstract fun bindFavoriteRepository(impl: FavoriteRepositoryImpl): FavoriteRepository

    @Binds
    @Singleton
    abstract fun bindPlaylistRepository(impl: PlaylistRepositoryImpl): PlaylistRepository

    @Binds
    @Singleton
    abstract fun bindRecentlyPlayedRepository(impl: RecentlyPlayedRepositoryImpl): RecentlyPlayedRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
