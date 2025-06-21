package com.yoopi.ui.login

import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.yoopi.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val dao: PlaylistDao
) : ViewModel() {

    /* ───────────── one-shot events as LiveData ───────────── */

    private val _goToChannels = EventLiveData<Long>()
    val goToChannels get() = _goToChannels          // same type, just shorter

    private val _showError = EventLiveData<String>()
    val showError get() = _showError


    /* ───────────── loading state (unchanged) ───────────── */

    sealed interface LoadState {
        data object Idle    : LoadState
        data object Loading : LoadState
        data class Loaded(val count: Int) : LoadState
        data class Error(val msg: String) : LoadState
    }
    private val _state = MutableLiveData<LoadState>(LoadState.Idle)
    val state: LiveData<LoadState> = _state

    /* ───────────── credential helpers ───────────── */

    fun saveM3u(link: String, ctx: Context) = viewModelScope.launch {
        val entity = PlaylistEntity(type = PlaylistType.M3U, url = link.trim())
        val id     = dao.insert(entity)
        load(entity.copy(id = id))
    }

    fun saveXtream(server: String, user: String, pass: String, ctx: Context) =
        viewModelScope.launch {
            val entity = PlaylistEntity(
                type     = PlaylistType.XTREAM,
                url      = server.trimEnd('/'),
                username = user,
                password = pass
            )
            val id = dao.insert(entity)
            load(entity.copy(id = id))
        }

    /* ───────────── parse + quick navigation ───────────── */

    private fun load(entity: PlaylistEntity) {

        /* 1️⃣  heavy parse on IO */
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { PlaylistParser.parse(entity, dao) }
                .onFailure { err ->
                    withContext(Dispatchers.Main) {
                        _state.value = LoadState.Error(err.message ?: "Parse error")
                        _showError.value = err.localizedMessage ?: "Parse error"
                    }
                }
        }

        /* 2️⃣  wait only for the FIRST row, then navigate */
        viewModelScope.launch {
            _state.value = LoadState.Loading

            dao.flowFirstRow(entity.id)
                .filterNotNull()
                .first()                       // suspend until ≥1 row

            _state.value = LoadState.Loaded(1)
            Log.d("LoginVM", "🚀 Navigate to Channels with id=${entity.id}")
            _goToChannels.value = entity.id    // <- one-shot
        }
    }
}
