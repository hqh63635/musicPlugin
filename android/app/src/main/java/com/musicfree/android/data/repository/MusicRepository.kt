package com.musicfree.android.data.repository

import com.musicfree.android.data.local.UserLibraryStore
import com.musicfree.android.data.model.AudioQuality
import com.musicfree.android.data.model.AudioSource
import com.musicfree.android.data.model.CustomPlaylist
import com.musicfree.android.data.model.HomeFeed
import com.musicfree.android.data.model.LyricPayload
import com.musicfree.android.data.model.PlaybackSource
import com.musicfree.android.data.model.PlaylistDetail
import com.musicfree.android.data.model.PlaylistSheet
import com.musicfree.android.data.model.SearchPayload
import com.musicfree.android.data.model.SearchType
import com.musicfree.android.data.model.SheetTag
import com.musicfree.android.data.model.Song
import com.musicfree.android.data.remote.MusicRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class MusicRepository(
    private val remote: MusicRemoteDataSource,
    private val libraryStore: UserLibraryStore,
) {
    val favorites: Flow<List<Song>> = libraryStore.favorites
    val searchHistory: Flow<List<String>> = libraryStore.searchHistory
    val customPlaylists: Flow<List<CustomPlaylist>> = libraryStore.customPlaylists
    val settings = libraryStore.settings

    suspend fun loadHomeFeed(selectedTagId: String? = null): HomeFeed {
        val topGroups = remote.fetchTopLists()
        val featuredChart = topGroups.firstOrNull()?.items?.firstOrNull()
        val featuredSongs = featuredChart?.let {
            remote.fetchTopListDetail(it).songs.withFavorites()
        }.orEmpty()
        val tags = remote.fetchRecommendSheetTags()
        val selected = resolveTag(tags.pinned + tags.groups.flatMap { it.items }, selectedTagId)
        val recommendedSheets = remote.fetchRecommendSheetsByTag(selected?.id, 1).first
        return HomeFeed(
            topGroups = topGroups,
            featuredChart = featuredChart,
            featuredSongs = featuredSongs,
            recommendedTags = tags,
            selectedTag = selected,
            recommendedSheets = recommendedSheets,
        )
    }

    suspend fun search(
        keyword: String,
        page: Int,
        type: SearchType,
    ): SearchPayload {
        val result = remote.search(keyword, page, type)
        return result.copy(songs = result.songs.withFavorites())
    }

    suspend fun fetchTopListDetail(sheet: PlaylistSheet): PlaylistDetail {
        val detail = remote.fetchTopListDetail(sheet)
        return detail.copy(songs = detail.songs.withFavorites())
    }

    suspend fun fetchMusicSheetDetail(sheet: PlaylistSheet): PlaylistDetail {
        val detail = remote.fetchMusicSheetDetail(sheet)
        return detail.copy(songs = detail.songs.withFavorites())
    }

    suspend fun toggleFavorite(song: Song): Boolean {
        return libraryStore.toggleFavorite(song)
    }

    suspend fun saveSearch(keyword: String) {
        libraryStore.saveSearch(keyword)
    }

    suspend fun clearSearchHistory() {
        libraryStore.clearHistory()
    }

    suspend fun setQuality(quality: AudioQuality) {
        libraryStore.setQuality(quality)
    }

    suspend fun setSource(source: AudioSource) {
        libraryStore.setSource(source)
    }

    suspend fun fetchPlaybackSource(song: Song): PlaybackSource? {
        val settings = settings.first()
        return remote.fetchPlaybackSource(song, settings.quality, settings.source)
    }

    suspend fun fetchLyric(song: Song): LyricPayload {
        return remote.fetchLyric(song.songmid)
    }

    suspend fun favoriteIds(): Set<String> {
        return libraryStore.favoriteIds()
    }

    suspend fun importMusicSheet(urlOrId: String): PlaylistDetail {
        val detail = remote.importMusicSheet(urlOrId)
        return detail.copy(songs = detail.songs.withFavorites())
    }

    suspend fun createCustomPlaylist(
        name: String,
        songs: List<Song> = emptyList(),
        artwork: String? = songs.firstOrNull()?.artwork,
        description: String? = null,
    ): CustomPlaylist {
        return libraryStore.createCustomPlaylist(
            name = name,
            songs = songs,
            artwork = artwork,
            description = description,
        )
    }

    suspend fun addSongsToCustomPlaylist(
        playlistId: String,
        songs: List<Song>,
    ) {
        libraryStore.addSongsToCustomPlaylist(playlistId, songs)
    }

    suspend fun deleteCustomPlaylist(playlistId: String) {
        libraryStore.deleteCustomPlaylist(playlistId)
    }

    suspend fun fetchCustomPlaylistDetail(playlistId: String): PlaylistDetail? {
        val playlist = libraryStore.getCustomPlaylistById(playlistId) ?: return null
        return PlaylistDetail(
            sheet = playlist.toPlaylistSheet(),
            songs = playlist.songs.withFavorites(),
        )
    }

    suspend fun clearCustomPlaylistSongs(playlistId: String) {
        libraryStore.clearCustomPlaylistSongs(playlistId)
    }

    private suspend fun List<Song>.withFavorites(): List<Song> {
        val ids = favoriteIds()
        return map { song ->
            song.copy(isFavorite = ids.contains(song.identity))
        }
    }

    private fun resolveTag(
        tags: List<SheetTag>,
        selectedTagId: String?,
    ): SheetTag? {
        return if (selectedTagId.isNullOrBlank()) {
            tags.firstOrNull()
        } else {
            tags.find { it.id == selectedTagId } ?: tags.firstOrNull()
        }
    }

    private fun CustomPlaylist.toPlaylistSheet(): PlaylistSheet {
        return PlaylistSheet(
            id = id,
            title = name,
            artwork = artwork ?: songs.firstOrNull()?.artwork,
            description = description,
            artist = "我的歌单",
            createTime = createTime.toString(),
            worksNum = trackCount,
        )
    }
}
