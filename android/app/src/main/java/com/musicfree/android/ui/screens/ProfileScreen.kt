package com.musicfree.android.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.musicfree.android.BuildConfig
import com.musicfree.android.data.model.AudioQuality
import com.musicfree.android.data.model.AudioSource
import com.musicfree.android.ui.components.GlassPanel
import com.musicfree.android.ui.components.MetaBadge
import com.musicfree.android.ui.components.SectionHeader
import com.musicfree.android.ui.theme.Aqua500
import com.musicfree.android.ui.theme.Coral400
import com.musicfree.android.ui.theme.Navy900
import com.musicfree.android.ui.viewmodel.LibraryUiState

@Composable
fun ProfileScreen(
    state: LibraryUiState,
    modifier: Modifier = Modifier,
    onQualityChanged: (AudioQuality) -> Unit,
    onSourceChanged: (AudioSource) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = Color.Transparent,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp),
                ) {
                    Surface(
                        color = Color.Transparent,
                        shadowElevation = 10.dp,
                        shape = MaterialTheme.shapes.extraLarge,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                        ) {
                            Surface(
                                shape = MaterialTheme.shapes.extraLarge,
                                color = Color.Transparent,
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(0.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Surface(
                                            modifier = Modifier.padding(end = 14.dp),
                                            shape = CircleShape,
                                            color = Aqua500,
                                        ) {
                                            Text(
                                                text = "M",
                                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                                                style = MaterialTheme.typography.headlineMedium,
                                                color = Color.White,
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "MusicFree Native",
                                                style = MaterialTheme.typography.headlineMedium,
                                                color = Navy900,
                                                fontWeight = FontWeight.Bold,
                                            )
                                            Text(
                                                text = "Kotlin + Jetpack Compose 原生实现",
                                                style = MaterialTheme.typography.bodyMedium,
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(18.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        MetaBadge(text = "收藏 ${state.favorites.size}")
                                        MetaBadge(text = "列表 ${state.queue.size}")
                                        MetaBadge(text = "历史 ${state.history.size}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            GlassPanel {
                Column(modifier = Modifier.padding(18.dp)) {
                    SectionHeader(title = "播放音质")
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(AudioQuality.entries, key = { it.name }) { quality ->
                            Surface(
                                shape = MaterialTheme.shapes.large,
                                color = if (quality == state.settings.quality) Aqua500.copy(alpha = 0.15f) else Color.White,
                                modifier = Modifier.clickable { onQualityChanged(quality) },
                            ) {
                                Text(
                                    text = quality.title,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
        item {
            GlassPanel {
                Column(modifier = Modifier.padding(18.dp)) {
                    SectionHeader(title = "接口来源")
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(AudioSource.entries, key = { it.name }) { source ->
                            Surface(
                                shape = MaterialTheme.shapes.large,
                                color = if (source == state.settings.source) Coral400.copy(alpha = 0.12f) else Color.White,
                                modifier = Modifier.clickable { onSourceChanged(source) },
                            ) {
                                Text(
                                    text = source.title,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
        item {
            GlassPanel {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "版本 ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.titleMedium)
                    Text(text = "底部导航固定为：首页 / 播放列表 / 收藏 / 我的。")
                    Text(text = "网络接口行为对齐 web 目录下的搜索、榜单、推荐歌单、歌词和播放源逻辑。")
                    Text(text = "默认优先海棠播放源，失败时自动回退落雪。", color = Coral400)
                }
            }
        }
    }
}
