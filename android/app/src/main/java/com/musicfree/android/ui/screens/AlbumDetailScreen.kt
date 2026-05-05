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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.musicfree.android.data.model.Song
import com.musicfree.android.ui.components.EmptyContent
import com.musicfree.android.ui.theme.Aqua500
import com.musicfree.android.ui.theme.Coral400
import com.musicfree.android.ui.theme.Gray100
import com.musicfree.android.ui.theme.Gray500
import com.musicfree.android.ui.theme.Mint100
import com.musicfree.android.ui.theme.Mint300
import com.musicfree.android.ui.theme.Navy900
import com.musicfree.android.ui.theme.Navy700
import com.musicfree.android.ui.viewmodel.AlbumDetailUiState

@Composable
fun AlbumDetailScreen(
    state: AlbumDetailUiState,
    modifier: Modifier = Modifier,
    currentPlayingSongId: String?,
    playerLoading: Boolean,
    onBack: () -> Unit,
    onPlaySong: (Song, List<Song>) -> Unit,
    onToggleFavorite: (Song) -> Unit,
) {
    when {
        state.loading -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Gray100),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Aqua500)
            }
        }

        state.songs.isEmpty() -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(Gray100),
            ) {
                AlbumHeader(
                    title = state.title,
                    artist = state.artist,
                    artwork = state.artwork,
                    onBack = onBack,
                )
                EmptyContent(
                    title = "还没有找到专辑歌曲",
                    subtitle = state.error ?: "这个专辑暂时没加载出来。",
                    modifier = Modifier.padding(20.dp),
                )
            }
        }

        else -> {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .background(Gray100),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    AlbumHeader(
                        title = state.title,
                        artist = state.artist,
                        artwork = state.artwork,
                        onBack = onBack,
                    )
                }
                item {
                    Text(
                        text = "歌曲 ${state.songs.size} 首",
                        style = MaterialTheme.typography.labelLarge,
                        color = Gray500,
                    )
                }
                items(state.songs, key = { it.identity }) { song ->
                    AlbumSongRow(
                        song = song,
                        onPlay = { onPlaySong(song, state.songs) },
                        onFavorite = { onToggleFavorite(song) },
                        isActive = currentPlayingSongId == song.identity,
                        isLoading = playerLoading && currentPlayingSongId == song.identity,
                    )
                }
            }
        }
    }
}

@Composable
private fun AlbumSongRow(
    song: Song,
    onPlay: () -> Unit,
    onFavorite: () -> Unit,
    isActive: Boolean,
    isLoading: Boolean,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay),
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = song.artwork,
                contentDescription = song.title,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Navy900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    song.badges.take(3).forEach { badge ->
                        AlbumSongBadge(text = badge)
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
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = if (isActive || isLoading) Aqua500.copy(alpha = 0.14f) else Color.White,
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (isActive || isLoading) Aqua500.copy(alpha = 0.24f) else Gray100,
                ),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    when {
                        isLoading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Aqua500,
                            )
                        }
                        isActive -> {
                            Icon(
                                imageVector = Icons.Outlined.GraphicEq,
                                contentDescription = "当前播放",
                                tint = Aqua500,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Outlined.PlayArrow,
                                contentDescription = "播放",
                                tint = Gray500,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
            IconButton(onClick = onFavorite) {
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
private fun AlbumSongBadge(
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
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelLarge,
            color = foreground,
        )
    }
}

@Composable
private fun AlbumHeader(
    title: String,
    artist: String?,
    artwork: String?,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "返回",
                tint = Navy900,
            )
        }
        AsyncImage(
            model = artwork,
            contentDescription = title,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop,
        )
        Spacer(modifier = Modifier.padding(6.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Navy900,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = artist ?: "专辑",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
