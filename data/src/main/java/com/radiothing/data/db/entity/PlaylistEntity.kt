package com.radiothing.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.radiothing.domain.model.Playlist

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain(): Playlist {
        return Playlist(
            id = id,
            name = name,
            description = description,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
