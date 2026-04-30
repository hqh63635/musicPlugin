package com.musicfree.android.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.musicfree.android.core.formatDuration
import com.musicfree.android.player.PlayerUiState
import com.musicfree.android.ui.components.GlassPanel
import com.musicfree.android.ui.theme.Aqua500
import com.musicfree.android.ui.theme.Coral400
import com.musicfree.android.ui.theme.Gray500
import com.musicfree.android.ui.theme.Mint50
import com.musicfree.android.ui.theme.Mint100
import com.musicfree.android.ui.theme.Mint300
import com.musicfree.android.ui.theme.Navy700
import com.musicfree.android.ui.theme.Navy900
import kotlin.math.max

private enum class PlayerPage {
    Cover,
    Lyric,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    state: PlayerUiState,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onSeek: (Float) -> Unit,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onPlayQueueSong: (Int) -> Unit,
    onRemoveQueueSong: (Int) -> Unit,
) {
    val song = state.currentSong
    var page by rememberSaveable { mutableStateOf(PlayerPage.Cover) }
    var showQueueSheet by rememberSaveable { mutableStateOf(false) }
    val lyricListState = rememberLazyListState()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(state.currentLyricIndex, page) {
        if (page == PlayerPage.Lyric && state.currentLyricIndex >= 0) {
            lyricListState.animateScrollToItem(max(0, state.currentLyricIndex - 3))
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF8FC7FF), Color(0xFFCDE7FF), Color(0xFFF8FAFF)),
                ),
            ),
    ) {
        if (song == null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "还没有正在播放的歌曲",
                    style = MaterialTheme.typography.titleLarge,
                    color = Navy900,
                )
            }
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 14.dp),
        ) {
            PlayerTopBar(
                currentPage = page,
                isFavorite = song.isFavorite,
                onBack = onBack,
                onSelectPage = { page = it },
            )

            Spacer(modifier = Modifier.height(12.dp))

            when (page) {
                PlayerPage.Cover -> CoverPlayerPage(
                    state = state,
                    modifier = Modifier.weight(1f),
                )
                PlayerPage.Lyric -> LyricPlayerPage(
                    state = state,
                    listState = lyricListState,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            SongInfoBlock(
                title = song.title,
                artist = song.artist,
                isFavorite = song.isFavorite,
                quality = state.quality.title,
                source = state.source.title,
                queueCount = state.queue.size,
            )

            Spacer(modifier = Modifier.height(14.dp))

            AiryProgressBar(
                progress = state.progress,
                modifier = Modifier.fillMaxWidth(),
                onSeek = onSeek,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = formatDuration(state.positionMs),
                    style = MaterialTheme.typography.labelLarge,
                    color = Navy700.copy(alpha = 0.9f),
                )
                Text(
                    text = formatDuration(state.durationMs),
                    style = MaterialTheme.typography.labelLarge,
                    color = Navy700.copy(alpha = 0.9f),
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            PlayerActionStrip(
                currentPage = page,
                onShowLyrics = { page = PlayerPage.Lyric },
                onShowCover = { page = PlayerPage.Cover },
                onOpenQueue = { showQueueSheet = true },
            )

            Spacer(modifier = Modifier.height(14.dp))

            MainPlayerControls(
                isPlaying = state.isPlaying,
                onPrevious = onPrevious,
                onTogglePlay = onTogglePlay,
                onNext = onNext,
                onOpenQueue = { showQueueSheet = true },
            )
        }

        if (showQueueSheet) {
            ModalBottomSheet(
                onDismissRequest = { showQueueSheet = false },
                sheetState = bottomSheetState,
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
                containerColor = Color.White.copy(alpha = 0.96f),
                tonalElevation = 12.dp,
            ) {
                QueueSheet(
                    state = state,
                    onPlayQueueSong = { index ->
                        onPlayQueueSong(index)
                        showQueueSheet = false
                    },
                    onRemoveQueueSong = onRemoveQueueSong,
                )
            }
        }
    }
}

@Composable
private fun PlayerTopBar(
    currentPage: PlayerPage,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onSelectPage: (PlayerPage) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "返回",
                tint = Navy700,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        PageIndicator(
            currentPage = currentPage,
            onSelectPage = onSelectPage,
        )
        Spacer(modifier = Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "收藏状态",
                    tint = if (isFavorite) Coral400 else Navy700.copy(alpha = 0.85f),
                )
            }
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = "分享",
                    tint = Navy700.copy(alpha = 0.85f),
                )
            }
        }
    }
}

@Composable
private fun PageIndicator(
    currentPage: PlayerPage,
    onSelectPage: (PlayerPage) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        listOf(PlayerPage.Cover, PlayerPage.Lyric).forEach { page ->
            val selected = currentPage == page
            Surface(
                modifier = Modifier
                    .width(if (selected) 22.dp else 8.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .clickable { onSelectPage(page) },
                color = if (selected) Navy700 else Color.White.copy(alpha = 0.55f),
            ) {}
        }
    }
}

@Composable
private fun CoverPlayerPage(
    state: PlayerUiState,
    modifier: Modifier = Modifier,
) {
    val song = state.currentSong ?: return
    val armRotation by animateFloatAsState(
        targetValue = if (state.isPlaying) 24f else 8f,
        label = "tonearm_rotation",
    )
    val vinylRotation by rememberInfiniteTransition(label = "vinyl_rotation").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "vinyl_rotation_value",
    )

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        GlassPanel(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 400.dp, max = 470.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.White.copy(alpha = 0.52f), Color.White.copy(alpha = 0.16f)),
                        ),
                        cornerRadius = CornerRadius(36.dp.toPx(), 36.dp.toPx()),
                        style = Stroke(width = 3.dp.toPx()),
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(280.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    VinylDisc(
                        artwork = song.artwork,
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(if (state.isPlaying) vinylRotation else 0f),
                    )
                }

                ToneArm(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 20.dp, top = 10.dp)
                        .height(300.dp),
                    rotation = armRotation,
                )
            }
        }
    }
}

@Composable
private fun VinylDisc(
    artwork: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2.15f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFE7F4FF), Color(0xFF8AB7F8), Color(0xFF5B94E9)),
                    center = center,
                    radius = radius * 1.1f,
                ),
                radius = radius,
                center = center,
            )
            repeat(18) { index ->
                drawCircle(
                    color = Color.White.copy(alpha = 0.06f + index * 0.003f),
                    radius = radius - index * 7.dp.toPx(),
                    center = center,
                    style = Stroke(width = 1.6.dp.toPx()),
                )
            }
            drawCircle(
                color = Color.White.copy(alpha = 0.3f),
                radius = radius * 0.22f,
                center = center,
            )
        }
        AsyncImage(
            model = artwork,
            contentDescription = "专辑封面",
            modifier = Modifier
                .size(138.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
        Canvas(modifier = Modifier.size(24.dp)) {
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = size.minDimension / 2f,
            )
            drawCircle(
                color = Color(0xFFB4D6FF),
                radius = size.minDimension / 6f,
            )
        }
    }
}

@Composable
private fun ToneArm(
    modifier: Modifier = Modifier,
    rotation: Float,
) {
    Box(
        modifier = modifier.width(90.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(30.dp)
                .clip(CircleShape)
                .background(Color(0xFFF5F0E9)),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 18.dp, end = 13.dp)
                .width(4.dp)
                .fillMaxHeight()
                .rotate(rotation)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF6D727C), Color(0xFFB5BAC2)),
                    ),
                    shape = RoundedCornerShape(999.dp),
                ),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = Path().apply {
                    moveTo(size.width / 2f, size.height * 0.56f)
                    quadraticTo(
                        size.width * 1.4f,
                        size.height * 0.72f,
                        size.width * 0.92f,
                        size.height * 0.94f,
                    )
                }
                drawPath(
                    path = path,
                    color = Color(0xFF7A808B),
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
                )
                drawCircle(
                    color = Color(0xFFF7F2EA),
                    radius = 12.dp.toPx(),
                    center = Offset(size.width * 0.56f, size.height * 0.12f),
                )
                drawCircle(
                    color = Color(0xFFF7F2EA),
                    radius = 10.dp.toPx(),
                    center = Offset(size.width * 0.88f, size.height * 0.93f),
                )
            }
        }
    }
}

@Composable
private fun LyricPlayerPage(
    state: PlayerUiState,
    listState: androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier = Modifier,
) {
    val song = state.currentSong ?: return
    GlassPanel(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 20.dp),
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.headlineMedium,
                color = Navy900,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${song.artist} · 歌词模式",
                style = MaterialTheme.typography.bodyMedium,
                color = Navy700.copy(alpha = 0.75f),
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (state.lyrics.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "暂无歌词",
                        style = MaterialTheme.typography.titleLarge,
                        color = Gray500,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    itemsIndexed(state.lyrics, key = { index, line -> "${line.timeMs}-$index" }) { index, line ->
                        val highlighted = index == state.currentLyricIndex
                        Text(
                            text = line.text,
                            style = if (highlighted) {
                                MaterialTheme.typography.headlineMedium
                            } else {
                                MaterialTheme.typography.bodyLarge
                            },
                            color = if (highlighted) Navy900 else Navy700.copy(alpha = 0.38f),
                            fontWeight = if (highlighted) FontWeight.Bold else FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SongInfoBlock(
    title: String,
    artist: String,
    isFavorite: Boolean,
    quality: String,
    source: String,
    queueCount: Int,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Navy900,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = artist,
                    style = MaterialTheme.typography.titleMedium,
                    color = Navy700.copy(alpha = 0.78f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "收藏状态",
                tint = if (isFavorite) Coral400 else Navy700.copy(alpha = 0.82f),
                modifier = Modifier.size(34.dp),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PlayerChip(text = quality)
            PlayerChip(text = source)
            PlayerChip(text = "队列 $queueCount")
        }
    }
}

@Composable
private fun PlayerChip(
    text: String,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color.White.copy(alpha = 0.72f),
        shadowElevation = 2.dp,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelLarge,
            color = Navy700,
        )
    }
}

@Composable
private fun AiryProgressBar(
    progress: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .height(26.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val ratio = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                    onSeek(ratio)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val ratio = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        onSeek(ratio)
                    },
                    onDrag = { change, _ ->
                        val ratio = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                        onSeek(ratio)
                        change.consume()
                    },
                )
            },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val trackY = size.height / 2f
            val stroke = 4.dp.toPx()
            drawLine(
                color = Color.Black.copy(alpha = 0.12f),
                start = Offset(0f, trackY),
                end = Offset(size.width, trackY),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = Navy900.copy(alpha = 0.88f),
                start = Offset(0f, trackY),
                end = Offset(size.width * progress.coerceIn(0f, 1f), trackY),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
            val thumbX = size.width * progress.coerceIn(0f, 1f)
            drawCircle(
                color = Color.White,
                radius = 8.dp.toPx(),
                center = Offset(thumbX, trackY),
            )
            drawCircle(
                color = Navy900,
                radius = 5.dp.toPx(),
                center = Offset(thumbX, trackY),
            )
        }
    }
}

@Composable
private fun PlayerActionStrip(
    currentPage: PlayerPage,
    onShowLyrics: () -> Unit,
    onShowCover: () -> Unit,
    onOpenQueue: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SmallActionPill(
                active = currentPage == PlayerPage.Cover,
                icon = Icons.Outlined.GraphicEq,
                label = "唱片",
                onClick = onShowCover,
            )
            SmallActionPill(
                active = currentPage == PlayerPage.Lyric,
                icon = Icons.Outlined.PlayArrow,
                label = "歌词",
                onClick = onShowLyrics,
            )
        }
        SmallActionPill(
            active = false,
            icon = Icons.AutoMirrored.Outlined.QueueMusic,
            label = "列表",
            onClick = onOpenQueue,
        )
    }
}

@Composable
private fun SmallActionPill(
    active: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = if (active) Color.White.copy(alpha = 0.88f) else Color.White.copy(alpha = 0.55f),
        shadowElevation = if (active) 4.dp else 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(16.dp),
                tint = if (active) Navy900 else Navy700.copy(alpha = 0.72f),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (active) Navy900 else Navy700.copy(alpha = 0.72f),
            )
        }
    }
}

@Composable
private fun MainPlayerControls(
    isPlaying: Boolean,
    onPrevious: () -> Unit,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onOpenQueue: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onOpenQueue) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.QueueMusic,
                contentDescription = "播放列表",
                tint = Navy700,
                modifier = Modifier.size(28.dp),
            )
        }
        IconButton(onClick = onPrevious) {
            Icon(
                imageVector = Icons.Outlined.SkipPrevious,
                contentDescription = "上一首",
                tint = Navy900,
                modifier = Modifier.size(34.dp),
            )
        }
        FilledIconButton(
            onClick = onTogglePlay,
            modifier = Modifier.size(86.dp),
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                contentDescription = if (isPlaying) "暂停" else "播放",
                modifier = Modifier.size(42.dp),
            )
        }
        IconButton(onClick = onNext) {
            Icon(
                imageVector = Icons.Outlined.SkipNext,
                contentDescription = "下一首",
                tint = Navy900,
                modifier = Modifier.size(34.dp),
            )
        }
        Spacer(modifier = Modifier.width(48.dp))
    }
}

@Composable
private fun QueueSheet(
    state: PlayerUiState,
    onPlayQueueSong: (Int) -> Unit,
    onRemoveQueueSong: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            QueueHeadline(title = "正在播放", value = state.queue.size.toString())
            QueueHeadline(title = "已播歌曲", value = (state.currentIndex + 1).coerceAtLeast(0).toString())
            QueueHeadline(
                title = "剩余歌曲",
                value = (state.queue.size - state.currentIndex - 1).coerceAtLeast(0).toString(),
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFFF2F7FB),
        ) {
            Text(
                text = "当前接口：${state.source.title} · 音质：${state.quality.title}",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = Navy700,
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 520.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            itemsIndexed(state.queue, key = { _, song -> song.identity }) { index, song ->
                val active = index == state.currentIndex
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPlayQueueSong(index) },
                    shape = RoundedCornerShape(20.dp),
                    color = if (active) Color(0xFFEAFBF5) else Color.Transparent,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AsyncImage(
                            model = song.artwork,
                            contentDescription = song.title,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (active) Color(0xFF20B97A) else Navy900,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = song.artist,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Gray500,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Icon(
                            imageVector = if (active) Icons.Outlined.GraphicEq else Icons.Outlined.FavoriteBorder,
                            contentDescription = null,
                            tint = if (active) Color(0xFF20B97A) else Gray500,
                        )
                        IconButton(onClick = { onRemoveQueueSong(index) }) {
                            Text(
                                text = "×",
                                style = MaterialTheme.typography.titleLarge,
                                color = Gray500,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QueueHeadline(
    title: String,
    value: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = Gray500,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = Navy900,
            fontWeight = FontWeight.Bold,
        )
    }
}
