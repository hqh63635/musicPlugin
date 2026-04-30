package com.musicfree.android.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.musicfree.android.data.model.AudioQuality
import com.musicfree.android.data.model.AudioSource
import com.musicfree.android.data.model.CustomPlaylist
import com.musicfree.android.data.model.Song
import com.musicfree.android.data.model.UserSettings
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "musicfree_library")

class UserLibraryStore(
    private val context: Context,
) {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    val favorites: Flow<List<Song>> = context.dataStore.data.map { prefs ->
        decodeSongs(prefs[FAVORITES_KEY])
    }

    val searchHistory: Flow<List<String>> = context.dataStore.data.map { prefs ->
        decodeStrings(prefs[SEARCH_HISTORY_KEY])
    }

    val customPlaylists: Flow<List<CustomPlaylist>> = context.dataStore.data.map { prefs ->
        decodeCustomPlaylists(prefs[CUSTOM_PLAYLISTS_KEY])
    }

    val settings: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            quality = prefs[QUALITY_KEY]
                ?.let { runCatching { AudioQuality.valueOf(it) }.getOrNull() }
                ?: AudioQuality.SUPER,
            source = prefs[SOURCE_KEY]
                ?.let { runCatching { AudioSource.valueOf(it) }.getOrNull() }
                ?: AudioSource.HAITANG,
        )
    }

    suspend fun toggleFavorite(song: Song): Boolean {
        var newState = false
        context.dataStore.edit { prefs ->
            val current = decodeSongs(prefs[FAVORITES_KEY]).toMutableList()
            val index = current.indexOfFirst { it.identity == song.identity }
            if (index >= 0) {
                current.removeAt(index)
                newState = false
            } else {
                current.add(0, song.copy(isFavorite = true))
                newState = true
            }
            prefs[FAVORITES_KEY] = encodeSongs(current)
        }
        return newState
    }

    suspend fun favoriteIds(): Set<String> {
        return favorites.first().map { it.identity }.toSet()
    }

    suspend fun saveSearch(keyword: String) {
        val normalized = keyword.trim()
        if (normalized.isBlank()) {
            return
        }
        context.dataStore.edit { prefs ->
            val existing = decodeStrings(prefs[SEARCH_HISTORY_KEY])
                .filterNot { it.equals(normalized, ignoreCase = true) }
                .toMutableList()
            existing.add(0, normalized)
            prefs[SEARCH_HISTORY_KEY] = encodeStrings(existing.take(MAX_HISTORY))
        }
    }

    suspend fun clearHistory() {
        context.dataStore.edit { prefs ->
            prefs[SEARCH_HISTORY_KEY] = encodeStrings(emptyList())
        }
    }

    suspend fun setQuality(quality: AudioQuality) {
        context.dataStore.edit { prefs ->
            prefs[QUALITY_KEY] = quality.name
        }
    }

    suspend fun setSource(source: AudioSource) {
        context.dataStore.edit { prefs ->
            prefs[SOURCE_KEY] = source.name
        }
    }

    suspend fun createCustomPlaylist(
        name: String,
        songs: List<Song> = emptyList(),
        artwork: String? = songs.firstOrNull()?.artwork,
        description: String? = null,
    ): CustomPlaylist {
        val playlist = CustomPlaylist(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            createTime = System.currentTimeMillis(),
            songs = songs.distinctBy { it.identity },
            artwork = artwork,
            description = description,
        )
        context.dataStore.edit { prefs ->
            val updated = decodeCustomPlaylists(prefs[CUSTOM_PLAYLISTS_KEY]).toMutableList()
            updated.add(0, playlist)
            prefs[CUSTOM_PLAYLISTS_KEY] = encodeCustomPlaylists(updated)
        }
        return playlist
    }

    suspend fun addSongsToCustomPlaylist(
        playlistId: String,
        songs: List<Song>,
    ) {
        context.dataStore.edit { prefs ->
            val updated = decodeCustomPlaylists(prefs[CUSTOM_PLAYLISTS_KEY]).map { playlist ->
                if (playlist.id == playlistId) {
                    val merged = (playlist.songs + songs).distinctBy { it.identity }
                    playlist.copy(
                        songs = merged,
                        artwork = playlist.artwork ?: merged.firstOrNull()?.artwork,
                    )
                } else {
                    playlist
                }
            }
            prefs[CUSTOM_PLAYLISTS_KEY] = encodeCustomPlaylists(updated)
        }
    }

    suspend fun deleteCustomPlaylist(playlistId: String) {
        context.dataStore.edit { prefs ->
            val updated = decodeCustomPlaylists(prefs[CUSTOM_PLAYLISTS_KEY])
                .filterNot { it.id == playlistId }
            prefs[CUSTOM_PLAYLISTS_KEY] = encodeCustomPlaylists(updated)
        }
    }

    suspend fun getCustomPlaylistById(playlistId: String): CustomPlaylist? {
        return customPlaylists.first().firstOrNull { it.id == playlistId }
    }

    suspend fun clearCustomPlaylistSongs(playlistId: String) {
        context.dataStore.edit { prefs ->
            val updated = decodeCustomPlaylists(prefs[CUSTOM_PLAYLISTS_KEY]).map { playlist ->
                if (playlist.id == playlistId) {
                    playlist.copy(songs = emptyList())
                } else {
                    playlist
                }
            }
            prefs[CUSTOM_PLAYLISTS_KEY] = encodeCustomPlaylists(updated)
        }
    }

    private fun decodeSongs(payload: String?): List<Song> {
        if (payload.isNullOrBlank()) {
            return emptyList()
        }
        return runCatching {
            json.decodeFromString(ListSerializer(Song.serializer()), payload)
        }.getOrDefault(emptyList())
    }

    private fun encodeSongs(items: List<Song>): String {
        return json.encodeToString(ListSerializer(Song.serializer()), items)
    }

    private fun decodeStrings(payload: String?): List<String> {
        if (payload.isNullOrBlank()) {
            return emptyList()
        }
        return runCatching {
            json.decodeFromString(ListSerializer(String.serializer()), payload)
        }.getOrDefault(emptyList())
    }

    private fun encodeStrings(items: List<String>): String {
        return json.encodeToString(ListSerializer(String.serializer()), items)
    }

    private fun decodeCustomPlaylists(payload: String?): List<CustomPlaylist> {
        if (payload.isNullOrBlank()) {
            return emptyList()
        }
        return runCatching {
            json.decodeFromString(ListSerializer(CustomPlaylist.serializer()), payload)
        }.getOrDefault(emptyList())
    }

    private fun encodeCustomPlaylists(items: List<CustomPlaylist>): String {
        return json.encodeToString(ListSerializer(CustomPlaylist.serializer()), items)
    }

    companion object {
        private const val MAX_HISTORY = 8

        private val FAVORITES_KEY = stringPreferencesKey("favorites")
        private val SEARCH_HISTORY_KEY = stringPreferencesKey("search_history")
        private val CUSTOM_PLAYLISTS_KEY = stringPreferencesKey("custom_playlists")
        private val QUALITY_KEY = stringPreferencesKey("quality")
        private val SOURCE_KEY = stringPreferencesKey("source")
    }
}
