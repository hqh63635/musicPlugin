package com.musicfree.android.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.musicfree.android.data.model.Song
import com.musicfree.android.ui.components.EmptyContent
import com.musicfree.android.ui.components.SongRow
import com.musicfree.android.ui.theme.Aqua500
import com.musicfree.android.ui.theme.Gray100
import com.musicfree.android.ui.theme.Gray500
import com.musicfree.android.ui.theme.Navy900
import com.musicfree.android.ui.viewmodel.ArtistDetailUiState

@Composable
fun ArtistDetailScreen(
    state: ArtistDetailUiState,
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
                ArtistHeader(
                    name = state.name,
                    artwork = state.artwork,
                    onBack = onBack,
                )
                EmptyContent(
                    title = "还没有找到歌曲",
                    subtitle = state.error ?: "这个艺人的歌曲暂时没加载出来。",
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
                    ArtistHeader(
                        name = state.name,
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
                    SongRow(
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
private fun ArtistHeader(
    name: String,
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
            contentDescription = name,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
        Spacer(modifier = Modifier.padding(6.dp))
        Column {
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge,
                color = Navy900,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "艺人",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500,
            )
        }
    }
}
