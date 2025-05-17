package com.yoopi.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PlaylistEntity::class], version = 1)
abstract class YoopiDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao

    companion object {
        @Volatile private var INSTANCE: YoopiDatabase? = null
        fun get(context: Context): YoopiDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    YoopiDatabase::class.java,
                    "yoopi.db"
                ).build().also { INSTANCE = it }
            }
    }
}