package com.musicfree.android.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.musicfree.android.data.model.Album
import com.musicfree.android.data.model.PlaylistSheet
import com.musicfree.android.data.model.SearchType
import com.musicfree.android.data.model.Artist
import com.musicfree.android.data.model.Song
import com.musicfree.android.ui.components.EmptyContent
import com.musicfree.android.ui.components.PlaylistCard
import com.musicfree.android.ui.components.SongRow
import com.musicfree.android.ui.theme.Aqua500
import com.musicfree.android.ui.theme.Gray100
import com.musicfree.android.ui.theme.Gray300
import com.musicfree.android.ui.theme.Gray500
import com.musicfree.android.ui.theme.Navy900
import com.musicfree.android.ui.viewmodel.SearchUiState

@Composable
fun SearchScreen(
    state: SearchUiState,
    modifier: Modifier = Modifier,
    currentPlayingSongId: String?,
    playerLoading: Boolean,
    onBack: () -> Unit,
    onQueryChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onTypeSelected: (SearchType) -> Unit,
    onPlaySong: (Song, List<Song>) -> Unit,
    onOpenPlaylist: (PlaylistSheet) -> Unit,
    onOpenArtist: (Artist) -> Unit,
    onOpenAlbum: (Album) -> Unit,
    onUseHistory: (String) -> Unit,
    onClearHistory: () -> Unit,
    onLoadMore: () -> Unit,
    onToggleFavorite: (Song) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChanged,
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
                placeholder = { Text("搜索歌曲、歌手、专辑") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = Gray500,
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Gray100,
                    unfocusedContainerColor = Gray100,
                    focusedBorderColor = Aqua500.copy(alpha = 0.4f),
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Navy900,
                    unfocusedTextColor = Navy900,
                    cursorColor = Navy900,
                    focusedPlaceholderColor = Gray500,
                    unfocusedPlaceholderColor = Gray500,
                ),
            )
            Text(
                text = "取消",
                modifier = Modifier.clickable(onClick = onBack),
                color = Aqua500,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SearchType.entries.forEach { type ->
                val selected = type == state.currentType
                Text(
                    text = type.title,
                    modifier = Modifier
                        .clickable { onTypeSelected(type) }
                        .padding(vertical = 10.dp),
                    color = if (selected) Aqua500 else Gray500,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Gray100),
        )

        if (state.query.isBlank() && state.history.isNotEmpty()) {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("搜索历史", style = MaterialTheme.typography.titleMedium, color = Navy900)
                        Text(
                            text = "清空",
                            modifier = Modifier.clickable(onClick = onClearHistory),
                            style = MaterialTheme.typography.labelLarge,
                            color = Gray500,
                        )
                    }
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.history) { history ->
                            Surface(
                                modifier = Modifier.clickable { onUseHistory(history) },
                                shape = MaterialTheme.shapes.large,
                                color = Gray100,
                            ) {
                                Text(
                                    text = history,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Navy900,
                                )
                            }
                        }
                    }
                }
            }
            return
        }

        if (state.loading && !state.hasResults) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Aqua500)
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
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.query.isNotBlank() && state.hasResults) {
                item {
                    Text(
                        text = "搜索结果",
                        style = MaterialTheme.typography.labelLarge,
                        color = Gray500,
                    )
                }
            }

            when (state.currentType) {
                SearchType.SONG -> {
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
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenArtist(artist) },
                            shape = MaterialTheme.shapes.large,
                            color = Color.White,
                            border = BorderStroke(1.dp, Gray100),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = artist.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Navy900,
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "作品 ${artist.workCount ?: 0}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Gray500,
                                )
                            }
                        }
                    }
                }

                SearchType.ALBUM -> {
                    items(state.albums, key = { it.albumMid }) { album ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenAlbum(album) },
                            shape = MaterialTheme.shapes.large,
                            color = Color.White,
                            border = BorderStroke(1.dp, Gray100),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = album.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Navy900,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = listOfNotNull(album.artist, album.date).joinToString(" · "),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Gray500,
                                )
                            }
                        }
                    }
                }
            }

            if (!state.isEnd && state.hasResults) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onLoadMore),
                        shape = MaterialTheme.shapes.large,
                        color = Gray100,
                        border = BorderStroke(1.dp, Gray300),
                    ) {
                        Text(
                            text = if (state.loading) "加载中…" else "继续加载",
                            modifier = Modifier.padding(vertical = 14.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Navy900,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}
