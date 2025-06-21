package com.yoopi.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type:       PlaylistType,
    val url:        String,          // full M3U link OR Xtream base URL
    val username:   String? = null,  // Xtream only
    val password:   String? = null   // Xtream only
)
