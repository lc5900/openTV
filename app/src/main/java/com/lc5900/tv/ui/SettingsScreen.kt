package com.lc5900.tv.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lc5900.tv.data.TvChannel
import com.lc5900.tv.ui.components.GradientButton
import com.lc5900.tv.ui.components.OpenTvBackground
import com.lc5900.tv.ui.components.OpenTvColors

private data class SourceEditorState(
    val channelId: Int,
    val sourceIndex: Int?,
    val value: String,
    val canDelete: Boolean = false,
)

private data class ChannelEditorState(
    val name: String = "",
    val group: String = "",
    val url: String = "",
)

@Composable
fun SettingsScreen(
    uiState: ChannelUiState,
    onBack: () -> Unit,
    onSubscriptionUrlChange: (String) -> Unit,
    onSync: () -> Unit,
    onRestoreBuiltIn: () -> Unit,
    onAddChannel: (String, String, String) -> Unit,
    onAddSource: (Int, String) -> Unit,
    onUpdateSource: (Int, Int, String) -> Unit,
    onRemoveSource: (Int, Int) -> Unit,
    onMessageShown: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var editor by remember { mutableStateOf<SourceEditorState?>(null) }
    var confirmRestore by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var channelEditor by remember { mutableStateOf<ChannelEditorState?>(null) }

    LaunchedEffect(uiState.settingsMessage) {
        uiState.settingsMessage?.let { message ->
            if (message.startsWith("订阅成功")) successMessage = message else snackbarHostState.showSnackbar(message)
            onMessageShown()
        }
    }

    OpenTvBackground(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize()
                .align(Alignment.TopCenter)
                .statusBarsPadding(),
        ) {
            SettingsTopBar(onBack)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    SubscriptionCard(
                        subscriptionUrl = uiState.subscriptionUrl,
                        isSyncing = uiState.isSyncing,
                        onUrlChange = onSubscriptionUrlChange,
                        onSync = onSync,
                    )
                }
                item { StatsCard(uiState.channels) }
                item { SubscriptionAddressCard(uiState.subscriptionUrl) }
                item {
                    SettingsActionCard(
                        icon = Icons.Rounded.Restore,
                        iconColor = Color(0xFFFFB0C5),
                        title = "恢复内置频道",
                        subtitle = "清除订阅与所有自定义内容，恢复内置频道",
                        onClick = { confirmRestore = true },
                    )
                }
                item {
                    SettingsActionCard(
                        icon = Icons.Rounded.PlayCircle,
                        iconColor = Color(0xFFAFC6FF),
                        title = "播放源管理",
                        subtitle = "管理频道的各线路播放地址",
                    )
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("频道线路", style = MaterialTheme.typography.titleLarge)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("共 ${uiState.channels.size} 个频道", color = OpenTvColors.TextSecondary)
                            Surface(
                                modifier = Modifier
                                    .padding(start = 10.dp)
                                    .clickable { channelEditor = ChannelEditorState() },
                                shape = RoundedCornerShape(10.dp),
                                color = OpenTvColors.Purple,
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(17.dp))
                                    Text("新增频道", modifier = Modifier.padding(start = 4.dp), style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
                items(uiState.channels, key = TvChannel::id) { channel ->
                    ChannelSourcesCard(
                        channel = channel,
                        onAdd = { editor = SourceEditorState(channel.id, null, "") },
                        onEdit = { index, url ->
                            editor = SourceEditorState(
                                channelId = channel.id,
                                sourceIndex = index,
                                value = url,
                                canDelete = channel.urls.size > 1,
                            )
                        },
                        onRemove = { index -> onRemoveSource(channel.id, index) },
                    )
                }
                item {
                    SettingsActionCard(
                        icon = Icons.Rounded.Info,
                        iconColor = Color(0xFF9B7BFF),
                        title = "关于 OpenTV",
                        subtitle = "当前版本 2.0.0 · Kotlin / Compose / Media3",
                    )
                }
                item { Spacer(Modifier.height(18.dp)) }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp),
        )
    }

    editor?.let { state ->
        SourceEditorDialog(
            state = state,
            onDismiss = { editor = null },
            onConfirm = { value ->
                editor = null
                if (state.sourceIndex == null) onAddSource(state.channelId, value)
                else onUpdateSource(state.channelId, state.sourceIndex, value)
            },
            onDelete = state.sourceIndex?.takeIf { state.canDelete }?.let { index ->
                { editor = null; onRemoveSource(state.channelId, index) }
            },
        )
    }
    if (confirmRestore) {
        RestoreDialog(
            onDismiss = { confirmRestore = false },
            onConfirm = { confirmRestore = false; onRestoreBuiltIn() },
        )
    }
    successMessage?.let { message ->
        SuccessDialog(message) { successMessage = null }
    }
    channelEditor?.let {
        AddChannelDialog(
            onDismiss = { channelEditor = null },
            onConfirm = { name, group, url ->
                channelEditor = null
                onAddChannel(name, group, url)
            },
        )
    }
}

@Composable
private fun SettingsTopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
    ) {
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "返回")
        }
        Text(
            "设置与订阅",
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun SubscriptionCard(
    subscriptionUrl: String,
    isSyncing: Boolean,
    onUrlChange: (String) -> Unit,
    onSync: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        color = OpenTvColors.Surface.copy(alpha = 0.96f),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Language, contentDescription = null, tint = OpenTvColors.Blue)
                Text("IPTV 网络订阅", modifier = Modifier.padding(start = 10.dp), fontWeight = FontWeight.Bold)
            }
            OutlinedTextField(
                value = subscriptionUrl,
                onValueChange = onUrlChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                placeholder = { Text("https://example.com/iptv/playlist.m3u") },
                singleLine = true,
                enabled = !isSyncing,
                shape = RoundedCornerShape(14.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = OpenTvColors.TextPrimary,
                    unfocusedTextColor = OpenTvColors.TextPrimary,
                    disabledTextColor = OpenTvColors.TextSecondary,
                    cursorColor = OpenTvColors.Cyan,
                    focusedPlaceholderColor = OpenTvColors.TextSecondary,
                    unfocusedPlaceholderColor = OpenTvColors.TextSecondary,
                    focusedContainerColor = OpenTvColors.SurfaceHigh,
                    unfocusedContainerColor = OpenTvColors.SurfaceHigh,
                    focusedIndicatorColor = OpenTvColors.Purple,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
            )
            GradientButton(
                text = if (isSyncing) "正在同步…" else "同步订阅",
                onClick = onSync,
                modifier = Modifier.padding(top = 12.dp),
                enabled = !isSyncing && subscriptionUrl.isNotBlank(),
            )
            if (isSyncing) {
                Row(
                    modifier = Modifier.padding(top = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("正在同步…", style = MaterialTheme.typography.bodySmall, color = OpenTvColors.TextSecondary)
                    LinearProgressIndicator(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                        color = OpenTvColors.Purple,
                        trackColor = OpenTvColors.SurfaceHigh,
                    )
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                }
            }
        }
    }
}

@Composable
private fun StatsCard(channels: List<TvChannel>) {
    val groupCount = channels.map { it.group.ifBlank { "其他" } }.distinct().size
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        color = OpenTvColors.Surface,
    ) {
        Row(modifier = Modifier.padding(vertical = 18.dp)) {
            StatItem("${channels.size}", "频道总数", Color(0xFFB8C7FF), Modifier.weight(1f))
            Box(Modifier.size(width = 1.dp, height = 54.dp).background(OpenTvColors.Outline))
            StatItem("$groupCount", "分组数量", Color(0xFFF1B1FF), Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatItem(value: String, label: String, color: Color, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.bodySmall, color = OpenTvColors.TextSecondary)
    }
}

@Composable
private fun SubscriptionAddressCard(url: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(17.dp),
        color = OpenTvColors.Surface,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("订阅地址", fontWeight = FontWeight.SemiBold)
            Text(
                url.ifBlank { "尚未配置网络订阅" },
                modifier = Modifier.padding(top = 5.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                color = OpenTvColors.TextSecondary,
            )
        }
    }
}

@Composable
private fun SettingsActionCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(17.dp),
        color = OpenTvColors.Surface,
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(38.dp), shape = RoundedCornerShape(12.dp), color = iconColor.copy(alpha = 0.12f)) {
                Icon(icon, contentDescription = null, modifier = Modifier.padding(9.dp), tint = iconColor)
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = OpenTvColors.TextSecondary)
            }
            if (onClick != null) {
                Icon(Icons.AutoMirrored.Rounded.ArrowForwardIos, contentDescription = null, tint = OpenTvColors.TextSecondary)
            }
        }
    }
}

@Composable
private fun ChannelSourcesCard(
    channel: TvChannel,
    onAdd: () -> Unit,
    onEdit: (Int, String) -> Unit,
    onRemove: (Int) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(17.dp),
        color = OpenTvColors.Surface,
    ) {
        Column {
            Row(
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(channel.name, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${channel.group.ifBlank { "未分组" }} · ${channel.urls.size} 条线路",
                        style = MaterialTheme.typography.bodySmall,
                        color = OpenTvColors.TextSecondary,
                    )
                }
                IconButton(onClick = onAdd) { Icon(Icons.Rounded.Add, "为 ${channel.name} 添加播放源") }
            }
            channel.urls.forEachIndexed { index, url ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (index == 0) Color(0xFF151D2A) else Color.Transparent)
                        .clickable { onEdit(index, url) }
                        .padding(start = 16.dp, top = 10.dp, end = 6.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(modifier = Modifier.size(28.dp), shape = CircleShape, color = OpenTvColors.SurfaceHigh) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("${index + 1}", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    Column(modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
                        Text("线路 ${index + 1}${if (index == 0) "（主线路）" else "（备用）"}", style = MaterialTheme.typography.bodyMedium)
                        Text(displayUrl(url), maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall, color = OpenTvColors.TextSecondary)
                    }
                    IconButton(onClick = { onEdit(index, url) }) { Icon(Icons.Rounded.Edit, "编辑线路 ${index + 1}") }
                    IconButton(onClick = { onRemove(index) }, enabled = channel.urls.size > 1) {
                        Icon(Icons.Rounded.Delete, "删除线路 ${index + 1}", tint = if (channel.urls.size > 1) OpenTvColors.Red else OpenTvColors.Outline)
                    }
                }
            }
        }
    }
}

@Composable
private fun SourceEditorDialog(
    state: SourceEditorState,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var value by remember(state) { mutableStateOf(state.value) }
    val protocol = Uri.parse(value).scheme?.uppercase() ?: "自动（推荐）"
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = OpenTvColors.BackgroundElevated,
        shape = RoundedCornerShape(24.dp),
        title = { Text(if (state.sourceIndex == null) "添加播放源" else "编辑播放源") },
        text = {
            Column {
                Text("播放地址", style = MaterialTheme.typography.labelMedium, color = OpenTvColors.TextSecondary)
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    placeholder = { Text("请输入 HTTP、HTTPS 或 RTSP 地址") },
                    minLines = 2,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OpenTvColors.TextPrimary,
                        unfocusedTextColor = OpenTvColors.TextPrimary,
                        cursorColor = OpenTvColors.Cyan,
                        focusedPlaceholderColor = OpenTvColors.TextSecondary,
                        unfocusedPlaceholderColor = OpenTvColors.TextSecondary,
                        focusedBorderColor = OpenTvColors.Purple,
                        unfocusedBorderColor = OpenTvColors.Outline,
                    ),
                )
                Text("协议", modifier = Modifier.padding(top = 16.dp), style = MaterialTheme.typography.labelMedium, color = OpenTvColors.TextSecondary)
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = OpenTvColors.SurfaceHigh,
                ) { Text(protocol, modifier = Modifier.padding(14.dp)) }
                Text("支持格式：HTTP(S) / RTSP", modifier = Modifier.padding(top = 14.dp), style = MaterialTheme.typography.bodySmall, color = OpenTvColors.TextSecondary)
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (onDelete != null) TextButton(onClick = onDelete) { Text("删除", color = OpenTvColors.Red) }
                TextButton(onClick = { onConfirm(value) }, enabled = value.isNotBlank()) { Text("保存") }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

@Composable
private fun AddChannelDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var group by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = OpenTvColors.TextPrimary,
        unfocusedTextColor = OpenTvColors.TextPrimary,
        cursorColor = OpenTvColors.Cyan,
        focusedPlaceholderColor = OpenTvColors.TextSecondary,
        unfocusedPlaceholderColor = OpenTvColors.TextSecondary,
        focusedBorderColor = OpenTvColors.Purple,
        unfocusedBorderColor = OpenTvColors.Outline,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = OpenTvColors.BackgroundElevated,
        shape = RoundedCornerShape(24.dp),
        title = { Text("新增频道") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("频道名称") },
                    placeholder = { Text("例如：新闻频道") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors,
                )
                OutlinedTextField(
                    value = group,
                    onValueChange = { group = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("频道分组（可选）") },
                    placeholder = { Text("例如：新闻") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors,
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("首条播放地址") },
                    placeholder = { Text("https://… 或 rtsp://…") },
                    minLines = 2,
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, group, url) },
                enabled = name.isNotBlank() && url.isNotBlank(),
            ) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

@Composable
private fun RestoreDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = OpenTvColors.BackgroundElevated,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Surface(modifier = Modifier.size(62.dp), shape = CircleShape, color = OpenTvColors.Amber.copy(alpha = 0.14f)) {
                Icon(Icons.Rounded.Restore, contentDescription = null, modifier = Modifier.padding(15.dp), tint = OpenTvColors.Amber)
            }
        },
        title = { Text("恢复内置频道") },
        text = { Text("这将清除当前订阅地址和所有自定义内容，并恢复应用内置的公开频道。此操作不可撤销。") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("确定") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消", color = OpenTvColors.TextSecondary) } },
    )
}

@Composable
private fun SuccessDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = OpenTvColors.BackgroundElevated,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Surface(modifier = Modifier.size(70.dp), shape = CircleShape, color = OpenTvColors.Green.copy(alpha = 0.14f)) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.padding(14.dp), tint = OpenTvColors.Green)
            }
        },
        title = { Text("同步成功") },
        text = { Text(message, color = OpenTvColors.TextSecondary) },
        confirmButton = { TextButton(onClick = onDismiss) { Text("确定") } },
    )
}

private fun displayUrl(url: String): String {
    val uri = Uri.parse(url)
    return buildString {
        append(uri.host ?: url)
        if (!uri.path.isNullOrBlank()) append(uri.path)
    }
}
