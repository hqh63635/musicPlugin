package com.musicfree.android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.LibraryAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.musicfree.android.data.model.CustomPlaylist
import com.musicfree.android.data.model.PlaylistDetail
import com.musicfree.android.data.model.PlaylistSheet
import com.musicfree.android.ui.components.EmptyContent
import com.musicfree.android.ui.components.GlassPanel
import com.musicfree.android.ui.components.SectionHeader
import com.musicfree.android.ui.theme.Aqua500
import com.musicfree.android.ui.theme.Coral400
import com.musicfree.android.ui.theme.Gray500
import com.musicfree.android.ui.theme.Navy900
import com.musicfree.android.ui.viewmodel.MyPlaylistsUiState
import kotlinx.coroutines.launch

@Composable
fun MyPlaylistsScreen(
    state: MyPlaylistsUiState,
    modifier: Modifier = Modifier,
    onImportPlaylist: suspend (String) -> PlaylistDetail,
    onCreatePlaylist: suspend (String, PlaylistDetail?) -> CustomPlaylist,
    onAddImportedToPlaylist: suspend (String, PlaylistDetail) -> Unit,
    onDeletePlaylist: (String) -> Unit,
    onOpenPlaylist: (PlaylistSheet) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var showImportDialog by rememberSaveable { mutableStateOf(false) }
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var showTargetDialog by rememberSaveable { mutableStateOf(false) }
    var importInput by rememberSaveable { mutableStateOf("") }
    var createName by rememberSaveable { mutableStateOf("") }
    var selectedTargetId by rememberSaveable { mutableStateOf<String?>(null) }
    var importedDetail by remember { mutableStateOf<PlaylistDetail?>(null) }
    var deletingId by rememberSaveable { mutableStateOf<String?>(null) }
    var localError by rememberSaveable { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            GlassPanel {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    SectionHeader(title = "我的歌单")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        PlaylistActionCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.FileDownload,
                            title = "导入歌单",
                            subtitle = "支持 QQ 歌单 URL 或纯 ID",
                            onClick = {
                                localError = null
                                importInput = ""
                                showImportDialog = true
                            },
                        )
                        PlaylistActionCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.LibraryAdd,
                            title = "新建歌单",
                            subtitle = "创建一个空白本地歌单",
                            onClick = {
                                importedDetail = null
                                createName = ""
                                showCreateDialog = true
                            },
                        )
                    }
                    localError?.let { message ->
                        Text(
                            text = message,
                            color = Coral400,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }

        if (state.playlists.isEmpty()) {
            item {
                EmptyContent(
                    title = "还没有我的歌单",
                    subtitle = "可以先新建一个空歌单，或者像 web 端一样直接粘贴 QQ 歌单链接或 ID 导入。",
                )
            }
        } else {
            items(state.playlists, key = { it.id }) { playlist ->
                CustomPlaylistRow(
                    playlist = playlist,
                    onClick = { onOpenPlaylist(playlist.toSheet()) },
                    onDelete = { deletingId = playlist.id },
                )
            }
        }
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("导入歌单") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("请输入歌单 URL 或 ID")
                    OutlinedTextField(
                        value = importInput,
                        onValueChange = { importInput = it },
                        singleLine = true,
                        placeholder = { Text("支持 QQ 音乐分享链接或纯数字 ID") },
                    )
                    Text(
                        text = "1. 支持 QQ 音乐 App 分享链接\n2. 支持 H5 歌单链接\n3. 也支持直接输入纯数字歌单 ID",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (importInput.trim().isBlank()) {
                            localError = "请输入歌单 URL 或 ID"
                            return@TextButton
                        }
                        scope.launch {
                            runCatching {
                                onImportPlaylist(importInput.trim())
                            }.onSuccess { detail ->
                                importedDetail = detail
                                showImportDialog = false
                                if (state.playlists.isEmpty()) {
                                    createName = detail.sheet.title
                                    showCreateDialog = true
                                } else {
                                    selectedTargetId = state.playlists.firstOrNull()?.id
                                    showTargetDialog = true
                                }
                                localError = null
                            }.onFailure { throwable ->
                                localError = throwable.message ?: "导入失败，请检查链接或 ID 是否有效"
                            }
                        }
                    },
                ) {
                    Text("导入")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("取消")
                }
            },
        )
    }

    if (showTargetDialog && importedDetail != null) {
        AlertDialog(
            onDismissRequest = { showTargetDialog = false },
            title = { Text("选择目标歌单") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("把导入的歌曲加入已有歌单，或新建一个歌单。")
                    TextButton(
                        onClick = {
                            createName = importedDetail?.sheet?.title.orEmpty()
                            showTargetDialog = false
                            showCreateDialog = true
                        },
                    ) {
                        Text("新建歌单")
                    }
                    state.playlists.forEach { playlist ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTargetId = playlist.id },
                            shape = MaterialTheme.shapes.large,
                            color = if (selectedTargetId == playlist.id) {
                                Aqua500.copy(alpha = 0.12f)
                            } else {
                                Color.White.copy(alpha = 0.92f)
                            },
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = playlist.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Navy900,
                                )
                                Text(
                                    text = "${playlist.trackCount} 首",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Gray500,
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val targetId = selectedTargetId ?: return@TextButton
                        val detail = importedDetail ?: return@TextButton
                        scope.launch {
                            runCatching {
                                onAddImportedToPlaylist(targetId, detail)
                            }.onSuccess {
                                importedDetail = null
                                selectedTargetId = null
                                showTargetDialog = false
                                localError = null
                            }.onFailure { throwable ->
                                localError = throwable.message ?: "加入歌单失败"
                            }
                        }
                    },
                ) {
                    Text("添加")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTargetDialog = false }) {
                    Text("取消")
                }
            },
        )
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("创建歌单") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("请输入歌单名称")
                    OutlinedTextField(
                        value = createName,
                        onValueChange = { createName = it.take(20) },
                        singleLine = true,
                        placeholder = { Text("例如：今晚循环") },
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (createName.trim().isBlank()) {
                            localError = "请输入歌单名称"
                            return@TextButton
                        }
                        val pendingImport = importedDetail
                        scope.launch {
                            runCatching {
                                onCreatePlaylist(createName.trim(), pendingImport)
                            }.onSuccess { playlist ->
                                importedDetail = null
                                createName = ""
                                showCreateDialog = false
                                localError = null
                                onOpenPlaylist(playlist.toSheet())
                            }.onFailure { throwable ->
                                localError = throwable.message ?: "创建歌单失败"
                            }
                        }
                    },
                ) {
                    Text("创建")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("取消")
                }
            },
        )
    }

    if (deletingId != null) {
        AlertDialog(
            onDismissRequest = { deletingId = null },
            title = { Text("删除歌单") },
            text = { Text("删除后歌单内歌曲无法恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeletePlaylist(deletingId!!)
                        deletingId = null
                    },
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingId = null }) {
                    Text("取消")
                }
            },
        )
    }
}

@Composable
private fun PlaylistActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 6.dp,
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Aqua500,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Navy900,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Gray500,
            )
        }
    }
}

@Composable
private fun CustomPlaylistRow(
    playlist: CustomPlaylist,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    GlassPanel {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(54.dp),
                shape = RoundedCornerShape(18.dp),
                color = Aqua500.copy(alpha = 0.14f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.LibraryAdd,
                        contentDescription = null,
                        tint = Aqua500,
                    )
                }
            }
            Spacer(modifier = Modifier.padding(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Navy900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.padding(2.dp))
                Text(
                    text = "${playlist.trackCount} 首",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray500,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "删除歌单",
                    tint = Gray500,
                )
            }
        }
    }
}

private fun CustomPlaylist.toSheet(): PlaylistSheet {
    return PlaylistSheet(
        id = id,
        title = name,
        artwork = artwork ?: songs.firstOrNull()?.artwork,
        description = description,
        artist = "我的歌单",
        createTime = createTime.toString(),
        worksNum = trackCount,
    )
}
