package com.radiothing.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.radiothing.data.db.entity.RecentlyPlayedEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentlyPlayedDao {
    @Query("SELECT * FROM recently_played ORDER BY playedAt DESC LIMIT 50")
    fun getAll(): Flow<List<RecentlyPlayedEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recentlyPlayed: RecentlyPlayedEntity)

    @Query("DELETE FROM recently_played WHERE stationUuid NOT IN (SELECT stationUuid FROM recently_played ORDER BY playedAt DESC LIMIT 50)")
    suspend fun deleteOldest()

    @Query("DELETE FROM recently_played")
    suspend fun clear()
}
