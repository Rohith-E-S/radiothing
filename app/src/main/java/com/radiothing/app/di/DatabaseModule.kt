package com.radiothing.app.di

import android.app.Application
import androidx.room.Room
import com.radiothing.data.db.RadioDatabase
import com.radiothing.data.db.dao.FavoriteDao
import com.radiothing.data.db.dao.PlaylistDao
import com.radiothing.data.db.dao.RecentlyPlayedDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): RadioDatabase {
        return Room.databaseBuilder(
            app,
            RadioDatabase::class.java,
            "radiothing.db"
        ).build()
    }

    @Provides
    fun provideFavoriteDao(db: RadioDatabase): FavoriteDao = db.favoriteDao()

    @Provides
    fun providePlaylistDao(db: RadioDatabase): PlaylistDao = db.playlistDao()

    @Provides
    fun provideRecentlyPlayedDao(db: RadioDatabase): RecentlyPlayedDao = db.recentlyPlayedDao()
}
