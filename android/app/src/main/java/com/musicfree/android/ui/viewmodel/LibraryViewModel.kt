package com.musicfree.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicfree.android.data.model.AudioQuality
import com.musicfree.android.data.model.AudioSource
import com.musicfree.android.data.model.Song
import com.musicfree.android.data.model.UserSettings
import com.musicfree.android.data.repository.MusicRepository
import com.musicfree.android.player.MusicPlayerController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LibraryUiState(
    val favorites: List<Song> = emptyList(),
    val history: List<String> = emptyList(),
    val queue: List<Song> = emptyList(),
    val currentSongId: String? = null,
    val settings: UserSettings = UserSettings(),
)

class LibraryViewModel(
    private val repository: MusicRepository,
    private val playerController: MusicPlayerController,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.favorites.collect { favorites ->
                _uiState.update { it.copy(favorites = favorites) }
            }
        }
        viewModelScope.launch {
            repository.searchHistory.collect { history ->
                _uiState.update { it.copy(history = history) }
            }
        }
        viewModelScope.launch {
            repository.settings.collect { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
        }
        viewModelScope.launch {
            playerController.uiState.collect { player ->
                _uiState.update {
                    it.copy(
                        queue = player.queue,
                        currentSongId = player.currentSong?.identity,
                    )
                }
            }
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            repository.toggleFavorite(song)
        }
    }

    fun removeFromQueue(index: Int) {
        playerController.removeFromQueue(index)
    }

    fun clearQueue() {
        playerController.clearQueue()
    }

    fun setQuality(quality: AudioQuality) {
        viewModelScope.launch {
            repository.setQuality(quality)
        }
    }

    fun setSource(source: AudioSource) {
        viewModelScope.launch {
            repository.setSource(source)
        }
    }
}
