package com.musicfree.android

import android.content.Context
import com.musicfree.android.data.local.UserLibraryStore
import com.musicfree.android.data.remote.MusicRemoteDataSource
import com.musicfree.android.data.repository.MusicRepository
import com.musicfree.android.player.MusicPlayerController
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class MusicfreeContainer(context: Context) {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val libraryStore = UserLibraryStore(context)
    val remoteDataSource = MusicRemoteDataSource(httpClient)
    val repository = MusicRepository(remoteDataSource, libraryStore)
    val playerController = MusicPlayerController(context, repository, libraryStore)
}
