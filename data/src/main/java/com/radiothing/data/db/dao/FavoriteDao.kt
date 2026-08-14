package com.radiothing.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.radiothing.data.db.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites")
    fun getAll(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE stationUuid = :stationUuid")
    suspend fun delete(stationUuid: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE stationUuid = :stationUuid)")
    suspend fun exists(stationUuid: String): Boolean

    @Query("SELECT stationUuid FROM favorites")
    fun getAllIds(): Flow<List<String>>
}
