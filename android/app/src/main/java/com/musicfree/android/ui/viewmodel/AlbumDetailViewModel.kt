package com.musicfree.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicfree.android.data.model.SearchType
import com.musicfree.android.data.model.Song
import com.musicfree.android.data.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AlbumDetailUiState(
    val title: String = "",
    val artist: String? = null,
    val artwork: String? = null,
    val loading: Boolean = true,
    val songs: List<Song> = emptyList(),
    val error: String? = null,
)

class AlbumDetailViewModel(
    private val repository: MusicRepository,
    private val albumTitle: String,
    private val albumArtist: String?,
    private val artwork: String?,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        AlbumDetailUiState(
            title = albumTitle,
            artist = albumArtist,
            artwork = artwork,
        ),
    )
    val uiState: StateFlow<AlbumDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            val isFavorite = repository.toggleFavorite(song)
            _uiState.update { state ->
                state.copy(
                    songs = state.songs.map {
                        if (it.identity == song.identity) it.copy(isFavorite = isFavorite) else it
                    },
                )
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            runCatching {
                repository.search(albumTitle, 1, SearchType.SONG)
            }.onSuccess { payload ->
                val filtered = payload.songs.filter { song ->
                    val titleMatch = song.album?.contains(albumTitle, ignoreCase = true) == true
                    val artistMatch = albumArtist.isNullOrBlank() || song.artist.contains(albumArtist, ignoreCase = true)
                    titleMatch && artistMatch
                }.ifEmpty { payload.songs }
                _uiState.update {
                    it.copy(
                        loading = false,
                        songs = filtered,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        error = throwable.message ?: "加载专辑歌曲失败",
                    )
                }
            }
        }
    }
}
