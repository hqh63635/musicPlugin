package com.musicfree.android.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.musicfree.android.core.formatPlayCount
import com.musicfree.android.data.model.Song
import com.musicfree.android.ui.components.EmptyContent
import com.musicfree.android.ui.components.GlassPanel
import com.musicfree.android.ui.components.MetaBadge
import com.musicfree.android.ui.components.SectionHeader
import com.musicfree.android.ui.components.SongRow
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
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
            }
        }

        state.detail == null -> {
            EmptyContent(
                title = "歌单加载失败",
                subtitle = state.error ?: "当前内容可能是私密歌单，或者接口暂时不可用。",
                modifier = modifier.padding(20.dp),
            )
        }

        else -> {
            val detail = state.detail
            LazyColumn(
                modifier = modifier,
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "返回",
                        )
                    }
                }
                item {
                    GlassPanel {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = detail.sheet.artwork,
                                    contentDescription = detail.sheet.title,
                                    modifier = Modifier
                                        .height(120.dp)
                                        .fillMaxWidth(0.33f),
                                )
                                Spacer(modifier = Modifier.padding(8.dp))
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Text(
                                        text = detail.sheet.title,
                                        style = MaterialTheme.typography.headlineMedium,
                                    )
                                    Text(
                                        text = detail.sheet.description ?: "公开歌单详情直接取自 QQ 歌单接口。",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        MetaBadge(text = formatPlayCount(detail.sheet.playCount))
                                        detail.sheet.worksNum?.let { MetaBadge(text = "$it 首") }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(18.dp))
                            OutlinedButton(onClick = { onPlayAll(detail.songs) }) {
                                Text("播放全部")
                            }
                        }
                    }
                }
                item {
                    SectionHeader(title = "歌曲列表")
                }
                items(detail.songs, key = { it.identity }) { song ->
                    SongRow(
                        song = song,
                        onPlay = { onPlaySong(song, detail.songs) },
                        onFavorite = { onToggleFavorite(song) },
                    )
                }
            }
        }
    }
}
