package com.yoopi.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton       // Hilt gives us a single instance
class PlaylistCache @Inject constructor(
    private val dao: PlaylistDao
) {
    suspend fun insertBatch(
        playlistId: Long,
        list: List<StreamEntity>
    ) = withContext(Dispatchers.IO) {
        dao.insertStreams(list)
    }
}
