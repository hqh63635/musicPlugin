package com.musicfree.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicfree.android.data.model.Album
import com.musicfree.android.data.model.Artist
import com.musicfree.android.data.model.PlaylistSheet
import com.musicfree.android.data.model.SearchType
import com.musicfree.android.data.model.Song
import com.musicfree.android.data.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val currentType: SearchType = SearchType.SONG,
    val loading: Boolean = false,
    val page: Int = 1,
    val isEnd: Boolean = true,
    val songs: List<Song> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val albums: List<Album> = emptyList(),
    val sheets: List<PlaylistSheet> = emptyList(),
    val history: List<String> = emptyList(),
    val error: String? = null,
) {
    val hasResults: Boolean
        get() = songs.isNotEmpty() || artists.isNotEmpty() || albums.isNotEmpty() || sheets.isNotEmpty()
}

class SearchViewModel(
    private val repository: MusicRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.searchHistory.collect { history ->
                _uiState.update { it.copy(history = history) }
            }
        }
    }

    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun submitQuery(query: String = uiState.value.query) {
        val normalized = query.trim()
        if (normalized.isBlank()) {
            return
        }
        _uiState.update {
            it.copy(
                query = normalized,
                loading = true,
                page = 1,
                error = null,
                songs = emptyList(),
                artists = emptyList(),
                albums = emptyList(),
                sheets = emptyList(),
            )
        }
        loadPage(reset = true)
        viewModelScope.launch {
            repository.saveSearch(normalized)
        }
    }

    fun switchType(type: SearchType) {
        if (type == uiState.value.currentType) {
            return
        }
        _uiState.update {
            it.copy(
                currentType = type,
                page = 1,
                songs = emptyList(),
                artists = emptyList(),
                albums = emptyList(),
                sheets = emptyList(),
                isEnd = true,
                error = null,
            )
        }
        if (uiState.value.query.isNotBlank()) {
            loadPage(reset = true)
        }
    }

    fun loadMore() {
        val state = uiState.value
        if (state.loading || state.isEnd || state.query.isBlank()) {
            return
        }
        _uiState.update { it.copy(loading = true, page = it.page + 1) }
        loadPage(reset = false)
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearSearchHistory()
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            val isFavorite = repository.toggleFavorite(song)
            _uiState.update { state ->
                state.copy(
                    songs = state.songs.map { item ->
                        if (item.identity == song.identity) {
                            item.copy(isFavorite = isFavorite)
                        } else {
                            item
                        }
                    },
                )
            }
        }
    }

    private fun loadPage(reset: Boolean) {
        viewModelScope.launch {
            val page = uiState.value.page
            val type = uiState.value.currentType
            val query = uiState.value.query
            runCatching {
                repository.search(query, page, type)
            }.onSuccess { payload ->
                _uiState.update { state ->
                    state.copy(
                        loading = false,
                        isEnd = payload.isEnd,
                        songs = if (reset) payload.songs else state.songs + payload.songs,
                        artists = if (reset) payload.artists else state.artists + payload.artists,
                        albums = if (reset) payload.albums else state.albums + payload.albums,
                        sheets = if (reset) payload.sheets else state.sheets + payload.sheets,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        error = throwable.message ?: "搜索失败",
                    )
                }
            }
        }
    }
}
