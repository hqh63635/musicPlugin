package com.musicfree.android.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.musicfree.android.data.model.Song
import com.musicfree.android.ui.components.EmptyContent
import com.musicfree.android.ui.components.GlassPanel
import com.musicfree.android.ui.components.SectionHeader
import com.musicfree.android.ui.components.SongRow
import com.musicfree.android.ui.viewmodel.LibraryUiState

@Composable
fun FavoritesScreen(
    state: LibraryUiState,
    modifier: Modifier = Modifier,
    onPlaySong: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SectionHeader(
                title = "我喜欢的歌",
                trailing = { Text(text = "${state.favorites.size} 首") },
            )
        }
        if (state.favorites.isEmpty()) {
            item {
                EmptyContent(
                    title = "还没有收藏",
                    subtitle = "在首页、搜索页或歌单详情里点亮心形，歌曲就会出现在这里。",
                )
            }
        } else {
            item {
                GlassPanel {
                    Text(
                        text = "收藏会持久化保存在本地，离开应用再回来也还在。",
                        modifier = Modifier.padding(18.dp),
                    )
                }
            }
            items(state.favorites, key = { it.identity }) { song ->
                SongRow(
                    song = song.copy(isFavorite = true),
                    onPlay = { onPlaySong(song) },
                    onFavorite = { onToggleFavorite(song) },
                )
            }
        }
    }
}
