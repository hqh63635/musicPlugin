package com.musicfree.android.data.remote

import android.util.Base64
import com.musicfree.android.core.normalizeArtwork
import com.musicfree.android.data.model.Album
import com.musicfree.android.data.model.Artist
import com.musicfree.android.data.model.AudioQuality
import com.musicfree.android.data.model.AudioSource
import com.musicfree.android.data.model.LyricPayload
import com.musicfree.android.data.model.PlaybackSource
import com.musicfree.android.data.model.PlaylistDetail
import com.musicfree.android.data.model.PlaylistGroup
import com.musicfree.android.data.model.PlaylistSheet
import com.musicfree.android.data.model.RecommendTags
import com.musicfree.android.data.model.SearchPayload
import com.musicfree.android.data.model.SearchType
import com.musicfree.android.data.model.SheetTag
import com.musicfree.android.data.model.SheetTagGroup
import com.musicfree.android.data.model.Song
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MusicRemoteDataSource(
    private val client: OkHttpClient,
) {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun search(
        keyword: String,
        page: Int,
        type: SearchType,
    ): SearchPayload {
        val payload = buildJsonObject {
            putJsonObject("req_1") {
                put("method", "DoSearchForQQMusicDesktop")
                put("module", "music.search.SearchCgiService")
                putJsonObject("param") {
                    put("num_per_page", PAGE_SIZE)
                    put("page_num", page)
                    put("query", keyword)
                    put("search_type", type.code)
                }
            }
        }

        val body = requestText(
            url = "https://u.y.qq.com/cgi-bin/musicu.fcg",
            method = "POST",
            body = json.encodeToString(JsonObject.serializer(), payload),
            headers = defaultHeaders,
        )

        val root = parseJsonObject(body)
        val req = root.obj("req_1")
            ?.obj("data")
            ?.obj("body")
            ?: return SearchPayload()

        return when (type) {
            SearchType.SONG -> {
                val items = req.obj("song")
                    ?.array("list")
                    .orEmptyArray()
                    .map(::mapSong)
                SearchPayload(songs = items, isEnd = items.size < PAGE_SIZE)
            }
            SearchType.ARTIST -> {
                val items = req.obj("singer")
                    ?.array("list")
                    .orEmptyArray()
                    .map(::mapArtist)
                SearchPayload(artists = items, isEnd = items.size < PAGE_SIZE)
            }
            SearchType.ALBUM -> {
                val items = req.obj("album")
                    ?.array("list")
                    .orEmptyArray()
                    .map(::mapAlbum)
                SearchPayload(albums = items, isEnd = items.size < PAGE_SIZE)
            }
            SearchType.SHEET -> {
                val items = req.obj("songlist")
                    ?.array("list")
                    .orEmptyArray()
                    .map(::mapSheet)
                SearchPayload(sheets = items, isEnd = items.size < PAGE_SIZE)
            }
        }
    }

    suspend fun fetchTopLists(): List<PlaylistGroup> {
        val payload = buildJsonObject {
            putJsonObject("comm") {
                put("g_tk", 5381)
                put("uin", 123456)
                put("format", "json")
                put("inCharset", "utf-8")
                put("outCharset", "utf-8")
                put("notice", 0)
                put("platform", "h5")
                put("needNewCode", 1)
                put("ct", 23)
                put("cv", 0)
            }
            putJsonObject("topList") {
                put("module", "musicToplist.ToplistInfoServer")
                put("method", "GetAll")
                putJsonObject("param") {}
            }
        }

        val encoded = URLEncoder.encode(
            json.encodeToString(JsonObject.serializer(), payload),
            Charsets.UTF_8.name(),
        )
        val body = requestText(
            url = "https://u.y.qq.com/cgi-bin/musicu.fcg?_=${System.currentTimeMillis()}&data=$encoded",
            headers = defaultHeaders,
        )
        val root = parseJsonObject(body)
        val groups = root.obj("topList")
            ?.obj("data")
            ?.array("group")
            .orEmptyArray()

        return groups.map { element ->
            val item = element.jsonObject
            PlaylistGroup(
                title = item.string("groupName").orEmpty(),
                items = item.array("toplist").orEmptyArray().map { top ->
                    val raw = top.jsonObject
                    PlaylistSheet(
                        id = raw.long("topId")?.toString().orEmpty(),
                        title = raw.string("title").orEmpty(),
                        artwork = normalizeArtwork(raw.string("headPicUrl") ?: raw.string("frontPicUrl")),
                        description = raw.string("intro"),
                        period = raw.string("period"),
                    )
                },
            )
        }
    }

    suspend fun fetchTopListDetail(sheet: PlaylistSheet): PlaylistDetail {
        val payload = buildJsonObject {
            putJsonObject("detail") {
                put("module", "musicToplist.ToplistInfoServer")
                put("method", "GetDetail")
                putJsonObject("param") {
                    put("topId", sheet.id.toIntOrNull() ?: 0)
                    put("offset", 0)
                    put("num", 100)
                    put("period", sheet.period.orEmpty())
                }
            }
            putJsonObject("comm") {
                put("ct", 24)
                put("cv", 0)
            }
        }

        val encoded = URLEncoder.encode(
            json.encodeToString(JsonObject.serializer(), payload),
            Charsets.UTF_8.name(),
        )
        val body = requestText(
            url = "https://u.y.qq.com/cgi-bin/musicu.fcg?g_tk=5381&data=$encoded",
            headers = defaultHeaders,
        )
        val root = parseJsonObject(body)
        val songs = root.obj("detail")
            ?.obj("data")
            ?.array("songInfoList")
            .orEmptyArray()
            .map(::mapSong)

        return PlaylistDetail(sheet = sheet, songs = songs)
    }

    suspend fun fetchRecommendSheetTags(): RecommendTags {
        val body = requestText(
            url = "https://c.y.qq.com/splcloud/fcgi-bin/fcg_get_diss_tag_conf.fcg?format=json&inCharset=utf8&outCharset=utf-8",
            headers = qqRefererHeaders,
        )
        val root = parseJsonObject(body)
        val categories = root.obj("data")
            ?.array("categories")
            .orEmptyArray()
            .drop(1)

        val groups = categories.map { element ->
            val group = element.jsonObject
            SheetTagGroup(
                title = group.string("categoryGroupName").orEmpty(),
                items = group.array("usableItemList").orEmptyArray().map { tag ->
                    val raw = tag.jsonObject
                    SheetTag(
                        id = raw.long("categoryId")?.toString().orEmpty(),
                        title = raw.string("categoryName").orEmpty(),
                    )
                },
            )
        }

        val pinned = groups.mapNotNull { it.items.firstOrNull() }
        return RecommendTags(pinned = pinned, groups = groups)
    }

    suspend fun fetchRecommendSheetsByTag(
        tagId: String?,
        page: Int,
    ): Pair<List<PlaylistSheet>, Boolean> {
        val safeTagId = if (tagId.isNullOrBlank()) "10000000" else tagId
        val sin = (page - 1) * PAGE_SIZE
        val ein = page * PAGE_SIZE - 1
        val body = requestText(
            url = "https://c.y.qq.com/splcloud/fcgi-bin/fcg_get_diss_by_tag.fcg?inCharset=utf8&outCharset=utf-8&sortId=5&categoryId=$safeTagId&sin=$sin&ein=$ein",
            headers = qqRefererHeaders,
        )
        val root = parseJsonObject(unwrapJsonp(body))
        val data = root.obj("data") ?: return emptyList<PlaylistSheet>() to true
        val sum = data.long("sum") ?: 0L
        val list = data.array("list").orEmptyArray().map { raw ->
            mapSheet(raw)
        }
        return list to (page * PAGE_SIZE >= sum)
    }

    suspend fun fetchMusicSheetDetail(sheet: PlaylistSheet): PlaylistDetail {
        val body = requestText(
            url = "https://i.y.qq.com/qzone/fcg-bin/fcg_ucc_getcdinfo_byids_cp.fcg?type=1&utf8=1&disstid=${sheet.id}&loginUin=0",
            headers = mapOf(
                "Referer" to "https://y.qq.com/n/ryqq/playlist/${sheet.id}",
                "Cookie" to "uin=0;",
                "User-Agent" to browserUserAgent,
            ),
        )
        val root = parseJsonObject(unwrapJsonp(body))
        val cd = root.array("cdlist")
            .orEmptyArray()
            .firstOrNull()
            ?.jsonObject
            ?: return PlaylistDetail(sheet, emptyList())

        val mergedSheet = sheet.copy(
            artwork = normalizeArtwork(cd.string("logo")) ?: sheet.artwork,
            description = cd.string("desc") ?: sheet.description,
            artist = cd.string("nickname") ?: sheet.artist,
            worksNum = cd.int("songnum") ?: sheet.worksNum,
            playCount = cd.long("visitnum") ?: sheet.playCount,
        )
        val songs = cd.array("songlist").orEmptyArray().map(::mapSong)
        return PlaylistDetail(sheet = mergedSheet, songs = songs)
    }

    suspend fun fetchLyric(songmid: String): LyricPayload {
        val body = requestText(
            url = "https://c.y.qq.com/lyric/fcgi-bin/fcg_query_lyric_new.fcg?songmid=$songmid&pcachetime=${System.currentTimeMillis()}&g_tk=5381&loginUin=0&hostUin=0&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0",
            headers = mapOf(
                "Referer" to "https://y.qq.com/",
                "Cookie" to "appver=1.7.2",
                "User-Agent" to browserUserAgent,
            ),
        )
        val root = parseJsonObject(unwrapJsonp(body))
        val lyric = decodeBase64(root.string("lyric"))
        val translation = decodeBase64(root.string("trans"))
        return LyricPayload(rawLrc = lyric, translation = translation)
    }

    suspend fun fetchPlaybackSource(
        song: Song,
        quality: AudioQuality,
        source: AudioSource,
    ): PlaybackSource? {
        return when (source) {
            AudioSource.HAITANG -> {
                requestHaitangSource(song, quality) ?: requestLuoxueSource(song, quality)
            }
            AudioSource.LUOXUE -> {
                requestLuoxueSource(song, quality) ?: requestHaitangSource(song, quality)
            }
        }
    }

    private suspend fun requestLuoxueSource(song: Song, quality: AudioQuality): PlaybackSource? {
        val body = requestText(
            url = "https://lxmusicapi.onrender.com/url/tx/${song.songmid}/${quality.remoteValue}",
            headers = mapOf(
                "X-Request-Key" to "share-v3",
                "User-Agent" to browserUserAgent,
            ),
        )
        val root = parseJsonObject(body)
        val url = normalizeStreamUrl(root.string("url"))
        return url?.let { PlaybackSource(url = it) }
    }

    private suspend fun requestHaitangSource(song: Song, quality: AudioQuality): PlaybackSource? {
        val body = requestText(
            url = "https://musicapi.haitangw.net/music/qq_song_kw.php?id=${song.songmid}&type=json&level=${quality.remoteValue}",
            headers = defaultHeaders,
        )
        val root = parseJsonObject(body)
        val data = root.obj("data")
        val url = normalizeStreamUrl(data?.string("url") ?: root.string("url"))
        return url?.let { PlaybackSource(url = it) }
    }

    private suspend fun requestText(
        url: String,
        method: String = "GET",
        body: String? = null,
        headers: Map<String, String>,
    ): String {
        return withContext(Dispatchers.IO) {
            val requestBuilder = Request.Builder().url(url)
            headers.forEach { (key, value) ->
                requestBuilder.header(key, value)
            }
            if (method == "POST") {
                requestBuilder.post(
                    (body ?: "").toRequestBody("application/json; charset=utf-8".toMediaType()),
                )
            }
            client.newCall(requestBuilder.build()).execute().use { response ->
                if (!response.isSuccessful) {
                    error("HTTP ${response.code} for $url")
                }
                response.body?.string().orEmpty()
            }
        }
    }

    private fun parseJsonObject(content: String): JsonObject {
        return json.parseToJsonElement(content).jsonObject
    }

    private fun unwrapJsonp(content: String): String {
        val start = content.indexOf('(')
        val end = content.lastIndexOf(')')
        return if (start >= 0 && end > start) {
            content.substring(start + 1, end)
        } else {
            content
        }
    }

    private fun decodeBase64(value: String?): String {
        if (value.isNullOrBlank()) {
            return ""
        }
        return runCatching {
            String(Base64.decode(value, Base64.DEFAULT), Charsets.UTF_8)
        }.getOrDefault("")
    }

    private fun mapSong(element: JsonElement): Song {
        val raw = element.jsonObject
        val album = raw.obj("album")
        val singers = raw.array("singer").orEmptyArray().joinToString(", ") { singer ->
            singer.jsonObject.string("name").orEmpty()
        }
        return Song(
            id = raw.long("id")?.toString()
                ?: raw.long("songid")?.toString()
                ?: raw.string("id")
                .orEmpty(),
            songmid = raw.string("mid")
                ?: raw.string("songmid")
                .orEmpty(),
            title = raw.string("title")
                ?: raw.string("songname")
                ?: raw.string("name")
                .orEmpty(),
            artist = singers.ifBlank { raw.string("artist").orEmpty() },
            artwork = normalizeArtwork(
                raw.string("artwork")
                    ?: album?.let { "https://y.gtimg.cn/music/photo_new/T002R800x800M000${it.string("mid")}.jpg" },
            ),
            album = album?.string("title")
                ?: album?.string("name")
                ?: raw.string("albumname"),
            albumId = album?.long("id")?.toString()
                ?: raw.long("albumid")?.toString(),
            albumMid = album?.string("mid")
                ?: raw.string("albummid"),
            duration = raw.int("interval"),
        )
    }

    private fun mapArtist(element: JsonElement): Artist {
        val raw = element.jsonObject
        return Artist(
            id = raw.long("singerID")?.toString().orEmpty(),
            singerMid = raw.string("singerMID").orEmpty(),
            name = raw.string("singerName").orEmpty(),
            artwork = normalizeArtwork(raw.string("singerPic")),
            workCount = raw.int("songNum"),
        )
    }

    private fun mapAlbum(element: JsonElement): Album {
        val raw = element.jsonObject
        return Album(
            id = raw.long("albumID")?.toString().orEmpty(),
            albumMid = raw.string("albumMID").orEmpty(),
            title = raw.string("albumName").orEmpty(),
            artist = raw.string("singerName"),
            artwork = normalizeArtwork(raw.string("albumPic")),
            date = raw.string("publicTime"),
            workCount = raw.int("song_count"),
        )
    }

    private fun mapSheet(element: JsonElement): PlaylistSheet {
        val raw = element.jsonObject
        val creator = raw.obj("creator")
        return PlaylistSheet(
            id = raw.long("dissid")?.toString()
                ?: raw.string("dissid")
                .orEmpty(),
            title = raw.string("dissname").orEmpty(),
            artwork = normalizeArtwork(raw.string("imgurl")),
            description = raw.string("introduction"),
            artist = creator?.string("name") ?: raw.string("nickname"),
            playCount = raw.long("listennum"),
            createTime = raw.string("createtime"),
            worksNum = raw.int("song_count"),
        )
    }

    private fun normalizeStreamUrl(url: String?): String? {
        return url
            ?.takeIf { it.isNotBlank() }
            ?.replace(" ", "%20")
    }

    private fun JsonArray?.orEmptyArray(): List<JsonElement> = this?.toList().orEmpty()

    companion object {
        private const val PAGE_SIZE = 20
        private const val browserUserAgent =
            "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"

        private val defaultHeaders = mapOf(
            "User-Agent" to browserUserAgent,
            "Referer" to "https://y.qq.com/",
        )

        private val qqRefererHeaders = mapOf(
            "User-Agent" to browserUserAgent,
            "Referer" to "https://y.qq.com/",
        )
    }
}
