package com.musicfree.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.musicfree.android.core.formatPlayCount
import com.musicfree.android.data.model.Song
import com.musicfree.android.ui.components.EmptyContent
import com.musicfree.android.ui.components.GlassPanel
import com.musicfree.android.ui.theme.Aqua500
import com.musicfree.android.ui.theme.Coral400
import com.musicfree.android.ui.theme.Gray500
import com.musicfree.android.ui.theme.Mint100
import com.musicfree.android.ui.theme.Mint300
import com.musicfree.android.ui.theme.Navy700
import com.musicfree.android.ui.theme.Navy900
import com.musicfree.android.ui.viewmodel.PlaylistDetailUiState

@Composable
fun PlaylistDetailScreen(
    state: PlaylistDetailUiState,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onPlaySong: (Song, List<Song>) -> Unit,
    onPlayAll: (List<Song>) -> Unit,
    onToggleFavorite: (Song) -> Unit,
) {
    when {
        state.loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        state.detail == null -> {
            EmptyContent(
                title = "歌单加载失败",
                subtitle = state.error ?: "当前歌单可能不可访问，或者接口暂时不可用。",
                modifier = modifier.padding(20.dp),
            )
        }

        else -> {
            val detail = state.detail
            val favoriteCount = detail.songs.count { it.isFavorite }
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFDDF6FF), Color(0xFFF7FCFF), Color.White),
                        ),
                    ),
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                    contentDescription = "返回",
                                    tint = Navy700,
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = {}) {
                                Icon(
                                    imageVector = Icons.Outlined.Share,
                                    contentDescription = "分享",
                                    tint = Navy700,
                                )
                            }
                        }
                    }

                    item {
                        GlassPanel {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    AsyncImage(
                                        model = detail.sheet.artwork,
                                        contentDescription = detail.sheet.title,
                                        modifier = Modifier
                                            .size(94.dp)
                                            .clip(RoundedCornerShape(22.dp)),
                                        contentScale = ContentScale.Crop,
                                    )
                                    Spacer(modifier = Modifier.padding(6.dp))
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Text(
                                            text = detail.sheet.title,
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = Navy900,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            text = detail.sheet.artist ?: "推荐歌单",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = Navy700.copy(alpha = 0.82f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            text = detail.sheet.description ?: "精选歌曲集合",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Gray500,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                                        ) {
                                            HeaderStat(
                                                icon = Icons.Outlined.FavoriteBorder,
                                                text = favoriteCount.toString(),
                                            )
                                            HeaderStat(
                                                icon = Icons.AutoMirrored.Outlined.QueueMusic,
                                                text = "${detail.sheet.worksNum ?: detail.songs.size}",
                                            )
                                            HeaderStat(
                                                icon = Icons.Outlined.PlayArrow,
                                                text = formatPlayCount(detail.sheet.playCount),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color.White.copy(alpha = 0.92f),
                            shadowElevation = 6.dp,
                            tonalElevation = 3.dp,
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                FilledIconButton(
                                    onClick = { onPlayAll(detail.songs) },
                                    modifier = Modifier.size(38.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.PlayArrow,
                                        contentDescription = "播放全部",
                                    )
                                }
                                Spacer(modifier = Modifier.padding(4.dp))
                                Text(
                                    text = "播放全部",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Navy900,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    imageVector = Icons.Outlined.MoreHoriz,
                                    contentDescription = null,
                                    tint = Gray500,
                                )
                            }
                        }
                    }

                    itemsIndexed(detail.songs, key = { _, song -> song.identity }) { index, song ->
                        PlaylistSongRow(
                            index = index + 1,
                            song = song,
                            onPlay = { onPlaySong(song, detail.songs) },
                            onToggleFavorite = { onToggleFavorite(song) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(15.dp),
            tint = Gray500,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = Gray500,
        )
    }
}

@Composable
private fun PlaylistSongRow(
    index: Int,
    song: Song,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay),
        shape = RoundedCornerShape(22.dp),
        color = Color.White.copy(alpha = 0.92f),
        shadowElevation = 4.dp,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = index.toString(),
                modifier = Modifier.size(22.dp),
                style = MaterialTheme.typography.titleMedium,
                color = Gray500,
            )
            Spacer(modifier = Modifier.padding(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Navy900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.padding(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    song.badges.take(3).forEach { badge ->
                        SongBadge(text = badge)
                    }
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = Mint300.copy(alpha = 0.28f),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PlayArrow,
                        contentDescription = "播放",
                        tint = Aqua500,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (song.isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (song.isFavorite) "取消收藏" else "收藏",
                    tint = if (song.isFavorite) Coral400 else Gray500,
                )
            }
        }
    }
}

@Composable
private fun SongBadge(
    text: String,
) {
    val (background, foreground) = when (text) {
        "VIP" -> Color(0xFFFFF0E6) to Color(0xFFFF7A45)
        "SQ" -> Color(0xFFE8FBF7) to Color(0xFF17B890)
        "HQ" -> Color(0xFFEAF4FF) to Color(0xFF4C8DFF)
        "MV" -> Color(0xFFFFEEF4) to Color(0xFFFF5C8A)
        else -> Mint100 to Navy700
    }
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = background,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = foreground,
        )
    }
}
