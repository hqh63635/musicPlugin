package com.musicfree.android.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.musicfree.android.data.model.AudioQuality
import com.musicfree.android.data.model.AudioSource
import com.musicfree.android.data.model.Song
import com.musicfree.android.data.model.UserSettings
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
    companion object {
        private const val MAX_HISTORY = 8

        private val FAVORITES_KEY = stringPreferencesKey("favorites")
        private val SEARCH_HISTORY_KEY = stringPreferencesKey("search_history")
        private val QUALITY_KEY = stringPreferencesKey("quality")
        private val SOURCE_KEY = stringPreferencesKey("source")
    }
}
