package com.yoopi

import android.content.Context
import com.yoopi.data.AppDatabase
import com.yoopi.data.PlaylistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        AppDatabase.getInstance(ctx)          // ✅ getInstance now exists

    @Singleton
    @Provides
    fun providePlaylistDao(db: AppDatabase): PlaylistDao =
        db.playlistDao()
}
