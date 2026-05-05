package com.musicfree.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicfree.android.data.model.Song
import com.musicfree.android.data.model.SearchType
import com.musicfree.android.data.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ArtistDetailUiState(
    val name: String = "",
    val artwork: String? = null,
    val loading: Boolean = true,
    val songs: List<Song> = emptyList(),
    val error: String? = null,
)

class ArtistDetailViewModel(
    private val repository: MusicRepository,
    private val artistName: String,
    private val artwork: String?,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ArtistDetailUiState(
            name = artistName,
            artwork = artwork,
        ),
    )
    val uiState: StateFlow<ArtistDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun retry() {
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
                repository.search(artistName, 1, SearchType.SONG)
            }.onSuccess { payload ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        songs = payload.songs,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        error = throwable.message ?: "加载艺人歌曲失败",
                    )
                }
            }
        }
    }
}
