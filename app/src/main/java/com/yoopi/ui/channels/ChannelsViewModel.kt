package com.yoopi.ui.channels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.yoopi.data.PlaylistDao
import com.yoopi.data.StreamsPagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*            // ← NEW
import javax.inject.Inject


@HiltViewModel
class ChannelsViewModel @Inject constructor(
    private val dao: PlaylistDao,          // ← keep a reference (was just `dao`)
    savedState: SavedStateHandle
) : ViewModel() {

    /** the ID that LoginFragment passed via Safe-Args */
    private val playlistId: Long = savedState["playlistId"] ?: 0L

    // ─── Existing paging flow – untouched ──────────────────────────────────────
    val pagingFlow = Pager(
        config = PagingConfig(pageSize = 300, enablePlaceholders = false),
        pagingSourceFactory = { StreamsPagingSource(dao, playlistId) }
    ).flow.cachedIn(viewModelScope)

    // ─── NEW: live counters for Channels / Movies / Series ─────────────────────
    data class Stats(val channels: Int, val movies: Int, val series: Int)

    /* ① counters ---------------------------------------------------------- */
    val statsFlow = combine(
        dao.liveCountFlow  (playlistId),
        dao.movieCountFlow (playlistId),
        dao.seriesCountFlow(playlistId)
    ) { live, movies, series ->
        PlaylistStats(live, movies, series)
    }
}
