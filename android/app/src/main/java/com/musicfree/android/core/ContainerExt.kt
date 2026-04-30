package com.musicfree.android.core

import android.content.Context
import com.musicfree.android.MusicfreeApplication
import com.musicfree.android.MusicfreeContainer

val Context.musicfreeContainer: MusicfreeContainer
    get() = (applicationContext as MusicfreeApplication).container
