package com.yoopi.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Query("SELECT id FROM streams WHERE playlistId = :id LIMIT 1")
    fun flowFirstRow(id: Long): Flow<Long?>


    /* ─────────────── playlists table ─────────────── */

    @Query("SELECT * FROM playlists")
    fun getAll(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists ORDER BY id DESC LIMIT 1")
    suspend fun getLatest(): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PlaylistEntity): Long

    @Delete
    suspend fun delete(entity: PlaylistEntity)

    /* ─────────────── streams table ─────────────── */

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreams(list: List<StreamEntity>)

    @Query("SELECT * FROM streams WHERE playlistId = :id ORDER BY id")
    fun flowStreamsFor(id: Long): Flow<List<StreamEntity>>

    // paging helper
    @Query(
        "SELECT * FROM streams " +
                "WHERE playlistId = :id " +
                "ORDER BY id " +
                "LIMIT :limit OFFSET :offset"
    )
    suspend fun pageStreams(id: Long, limit: Int, offset: Int): List<StreamEntity>

    @Query("SELECT COUNT(*) FROM streams WHERE playlistId = :playlistId")
    suspend fun countStreamsFor(playlistId: Long): Int

    /* ─────── simple live / movie / series counters ─────── */

    @Query(
        "SELECT COUNT(*) FROM streams " +
                "WHERE playlistId = :playlistId AND `group` = 'Live'"
    )
    fun liveCountFlow(playlistId: Long): Flow<Int>

    @Query(
        "SELECT COUNT(*) FROM streams " +
                "WHERE playlistId = :playlistId AND `group` = 'Movie'"
    )
    fun movieCountFlow(playlistId: Long): Flow<Int>

    @Query(
        "SELECT COUNT(*) FROM streams " +
                "WHERE playlistId = :playlistId AND `group` = 'Series'"
    )
    fun seriesCountFlow(playlistId: Long): Flow<Int>
}

