package com.radiothing.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.radiothing.data.db.dao.FavoriteDao
import com.radiothing.data.db.dao.PlaylistDao
import com.radiothing.data.db.dao.RecentlyPlayedDao
import com.radiothing.data.db.entity.FavoriteEntity
import com.radiothing.data.db.entity.PlaylistEntity
import com.radiothing.data.db.entity.PlaylistStationEntity
import com.radiothing.data.db.entity.RecentlyPlayedEntity

@Database(
    entities = [
        FavoriteEntity::class,
        PlaylistEntity::class,
        PlaylistStationEntity::class,
        RecentlyPlayedEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class RadioDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun recentlyPlayedDao(): RecentlyPlayedDao

    companion object {
        /**
         * v1 had no foreign key on playlist_stations, so deleting a playlist
         * leaked its station rows; since SQLite reuses freed rowids, a new
         * playlist could inherit those orphans. Rebuild the table with the
         * FK + index, dropping orphans first.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "DELETE FROM playlist_stations WHERE playlistId NOT IN (SELECT id FROM playlists)"
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `playlist_stations_new` (
                        `playlistId` INTEGER NOT NULL,
                        `stationUuid` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `url` TEXT NOT NULL,
                        `urlResolved` TEXT NOT NULL,
                        `homepage` TEXT NOT NULL,
                        `favicon` TEXT NOT NULL,
                        `tags` TEXT NOT NULL,
                        `country` TEXT NOT NULL,
                        `countryCode` TEXT NOT NULL,
                        `language` TEXT NOT NULL,
                        `codec` TEXT NOT NULL,
                        `bitrate` INTEGER NOT NULL,
                        `votes` INTEGER NOT NULL,
                        `clickCount` INTEGER NOT NULL,
                        `clickTrend` INTEGER NOT NULL,
                        `lastCheckOk` INTEGER NOT NULL,
                        `orderIndex` INTEGER NOT NULL,
                        PRIMARY KEY(`playlistId`, `stationUuid`),
                        FOREIGN KEY(`playlistId`) REFERENCES `playlists`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO `playlist_stations_new` (
                        `playlistId`, `stationUuid`, `name`, `url`, `urlResolved`,
                        `homepage`, `favicon`, `tags`, `country`, `countryCode`,
                        `language`, `codec`, `bitrate`, `votes`, `clickCount`,
                        `clickTrend`, `lastCheckOk`, `orderIndex`
                    )
                    SELECT
                        `playlistId`, `stationUuid`, `name`, `url`, `urlResolved`,
                        `homepage`, `favicon`, `tags`, `country`, `countryCode`,
                        `language`, `codec`, `bitrate`, `votes`, `clickCount`,
                        `clickTrend`, `lastCheckOk`, `orderIndex`
                    FROM `playlist_stations`
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE `playlist_stations`")
                db.execSQL("ALTER TABLE `playlist_stations_new` RENAME TO `playlist_stations`")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_playlist_stations_playlistId` ON `playlist_stations` (`playlistId`)"
                )
            }
        }
    }
}
