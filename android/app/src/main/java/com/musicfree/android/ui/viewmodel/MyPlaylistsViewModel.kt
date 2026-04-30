package com.musicfree.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicfree.android.data.model.CustomPlaylist
import com.musicfree.android.data.model.PlaylistDetail
import com.musicfree.android.data.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyPlaylistsUiState(
    val playlists: List<CustomPlaylist> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
)

class MyPlaylistsViewModel(
    private val repository: MusicRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MyPlaylistsUiState())
    val uiState: StateFlow<MyPlaylistsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.customPlaylists.collect { playlists ->
                _uiState.update {
                    it.copy(playlists = playlists)
                }
            }
        }
    }

    suspend fun importMusicSheet(urlOrId: String): PlaylistDetail {
        _uiState.update { it.copy(loading = true, error = null) }
        return runCatching {
            repository.importMusicSheet(urlOrId)
        }.onFailure { throwable ->
            _uiState.update {
                it.copy(
                    loading = false,
                    error = throwable.message ?: "导入失败",
                )
            }
        }.getOrThrow().also {
            _uiState.update { state ->
                state.copy(loading = false, error = null)
            }
        }
    }

    suspend fun createPlaylist(
        name: String,
        importedDetail: PlaylistDetail? = null,
    ): CustomPlaylist {
        return repository.createCustomPlaylist(
            name = name,
            songs = importedDetail?.songs.orEmpty(),
            artwork = importedDetail?.sheet?.artwork,
            description = importedDetail?.sheet?.description,
        )
    }

    suspend fun addImportedToPlaylist(
        playlistId: String,
        importedDetail: PlaylistDetail,
    ) {
        repository.addSongsToCustomPlaylist(playlistId, importedDetail.songs)
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            repository.deleteCustomPlaylist(playlistId)
        }
    }
}
