package com.yoopi.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yoopi.data.PlaylistDao
import com.yoopi.data.PlaylistEntity
import com.yoopi.data.PlaylistType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val dao: PlaylistDao
) : ViewModel() {

    fun saveXtream(server: String, user: String, pass: String) = viewModelScope.launch {
        dao.insert(
            PlaylistEntity(
                type = PlaylistType.XTREAM,
                url = server.trimEnd('/'),
                username = user,
                password = pass
            )
        )
    }

    fun saveM3u(link: String) = viewModelScope.launch {
        dao.insert(
            PlaylistEntity(type = PlaylistType.M3U, url = link.trim())
        )
    }
}