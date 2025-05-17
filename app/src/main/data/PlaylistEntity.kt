package com.yoopi.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: PlaylistType,
    val url: String,
    val username: String? = null,
    val password: String? = null,
    val addedOn: Long = System.currentTimeMillis()
)

enum class PlaylistType { XTREAM, M3U }