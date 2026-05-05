package com.musicfree.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.musicfree.android.data.model.PlaylistSheet
import com.musicfree.android.data.model.SheetTag
import com.musicfree.android.data.model.Song
import com.musicfree.android.ui.components.ActionTile
import com.musicfree.android.ui.components.EmptyContent
import com.musicfree.android.ui.components.PlaylistCard
import com.musicfree.android.ui.components.SearchEntryBar
import com.musicfree.android.ui.components.SectionHeader
import com.musicfree.android.ui.components.SongRow
import com.musicfree.android.ui.theme.Aqua500
import com.musicfree.android.ui.theme.Coral400
import com.musicfree.android.ui.theme.Gold300
import com.musicfree.android.ui.theme.Gray100
import com.musicfree.android.ui.theme.Gray300
import com.musicfree.android.ui.theme.Gray500
import com.musicfree.android.ui.theme.Mint100
import com.musicfree.android.ui.theme.Navy900
import com.musicfree.android.ui.viewmodel.HomeUiState

@Composable
fun HomeScreen(
    state: HomeUiState,
    modifier: Modifier = Modifier,
    currentPlayingSongId: String?,
    playerLoading: Boolean,
    onSearchClick: () -> Unit,
    onPlaySong: (Song, List<Song>) -> Unit,
    onPlayFeatured: (List<Song>) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onOpenPlaylist: (PlaylistSheet, String) -> Unit,
    onTagSelected: (SheetTag) -> Unit,
) {
    when {
        state.loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Aqua500)
            }
        }

        state.feed == null -> {
            EmptyContent(
                title = "首页暂时空了",
                subtitle = state.error ?: "请检查网络后重试",
                modifier = modifier.padding(20.dp),
            )
        }

        else -> {
            val feed = state.feed
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentPadding = PaddingValues(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 14.dp, start = 24.dp, end = 24.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            listOf("我的", "音乐馆", "动态").forEach { tab ->
                                val active = tab == "音乐馆"
                                Text(
                                    text = tab,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (active) Navy900 else Gray500,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 24.dp, vertical = 10.dp)
                                .fillMaxWidth(),
                        ) {
                            SearchEntryBar(
                                placeholder = "搜索歌曲、歌手、专辑",
                                onClick = onSearchClick,
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .padding(horizontal = 24.dp, vertical = 2.dp)
                                .fillMaxWidth()
                                .clickable {
                                    feed.featuredChart?.let { onOpenPlaylist(it, "chart") }
                                },
                            shape = MaterialTheme.shapes.large,
                            color = Color.Transparent,
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.large)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(Aqua500, Color(0xFF2DB87E), Color(0xFF28A06C)),
                                        ),
                                    )
                                    .padding(horizontal = 24.dp, vertical = 22.dp),
                            ) {
                                Column {
                                    Text(
                                        text = feed.featuredChart?.period ?: "飙升榜",
                                        color = Color.White.copy(alpha = 0.8f),
                                        style = MaterialTheme.typography.labelLarge,
                                    )
                                    Text(
                                        text = feed.featuredChart?.title ?: "本周最热歌曲",
                                        color = Color.White,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Black,
                                    )
                                    Text(
                                        text = "实时更新",
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                    Surface(
                                        modifier = Modifier.padding(top = 16.dp),
                                        shape = MaterialTheme.shapes.large,
                                        color = Color.White.copy(alpha = 0.16f),
                                    ) {
                                        Text(
                                            text = "播放全部",
                                            modifier = Modifier
                                                .clickable { onPlayFeatured(feed.featuredSongs) }
                                                .padding(horizontal = 14.dp, vertical = 8.dp),
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelLarge,
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .size(82.dp)
                                        .clip(MaterialTheme.shapes.extraLarge)
                                        .background(Color.White.copy(alpha = 0.14f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "♪",
                                        color = Color.White,
                                        style = MaterialTheme.typography.headlineLarge,
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            ActionTile(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.Search,
                                title = "搜索",
                                accent = Aqua500,
                                onClick = onSearchClick,
                            )
                            ActionTile(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.StarOutline,
                                title = "热榜",
                                accent = Coral400,
                                onClick = {
                                    feed.featuredChart?.let { onOpenPlaylist(it, "chart") }
                                },
                            )
                            ActionTile(
                                modifier = Modifier.weight(1f),
                                icon = Icons.AutoMirrored.Outlined.QueueMusic,
                                title = "歌单",
                                accent = Gold300,
                                onClick = {
                                    feed.recommendedSheets.firstOrNull()?.let {
                                        onOpenPlaylist(it, "sheet")
                                    }
                                },
                            )
                            ActionTile(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.FavoriteBorder,
                                title = "收藏",
                                accent = Coral400,
                                onClick = {
                                    feed.featuredSongs.firstOrNull()?.let(onToggleFavorite)
                                },
                            )
                        }
                    }
                }

                item {
                    SectionHeader(
                        title = "推荐歌单",
                        modifier = Modifier.padding(horizontal = 24.dp),
                        trailing = {
                            Text(
                                text = "更多",
                                style = MaterialTheme.typography.labelLarge,
                                color = Gray500,
                            )
                        },
                    )
                }

                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        items(feed.recommendedSheets, key = { it.id }) { sheet ->
                            PlaylistCard(
                                sheet = sheet,
                                modifier = Modifier.size(width = 120.dp, height = 188.dp),
                                onClick = { onOpenPlaylist(sheet, "sheet") },
                            )
                        }
                    }
                }

                item {
                    SectionHeader(
                        title = "精选标签",
                        modifier = Modifier.padding(horizontal = 24.dp),
                        trailing = {
                            Text(
                                text = "切换推荐",
                                style = MaterialTheme.typography.labelLarge,
                                color = Gray500,
                            )
                        },
                    )
                }

                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(feed.recommendedTags.pinned, key = { it.id }) { tag ->
                            val selected = feed.selectedTag?.id == tag.id
                            Surface(
                                modifier = Modifier.clickable { onTagSelected(tag) },
                                shape = MaterialTheme.shapes.large,
                                color = if (selected) Mint100 else Gray100,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (selected) Aqua500.copy(alpha = 0.25f) else Gray300,
                                ),
                            ) {
                                Text(
                                    text = tag.title,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) Aqua500 else Navy900,
                                )
                            }
                        }
                    }
                }

                item {
                    SectionHeader(
                        title = "今日轻听",
                        modifier = Modifier.padding(horizontal = 24.dp),
                        trailing = {
                            Text(
                                text = "${feed.featuredSongs.size} 首",
                                style = MaterialTheme.typography.labelLarge,
                                color = Gray500,
                            )
                        },
                    )
                }

                items(feed.featuredSongs, key = { it.identity }) { song ->
                    SongRow(
                        song = song,
                        modifier = Modifier.padding(horizontal = 24.dp),
                        onPlay = { onPlaySong(song, feed.featuredSongs) },
                        onFavorite = { onToggleFavorite(song) },
                        isActive = currentPlayingSongId == song.identity,
                        isLoading = playerLoading && currentPlayingSongId == song.identity,
                    )
                }
            }
        }
    }
}
