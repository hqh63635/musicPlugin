package com.musicfree.android.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.musicfree.android.core.formatDuration
import com.musicfree.android.core.formatPlayCount
import com.musicfree.android.data.model.PlaylistSheet
import com.musicfree.android.data.model.Song
import com.musicfree.android.player.PlayerUiState
import com.musicfree.android.ui.theme.Aqua500
import com.musicfree.android.ui.theme.Coral400
import com.musicfree.android.ui.theme.Gray500
import com.musicfree.android.ui.theme.Mint50
import com.musicfree.android.ui.theme.Mint100
import com.musicfree.android.ui.theme.Mint300
import com.musicfree.android.ui.theme.Navy700
import com.musicfree.android.ui.theme.Navy900

@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.92f),
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 4.dp,
        shadowElevation = 10.dp,
        border = BorderStroke(1.dp, Mint300.copy(alpha = 0.55f)),
        content = { content() },
    )
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = Navy900,
        )
        trailing?.invoke()
    }
}

@Composable
fun SearchEntryBar(
    modifier: Modifier = Modifier,
    placeholder: String = "搜索歌单、歌曲、歌手",
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.85f),
        shadowElevation = 8.dp,
        tonalElevation = 4.dp,
        border = BorderStroke(1.dp, Mint300.copy(alpha = 0.55f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Aqua500.copy(alpha = 0.22f), Mint300.copy(alpha = 0.66f)),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = Aqua500,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = placeholder,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = Gray500,
            )
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Aqua500,
            ) {
                Text(
                    text = "搜索",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = Mint50,
                )
            }
        }
    }
}

@Composable
fun ActionTile(
    icon: ImageVector,
    title: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.88f),
        shadowElevation = 6.dp,
        tonalElevation = 3.dp,
        border = BorderStroke(1.dp, Mint300.copy(alpha = 0.45f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(accent.copy(alpha = 0.2f), accent.copy(alpha = 0.05f)),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accent,
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = Navy700,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun SongRow(
    song: Song,
    modifier: Modifier = Modifier,
    onPlay: () -> Unit,
    onFavorite: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    GlassPanel(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onPlay)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = song.artwork,
                contentDescription = song.title,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(18.dp)),
                contentScale = ContentScale.Crop,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Navy900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = listOfNotNull(song.artist, song.album).joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                song.duration?.let {
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = formatDuration(it * 1_000L),
                        style = MaterialTheme.typography.labelLarge,
                        color = Aqua500,
                    )
                }
            }
            FilledIconButton(
                onClick = onPlay,
                modifier = Modifier.size(42.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.PlayArrow,
                    contentDescription = "播放",
                )
            }
            if (onFavorite != null) {
                IconButton(onClick = onFavorite) {
                    Icon(
                        imageVector = if (song.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (song.isFavorite) "取消收藏" else "收藏",
                        tint = if (song.isFavorite) Coral400 else Gray500,
                    )
                }
            }
            trailing?.invoke()
        }
    }
}

@Composable
fun PlaylistCard(
    sheet: PlaylistSheet,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .width(208.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = sheet.artwork,
                contentDescription = sheet.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Crop,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = sheet.title,
                style = MaterialTheme.typography.titleMedium,
                color = Navy900,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = sheet.artist ?: "QQ 音乐歌单",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MetaBadge(text = formatPlayCount(sheet.playCount))
                sheet.worksNum?.let { MetaBadge(text = "$it 首") }
            }
        }
    }
}

@Composable
fun MetaBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = Mint100,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            color = Aqua500,
        )
    }
}

@Composable
fun EmptyContent(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    GlassPanel(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Navy900,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500,
            )
        }
    }
}

@Composable
fun MiniPlayerBar(
    state: PlayerUiState,
    modifier: Modifier = Modifier,
    onOpen: () -> Unit,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
) {
    val song = state.currentSong ?: return
    Surface(
        modifier = modifier
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(30.dp),
        color = Mint100,
        tonalElevation = 8.dp,
        shadowElevation = 6.dp,
    ) {
        Column {
            LinearProgressIndicator(
                progress = { state.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = Aqua500,
                trackColor = Mint300,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AsyncImage(
                    model = song.artwork,
                    contentDescription = song.title,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Navy900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                IconButton(onClick = onTogglePlay) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                        contentDescription = if (state.isPlaying) "暂停" else "播放",
                        tint = Aqua500,
                    )
                }
                FilledIconButton(
                    onClick = onNext,
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SkipNext,
                        contentDescription = "下一首",
                    )
                }
            }
        }
    }
}

@Composable
fun HeroCard(
    title: String,
    subtitle: String,
    description: String,
    actionText: String,
    modifier: Modifier = Modifier,
    artwork: String? = null,
    onClick: () -> Unit,
    onActionClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(36.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFEAFBFF), Color(0xFFD6F8F2), Color(0xFFFDFEFF)),
                    ),
                )
                .padding(18.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    MetaBadge(text = subtitle)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Navy900,
                        maxLines = 2,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray500,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = onActionClick,
                        border = BorderStroke(1.dp, Aqua500),
                    ) {
                        Text(
                            text = actionText,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                AsyncImage(
                    model = artwork,
                    contentDescription = title,
                    modifier = Modifier
                        .width(132.dp)
                        .aspectRatio(0.85f)
                        .clip(RoundedCornerShape(28.dp)),
                    contentScale = ContentScale.Crop,
                )
            }
        }
    }
}
