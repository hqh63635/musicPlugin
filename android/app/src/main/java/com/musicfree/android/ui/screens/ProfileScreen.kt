package com.musicfree.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Equalizer
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.musicfree.android.data.model.AudioQuality
import com.musicfree.android.data.model.AudioSource
import com.musicfree.android.ui.theme.Aqua500
import com.musicfree.android.ui.theme.Coral400
import com.musicfree.android.ui.theme.Gold300
import com.musicfree.android.ui.theme.Gray100
import com.musicfree.android.ui.theme.Gray300
import com.musicfree.android.ui.theme.Gray500
import com.musicfree.android.ui.theme.Navy900
import com.musicfree.android.ui.viewmodel.LibraryUiState

private enum class ProfilePicker {
    Quality,
    Source,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: LibraryUiState,
    modifier: Modifier = Modifier,
    onQualityChanged: (AudioQuality) -> Unit,
    onSourceChanged: (AudioSource) -> Unit,
) {
    var activePicker by rememberSaveable { mutableStateOf<ProfilePicker?>(null) }
    val pickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFEAF6F2), Color.White),
                        ),
                    )
                    .padding(top = 20.dp, bottom = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(
                    shape = CircleShape,
                    color = Aqua500,
                    shadowElevation = 8.dp,
                ) {
                    Text(
                        text = "M",
                        modifier = Modifier.padding(horizontal = 26.dp, vertical = 16.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "音乐达人",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Navy900,
                    fontWeight = FontWeight.Black,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ID: mf_2026_8866",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray500,
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    ProfileStat(value = "128", label = "关注")
                    ProfileStat(value = "56", label = "粉丝")
                    ProfileStat(value = "892", label = "获赞")
                }
            }
        }

        item {
            SettingsSectionTitle("播放设置")
        }
        item {
            SettingsGroup {
                SettingsRow(
                    icon = Icons.Outlined.GraphicEq,
                    iconTint = Aqua500,
                    label = "播放音质",
                    value = state.settings.quality.title,
                    onClick = { activePicker = ProfilePicker.Quality },
                )
                SettingsRow(
                    icon = Icons.Outlined.Equalizer,
                    iconTint = Color(0xFF53C7C3),
                    label = "音效设置",
                    value = "均衡器",
                )
                SettingsRow(
                    icon = Icons.Outlined.Favorite,
                    iconTint = Gold300,
                    label = "定时停止播放",
                    value = "关闭",
                )
            }
        }

        item {
            SettingsSectionTitle("数据与存储")
        }
        item {
            SettingsGroup {
                SettingsRow(
                    icon = Icons.Outlined.Storage,
                    iconTint = Color(0xFF6B84EA),
                    label = "缓存管理",
                    value = "256 MB",
                )
                SettingsRow(
                    icon = Icons.Outlined.Inventory2,
                    iconTint = Color(0xFFE58CFF),
                    label = "导入歌单",
                )
                SettingsRow(
                    icon = Icons.Outlined.Schedule,
                    iconTint = Coral400,
                    label = "接口来源",
                    value = state.settings.source.title,
                    onClick = { activePicker = ProfilePicker.Source },
                )
            }
        }

        item {
            SettingsSectionTitle("关于")
        }
    }

    if (activePicker != null) {
        ModalBottomSheet(
            onDismissRequest = { activePicker = null },
            sheetState = pickerSheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        ) {
            when (activePicker) {
                ProfilePicker.Quality -> PickerSheet(
                    title = "播放音质",
                    options = AudioQuality.entries.map { it.title },
                    selected = state.settings.quality.title,
                    onSelect = { selected ->
                        AudioQuality.entries.firstOrNull { it.title == selected }?.let(onQualityChanged)
                        activePicker = null
                    },
                )

                ProfilePicker.Source -> PickerSheet(
                    title = "接口来源",
                    options = AudioSource.entries.map { it.title },
                    selected = state.settings.source.title,
                    onSelect = { selected ->
                        AudioSource.entries.firstOrNull { it.title == selected }?.let(onSourceChanged)
                        activePicker = null
                    },
                )

                null -> Unit
            }
        }
    }
}

@Composable
private fun ProfileStat(
    value: String,
    label: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = Navy900,
            fontWeight = FontWeight.Black,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Gray500,
        )
    }
}

@Composable
private fun SettingsSectionTitle(
    title: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = Gray500,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun SettingsGroup(
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.padding(horizontal = 20.dp),
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 8.dp,
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Column(
        modifier = clickableModifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(60.dp),
                shape = RoundedCornerShape(16.dp),
                color = Gray100,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Text(
                text = label,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                style = MaterialTheme.typography.titleMedium,
                color = Navy900,
                fontWeight = FontWeight.Bold,
            )
            value?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Gray500,
                )
                Spacer(modifier = Modifier.size(8.dp))
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = Gray300,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
            .height(1.dp)
            .background(Gray100),
        )
    }
}

@Composable
private fun PickerSheet(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Navy900,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(12.dp))
        options.forEach { option ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(option) },
                shape = RoundedCornerShape(18.dp),
                color = if (option == selected) Aqua500.copy(alpha = 0.10f) else Color.White,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = option,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (option == selected) Aqua500 else Navy900,
                        fontWeight = if (option == selected) FontWeight.Bold else FontWeight.Medium,
                    )
                    if (option == selected) {
                        Text(
                            text = "当前",
                            style = MaterialTheme.typography.labelLarge,
                            color = Aqua500,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}
