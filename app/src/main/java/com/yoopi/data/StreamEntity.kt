package com.yoopi.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "streams")
data class StreamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playlistId: Long,      // FK → playlists table  (we’ll pass it in)
    val name: String,
    val url:  String,
    val group: String = ""
)
