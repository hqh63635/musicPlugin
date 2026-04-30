package com.musicfree.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicfree.android.data.model.HomeFeed
import com.musicfree.android.data.model.PlaylistSheet
import com.musicfree.android.data.model.SheetTag
import com.musicfree.android.data.model.Song
import com.musicfree.android.data.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = true,
    val feed: HomeFeed? = null,
    val error: String? = null,
)

class HomeViewModel(
    private val repository: MusicRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh(selectedTagId: String? = uiState.value.feed?.selectedTag?.id) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            runCatching {
                repository.loadHomeFeed(selectedTagId)
            }.onSuccess { feed ->
                _uiState.value = HomeUiState(
                    loading = false,
                    feed = feed,
                )
            }.onFailure { throwable ->
                _uiState.value = HomeUiState(
                    loading = false,
                    error = throwable.message ?: "加载失败",
                )
            }
        }
    }

    fun selectTag(tag: SheetTag) {
        refresh(tag.id)
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            val isFavorite = repository.toggleFavorite(song)
            _uiState.update { state ->
                state.copy(
                    feed = state.feed?.copy(
                        featuredSongs = state.feed.featuredSongs.map { item ->
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

    fun playSheetUpdated(sheet: PlaylistSheet) {
        _uiState.update { state ->
            state.copy(
                feed = state.feed?.copy(
                    featuredChart = if (state.feed.featuredChart?.id == sheet.id) sheet else state.feed.featuredChart,
                ),
            )
        }
    }
}
