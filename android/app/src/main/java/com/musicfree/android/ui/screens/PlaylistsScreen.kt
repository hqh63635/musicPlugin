package com.musicfree.android.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.musicfree.android.ui.components.EmptyContent
import com.musicfree.android.ui.components.GlassPanel
import com.musicfree.android.ui.components.SectionHeader
import com.musicfree.android.ui.components.SongRow
import com.musicfree.android.ui.viewmodel.LibraryUiState

@Composable
fun PlaylistsScreen(
    state: LibraryUiState,
    modifier: Modifier = Modifier,
    onPlaySong: (Int) -> Unit,
    onRemoveSong: (Int) -> Unit,
    onClearQueue: () -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SectionHeader(
                title = "当前播放列表",
                trailing = {
                    if (state.queue.isNotEmpty()) {
                        Text(
                            text = "共 ${state.queue.size} 首",
                        )
                    }
                },
            )
        }
        item {
            GlassPanel {
                SectionHeader(
                    title = "现在播放",
                    modifier = Modifier.padding(18.dp),
                    trailing = {
                        if (state.queue.isNotEmpty()) {
                            IconButton(onClick = onClearQueue) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = "清空",
                                )
                            }
                        }
                    },
                )
            }
        }
        if (state.queue.isEmpty()) {
            item {
                EmptyContent(
                    title = "播放列表还是空的",
                    subtitle = "从首页或搜索页挑一首歌，原生播放器会直接把它加进来。",
                )
            }
        } else {
            itemsIndexed(state.queue, key = { _, song -> song.identity }) { index, song ->
                SongRow(
                    song = song,
                    onPlay = { onPlaySong(index) },
                    trailing = {
                        IconButton(onClick = { onRemoveSong(index) }) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = "移除",
                            )
                        }
                    },
                )
            }
        }
    }
}
