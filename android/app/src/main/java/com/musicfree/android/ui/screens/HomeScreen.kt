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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp
import com.musicfree.android.data.model.PlaylistSheet
import com.musicfree.android.data.model.SheetTag
import com.musicfree.android.data.model.Song
import com.musicfree.android.ui.components.ActionTile
import com.musicfree.android.ui.components.EmptyContent
import com.musicfree.android.ui.components.GlassPanel
import com.musicfree.android.ui.components.HeroCard
import com.musicfree.android.ui.components.PlaylistCard
import com.musicfree.android.ui.components.SearchEntryBar
import com.musicfree.android.ui.components.SectionHeader
import com.musicfree.android.ui.components.SongRow
import com.musicfree.android.ui.theme.Aqua500
import com.musicfree.android.ui.theme.Coral400
import com.musicfree.android.ui.theme.Gray500
import com.musicfree.android.ui.theme.Mint300
import com.musicfree.android.ui.theme.Navy700
import com.musicfree.android.ui.viewmodel.HomeUiState

@Composable
fun HomeScreen(
    state: HomeUiState,
    modifier: Modifier = Modifier,
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
                CircularProgressIndicator()
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
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFF0FEFC), Color(0xFFF7FCFF), Color.White),
                        ),
                    ),
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            SearchEntryBar(onClick = onSearchClick)
                            HeroCard(
                                title = feed.featuredChart?.title ?: "今日推荐",
                                subtitle = feed.featuredChart?.period ?: "热播榜单",
                                description = feed.featuredChart?.description
                                    ?: "从 QQ 榜单和推荐歌单里挑一组适合直接开播的内容。",
                                actionText = "播放全部",
                                artwork = feed.featuredChart?.artwork,
                                onClick = {
                                    feed.featuredChart?.let { onOpenPlaylist(it, "chart") }
                                },
                                onActionClick = {
                                    onPlayFeatured(feed.featuredSongs)
                                },
                            )
                            GlassPanel {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
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
                                        accent = Mint300,
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
                    }

                    item {
                        SectionHeader(
                            title = "推荐歌单",
                            trailing = {
                                Text(
                                    text = "为你精选",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Gray500,
                                )
                            },
                        )
                    }
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            items(feed.recommendedSheets, key = { it.id }) { sheet ->
                                PlaylistCard(
                                    sheet = sheet,
                                    onClick = { onOpenPlaylist(sheet, "sheet") },
                                )
                            }
                        }
                    }

                    item {
                        SectionHeader(
                            title = "精选标签",
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
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(feed.recommendedTags.pinned, key = { it.id }) { tag ->
                                val selected = feed.selectedTag?.id == tag.id
                                Surface(
                                    modifier = Modifier
                                        .clip(MaterialTheme.shapes.large)
                                        .clickable { onTagSelected(tag) },
                                    shape = MaterialTheme.shapes.large,
                                    color = if (selected) Aqua500.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.86f),
                                    shadowElevation = if (selected) 2.dp else 0.dp,
                                ) {
                                    Text(
                                        text = if (selected) "• ${tag.title}" else tag.title,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        color = Navy700,
                                        style = MaterialTheme.typography.labelLarge,
                                    )
                                }
                            }
                        }
                    }

                    item {
                        SectionHeader(
                            title = "今日轻听",
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
                            onPlay = { onPlaySong(song, feed.featuredSongs) },
                            onFavorite = { onToggleFavorite(song) },
                        )
                    }
                }
            }
        }
    }
}
