package com.musicfree.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicfree.android.data.model.PlaylistDetail
import com.musicfree.android.data.model.PlaylistSheet
import com.musicfree.android.data.model.Song
import com.musicfree.android.data.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class DetailKind {
    CHART,
    SHEET,
    LOCAL,
}

data class PlaylistDetailUiState(
    val loading: Boolean = true,
    val detail: PlaylistDetail? = null,
    val error: String? = null,
)

class PlaylistDetailViewModel(
    private val repository: MusicRepository,
    private val kind: DetailKind,
    private val sheet: PlaylistSheet,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PlaylistDetailUiState())
    val uiState: StateFlow<PlaylistDetailUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            runCatching {
                when (kind) {
                    DetailKind.CHART -> repository.fetchTopListDetail(sheet)
                    DetailKind.SHEET -> repository.fetchMusicSheetDetail(sheet)
                    DetailKind.LOCAL -> repository.fetchCustomPlaylistDetail(sheet.id)
                        ?: error("歌单不存在")
                }
            }.onSuccess { detail ->
                _uiState.value = PlaylistDetailUiState(
                    loading = false,
                    detail = detail,
                )
            }.onFailure { throwable ->
                _uiState.value = PlaylistDetailUiState(
                    loading = false,
                    error = throwable.message ?: "加载失败",
                )
            }
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            val isFavorite = repository.toggleFavorite(song)
            _uiState.update { state ->
                state.copy(
                    detail = state.detail?.copy(
                        songs = state.detail.songs.map { item ->
                            if (item.identity == song.identity) {
                                item.copy(isFavorite = isFavorite)
                            } else {
                                item
                            }
                        },
                    ),
                )
            }
        }
    }
}
