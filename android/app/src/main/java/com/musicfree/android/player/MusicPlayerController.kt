package com.musicfree.android.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.musicfree.android.data.local.UserLibraryStore
import com.musicfree.android.data.model.AudioQuality
import com.musicfree.android.data.model.AudioSource
import com.musicfree.android.data.model.LyricLine
import com.musicfree.android.data.model.Song
import com.musicfree.android.data.repository.MusicRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlayerUiState(
    val queue: List<Song> = emptyList(),
    val currentIndex: Int = -1,
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val durationMs: Long = 0L,
    val positionMs: Long = 0L,
    val progress: Float = 0f,
    val lyrics: List<LyricLine> = emptyList(),
    val currentLyricIndex: Int = -1,
    val quality: AudioQuality = AudioQuality.SUPER,
    val source: AudioSource = AudioSource.HAITANG,
    val loading: Boolean = false,
)

class MusicPlayerController(
    context: Context,
    private val repository: MusicRepository,
    private val libraryStore: UserLibraryStore,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val player = ExoPlayer.Builder(context).build()
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        player.addListener(
            object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _uiState.update { it.copy(isPlaying = isPlaying) }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        playNext()
                    }
                    publishProgress()
                }
            },
        )

        scope.launch {
            var initial = true
            libraryStore.settings.collect { settings ->
                val previous = _uiState.value
                _uiState.update {
                    it.copy(
                        quality = settings.quality,
                        source = settings.source,
                    )
                }
                if (!initial && previous.currentSong != null &&
                    (previous.quality != settings.quality || previous.source != settings.source)
                ) {
                    refreshSource()
                }
                initial = false
            }
        }

        scope.launch {
            while (isActive) {
                publishProgress()
                delay(350L)
            }
        }
    }

    fun setQueue(
        songs: List<Song>,
        startIndex: Int = 0,
    ) {
        if (songs.isEmpty()) {
            return
        }
        scope.launch {
            _uiState.update { it.copy(queue = songs) }
            playAtIndex(startIndex.coerceIn(0, songs.lastIndex))
        }
    }

    fun playSong(
        song: Song,
        queue: List<Song>? = null,
    ) {
        scope.launch {
            val workingQueue = when {
                !queue.isNullOrEmpty() -> queue
                _uiState.value.queue.any { it.identity == song.identity } -> _uiState.value.queue
                else -> _uiState.value.queue + song
            }
            val targetIndex = workingQueue.indexOfFirst { it.identity == song.identity }.coerceAtLeast(0)
            _uiState.update { it.copy(queue = workingQueue) }
            playAtIndex(targetIndex)
        }
    }

    fun togglePlay() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
        publishProgress()
    }

    fun playNext() {
        val state = _uiState.value
        if (state.queue.isEmpty()) {
            return
        }
        val nextIndex = (state.currentIndex + 1).mod(state.queue.size)
        scope.launch {
            playAtIndex(nextIndex)
        }
    }

    fun playPrevious() {
        val state = _uiState.value
        if (state.queue.isEmpty()) {
            return
        }
        val previousIndex = if (state.currentIndex <= 0) {
            state.queue.lastIndex
        } else {
            state.currentIndex - 1
        }
        scope.launch {
            playAtIndex(previousIndex)
        }
    }

    fun seekTo(progress: Float) {
        val clamped = progress.coerceIn(0f, 1f)
        val duration = player.duration.takeIf { it > 0 } ?: return
        player.seekTo((duration * clamped).toLong())
        publishProgress()
    }

    fun removeFromQueue(index: Int) {
        val state = _uiState.value
        if (index !in state.queue.indices) {
            return
        }
        val mutable = state.queue.toMutableList()
        mutable.removeAt(index)
        if (mutable.isEmpty()) {
            clearQueue()
            return
        }
        val newIndex = when {
            index < state.currentIndex -> state.currentIndex - 1
            index == state.currentIndex -> state.currentIndex.coerceAtMost(mutable.lastIndex)
            else -> state.currentIndex
        }
        _uiState.update { it.copy(queue = mutable, currentIndex = newIndex) }
        if (index == state.currentIndex) {
            scope.launch {
                playAtIndex(newIndex)
            }
        }
    }

    fun clearQueue() {
        player.stop()
        _uiState.value = PlayerUiState(
            quality = _uiState.value.quality,
            source = _uiState.value.source,
        )
    }

    private suspend fun refreshSource() {
        val state = _uiState.value
        val song = state.currentSong ?: return
        val source = repository.fetchPlaybackSource(song) ?: return
        val shouldResume = player.isPlaying
        val position = player.currentPosition.coerceAtLeast(0L)
        player.setMediaItem(MediaItem.fromUri(source.url))
        player.prepare()
        player.seekTo(position)
        player.playWhenReady = shouldResume
        publishProgress()
    }

    private suspend fun playAtIndex(index: Int) {
        val state = _uiState.value
        if (index !in state.queue.indices) {
            return
        }
        val song = state.queue[index]
        _uiState.update {
            it.copy(
                currentIndex = index,
                currentSong = song,
                loading = true,
            )
        }

        val sourceDeferred = scope.async(Dispatchers.IO) {
            repository.fetchPlaybackSource(song)
        }
        val lyricDeferred = scope.async(Dispatchers.IO) {
            repository.fetchLyric(song)
        }
        val source = sourceDeferred.await()
        val lyrics = lyricDeferred.await()

        if (source == null) {
            _uiState.update { it.copy(loading = false) }
            return
        }

        player.setMediaItem(MediaItem.fromUri(source.url))
        player.prepare()
        player.playWhenReady = true

        val parsedLyrics = LyricParser.parse(lyrics.rawLrc)
        _uiState.update {
            it.copy(
                currentSong = song,
                currentIndex = index,
                isPlaying = true,
                lyrics = parsedLyrics,
                currentLyricIndex = if (parsedLyrics.isEmpty()) -1 else 0,
                loading = false,
            )
        }
    }

    private fun publishProgress() {
        val duration = player.duration.takeIf { it > 0 } ?: 0L
        val position = player.currentPosition.coerceAtLeast(0L)
        val lyrics = _uiState.value.lyrics
        val currentLyricIndex = LyricParser.findCurrentIndex(lyrics, position)
        _uiState.update {
            it.copy(
                durationMs = duration,
                positionMs = position,
                progress = if (duration > 0) position.toFloat() / duration.toFloat() else 0f,
                currentLyricIndex = currentLyricIndex,
            )
        }
    }
}
