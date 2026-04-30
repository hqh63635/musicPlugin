package com.musicfree.android.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class SearchType(val code: Int, val title: String) {
    SONG(0, "歌曲"),
    ARTIST(1, "艺人"),
    ALBUM(2, "专辑"),
    SHEET(3, "歌单"),
}

@Serializable
enum class AudioQuality(val title: String, val remoteValue: String) {
    LOW("128K", "128k"),
    STANDARD("标准", "320k"),
    HIGH("高品", "320k"),
    SUPER("无损", "lossless"),
}

@Serializable
enum class AudioSource(val title: String) {
    HAITANG("海棠"),
    LUOXUE("落雪"),
}

@Serializable
data class Song(
    val id: String,
    val songmid: String,
    val title: String,
    val artist: String,
    val artwork: String? = null,
    val album: String? = null,
    val albumId: String? = null,
    val albumMid: String? = null,
    val duration: Int? = null,
    val isFavorite: Boolean = false,
) {
    val identity: String
        get() = songmid.ifBlank { id }
}

@Serializable
data class CustomPlaylist(
    val id: String,
    val name: String,
    val createTime: Long,
    val songs: List<Song> = emptyList(),
    val artwork: String? = null,
    val description: String? = null,
) {
    val trackCount: Int
        get() = songs.size
}

@Serializable
data class Artist(
    val id: String,
    val singerMid: String,
    val name: String,
    val artwork: String? = null,
    val workCount: Int? = null,
)

@Serializable
data class Album(
    val id: String,
    val albumMid: String,
    val title: String,
    val artist: String? = null,
    val artwork: String? = null,
    val date: String? = null,
    val description: String? = null,
    val workCount: Int? = null,
)

@Serializable
data class PlaylistSheet(
    val id: String,
    val title: String,
    val artwork: String? = null,
    val description: String? = null,
    val artist: String? = null,
    val playCount: Long? = null,
    val createTime: String? = null,
    val worksNum: Int? = null,
    val period: String? = null,
)

@Serializable
data class PlaylistGroup(
    val title: String,
    val items: List<PlaylistSheet>,
)

@Serializable
data class SheetTag(
    val id: String,
    val title: String,
)

@Serializable
data class SheetTagGroup(
    val title: String,
    val items: List<SheetTag>,
)

@Serializable
data class RecommendTags(
    val pinned: List<SheetTag>,
    val groups: List<SheetTagGroup>,
)

@Serializable
data class UserSettings(
    val quality: AudioQuality = AudioQuality.SUPER,
    val source: AudioSource = AudioSource.HAITANG,
)

@Serializable
data class PlaybackSource(
    val url: String,
    val userAgent: String? = null,
    val headers: Map<String, String> = emptyMap(),
)

data class PlaylistDetail(
    val sheet: PlaylistSheet,
    val songs: List<Song>,
)

data class HomeFeed(
    val topGroups: List<PlaylistGroup>,
    val featuredChart: PlaylistSheet?,
    val featuredSongs: List<Song>,
    val recommendedTags: RecommendTags,
    val selectedTag: SheetTag?,
    val recommendedSheets: List<PlaylistSheet>,
)

data class SearchPayload(
    val songs: List<Song> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val albums: List<Album> = emptyList(),
    val sheets: List<PlaylistSheet> = emptyList(),
    val isEnd: Boolean = true,
)

@Serializable
data class LyricPayload(
    val rawLrc: String = "",
    val translation: String = "",
)

data class LyricLine(
    val timeMs: Long,
    val text: String,
)
