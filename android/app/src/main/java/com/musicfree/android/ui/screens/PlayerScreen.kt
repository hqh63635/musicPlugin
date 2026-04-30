package com.musicfree.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.musicfree.android.core.formatDuration
import com.musicfree.android.player.PlayerUiState
import com.musicfree.android.ui.theme.Mint100
import com.musicfree.android.ui.theme.Navy900

@Composable
fun PlayerScreen(
    state: PlayerUiState,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onSeek: (Float) -> Unit,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
) {
    val song = state.currentSong
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFE9FCFF), Mint100, Color.White),
                ),
            ),
    ) {
        if (song == null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("还没有正在播放的歌曲")
            }
            return
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                    }
                    Text(
                        text = "正在播放",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            item {
                AsyncImage(
                    model = song.artwork,
                    contentDescription = song.title,
                    modifier = Modifier
                        .padding(horizontal = 28.dp)
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.extraLarge),
                    contentScale = ContentScale.Crop,
                )
            }
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Navy900,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Navy900.copy(alpha = 0.65f),
                    )
                }
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                ) {
                    Slider(
                        value = state.progress,
                        onValueChange = onSeek,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(formatDuration(state.positionMs))
                        Text(formatDuration(state.durationMs))
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onPrevious) {
                        Icon(
                            imageVector = Icons.Outlined.SkipPrevious,
                            contentDescription = "上一首",
                            modifier = Modifier.size(32.dp),
                        )
                    }
                    IconButton(onClick = onTogglePlay) {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                            contentDescription = if (state.isPlaying) "暂停" else "播放",
                            modifier = Modifier.size(44.dp),
                        )
                    }
                    IconButton(onClick = onNext) {
                        Icon(
                            imageVector = Icons.Outlined.SkipNext,
                            contentDescription = "下一首",
                            modifier = Modifier.size(32.dp),
                        )
                    }
                }
            }
            item {
                Text(
                    text = "歌词",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                )
            }
            itemsIndexed(state.lyrics, key = { index, line -> "${line.timeMs}-$index" }) { index, line ->
                Text(
                    text = line.text,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    style = if (index == state.currentLyricIndex) {
                        MaterialTheme.typography.titleMedium
                    } else {
                        MaterialTheme.typography.bodyLarge
                    },
                    textAlign = TextAlign.Center,
                    color = if (index == state.currentLyricIndex) Navy900 else Navy900.copy(alpha = 0.55f),
                )
            }
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
