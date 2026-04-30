package com.musicfree.android.ui.navigation

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.ui.graphics.vector.ImageVector
import com.musicfree.android.data.model.PlaylistSheet

sealed class BottomDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    data object Home : BottomDestination("home", "首页", Icons.Outlined.Home)
    data object Queue : BottomDestination("queue", "播放列表", Icons.AutoMirrored.Outlined.QueueMusic)
    data object Favorites : BottomDestination("favorites", "收藏", Icons.Outlined.FavoriteBorder)
    data object MySheets : BottomDestination("my_sheets", "我的歌单", Icons.Outlined.LibraryMusic)
    data object Profile : BottomDestination("profile", "我的", Icons.Outlined.PersonOutline)
}

object AppDestinations {
    const val search = "search"
    const val player = "player"
    const val detail = "detail/{kind}/{id}?title={title}&artwork={artwork}&description={description}&artist={artist}&period={period}"

    fun buildDetailRoute(
        kind: String,
        sheet: PlaylistSheet,
    ): String {
        return buildString {
            append("detail/")
            append(kind)
            append("/")
            append(Uri.encode(sheet.id))
            append("?title=${Uri.encode(sheet.title)}")
            append("&artwork=${Uri.encode(sheet.artwork.orEmpty())}")
            append("&description=${Uri.encode(sheet.description.orEmpty())}")
            append("&artist=${Uri.encode(sheet.artist.orEmpty())}")
            append("&period=${Uri.encode(sheet.period.orEmpty())}")
        }
    }
}

val bottomDestinations = listOf(
    BottomDestination.Home,
    BottomDestination.Queue,
    BottomDestination.Favorites,
    BottomDestination.MySheets,
    BottomDestination.Profile,
)
