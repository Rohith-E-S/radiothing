package com.radiothing.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.radiothing.data.db.entity.PlaylistEntity
import com.radiothing.data.db.entity.PlaylistStationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY updatedAt DESC")
    fun getPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    fun getPlaylistById(playlistId: Long): Flow<PlaylistEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Query("DELETE FROM playlist_stations WHERE playlistId = :playlistId")
    suspend fun deleteAllPlaylistStations(playlistId: Long)

    /**
     * Deletes a playlist and its stations atomically. The FK cascade on
     * playlist_stations also covers this, but the explicit delete keeps the
     * table clean even if FK enforcement is ever relaxed.
     */
    @Transaction
    suspend fun deletePlaylistWithStations(playlistId: Long) {
        deleteAllPlaylistStations(playlistId)
        deletePlaylist(playlistId)
    }

    @Query("SELECT * FROM playlist_stations WHERE playlistId = :playlistId ORDER BY orderIndex ASC")
    fun getPlaylistStations(playlistId: Long): Flow<List<PlaylistStationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistStation(playlistStation: PlaylistStationEntity)

    @Query("DELETE FROM playlist_stations WHERE playlistId = :playlistId AND stationUuid = :stationUuid")
    suspend fun removePlaylistStation(playlistId: Long, stationUuid: String)

    @Query("UPDATE playlist_stations SET orderIndex = :newOrder WHERE playlistId = :playlistId AND stationUuid = :stationUuid")
    suspend fun updateStationOrder(playlistId: Long, stationUuid: String, newOrder: Int)

    @Query("SELECT COUNT(*) FROM playlist_stations WHERE playlistId = :playlistId")
    suspend fun getStationCount(playlistId: Long): Int

    @Query("SELECT EXISTS(SELECT 1 FROM playlist_stations WHERE playlistId = :playlistId AND stationUuid = :stationUuid)")
    suspend fun stationExists(playlistId: Long, stationUuid: String): Int

    @Query("UPDATE playlists SET updatedAt = :timestamp WHERE id = :playlistId")
    suspend fun touchPlaylist(playlistId: Long, timestamp: Long)

    @Query("SELECT playlistId, COUNT(*) as stationCount FROM playlist_stations GROUP BY playlistId")
    fun getPlaylistStationCounts(): Flow<List<PlaylistStationCount>>
}

data class PlaylistStationCount(
    val playlistId: Long,
    val stationCount: Int
)
