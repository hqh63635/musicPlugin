package com.musicfree.android.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.musicfree.android.data.model.PlaylistSheet
import com.musicfree.android.data.model.SearchType
import com.musicfree.android.data.model.Song
import com.musicfree.android.ui.components.EmptyContent
import com.musicfree.android.ui.components.GlassPanel
import com.musicfree.android.ui.components.MetaBadge
import com.musicfree.android.ui.components.PlaylistCard
import com.musicfree.android.ui.components.SongRow
import com.musicfree.android.ui.viewmodel.SearchUiState

@Composable
fun SearchScreen(
    state: SearchUiState,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onQueryChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onTypeSelected: (SearchType) -> Unit,
    onPlaySong: (Song, List<Song>) -> Unit,
    onOpenPlaylist: (PlaylistSheet) -> Unit,
    onUseHistory: (String) -> Unit,
    onClearHistory: () -> Unit,
    onLoadMore: () -> Unit,
    onToggleFavorite: (Song) -> Unit,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChanged,
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
                placeholder = {
                    Text("输入歌曲、歌手、专辑或歌单")
                },
                trailingIcon = {
                    IconButton(onClick = onSubmit) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "搜索",
                        )
                    }
                },
            )
        }
        TabRow(selectedTabIndex = state.currentType.ordinal) {
            SearchType.entries.forEach { type ->
                Tab(
                    selected = type == state.currentType,
                    onClick = { onTypeSelected(type) },
                    text = { Text(type.title) },
                )
            }
        }
        if (state.query.isBlank() && state.history.isNotEmpty()) {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    GlassPanel {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "搜索历史",
                                    style = MaterialTheme.typography.titleLarge,
                                )
                                Text(
                                    text = "清空",
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .clickable(onClick = onClearHistory),
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(state.history) { history ->
                                    androidx.compose.material3.Surface(
                                        shape = MaterialTheme.shapes.large,
                                        modifier = Modifier.clickable {
                                            onUseHistory(history)
                                        },
                                    ) {
                                        Text(
                                            text = history,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return
        }

        if (state.loading && !state.hasResults) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
            }
            return
        }

        val isEmpty = !state.loading && !state.hasResults && state.query.isNotBlank()
        if (isEmpty) {
            EmptyContent(
                title = "没有搜到内容",
                subtitle = state.error ?: "换个关键词再试一次。",
                modifier = Modifier.padding(20.dp),
            )
            return
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            when (state.currentType) {
                SearchType.SONG -> {
                    items(state.songs, key = { it.identity }) { song ->
                        SongRow(
                            song = song,
                            onPlay = { onPlaySong(song, state.songs) },
                            onFavorite = { onToggleFavorite(song) },
                        )
                    }
                }
                SearchType.SHEET -> {
                    items(state.sheets, key = { it.id }) { sheet ->
                        PlaylistCard(
                            sheet = sheet,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onOpenPlaylist(sheet) },
                        )
                    }
                }
                SearchType.ARTIST -> {
                    items(state.artists, key = { it.singerMid }) { artist ->
                        GlassPanel {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = artist.name,
                                    style = MaterialTheme.typography.titleLarge,
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "作品 ${artist.workCount ?: 0}",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
                SearchType.ALBUM -> {
                    items(state.albums, key = { it.albumMid }) { album ->
                        GlassPanel {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = album.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = listOfNotNull(album.artist, album.date).joinToString(" · "),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
            }
            if (!state.isEnd && state.hasResults) {
                item {
                    GlassPanel(modifier = Modifier.clickable(onClick = onLoadMore)) {
                        Text(
                            text = if (state.loading) "加载中…" else "继续加载",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                        )
                    }
                }
            }
        }
    }
}
