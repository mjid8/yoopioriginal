package com.yoopi.data

import androidx.paging.PagingSource
import androidx.paging.PagingState

class StreamsPagingSource(
    private val dao: PlaylistDao,
    private val playlistId: Long
) : PagingSource<Int, StreamEntity>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StreamEntity> {
        val page   = params.key ?: 0
        val limit  = params.loadSize
        val offset = page * limit
        val data   = dao.pageStreams(playlistId, limit, offset)
        return LoadResult.Page(
            data     = data,
            prevKey  = if (page == 0) null else page - 1,
            nextKey  = if (data.size < limit) null else page + 1
        )
    }

    override fun getRefreshKey(state: PagingState<Int, StreamEntity>) = 0
}
