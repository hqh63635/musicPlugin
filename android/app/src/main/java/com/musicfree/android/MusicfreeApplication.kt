package com.musicfree.android

import android.app.Application

class MusicfreeApplication : Application() {
    lateinit var container: MusicfreeContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = MusicfreeContainer(this)
    }
}
