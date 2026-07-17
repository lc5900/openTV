package com.lc5900.tv.ui

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lc5900.tv.data.TvChannel

private data class SourceEditorState(
    val channelId: Int,
    val sourceIndex: Int?,
    val value: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: ChannelUiState,
    onBack: () -> Unit,
    onSubscriptionUrlChange: (String) -> Unit,
    onSync: () -> Unit,
    onRestoreBuiltIn: () -> Unit,
    onAddSource: (Int, String) -> Unit,
    onUpdateSource: (Int, Int, String) -> Unit,
    onRemoveSource: (Int, Int) -> Unit,
    onMessageShown: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var editor by remember { mutableStateOf<SourceEditorState?>(null) }
    var confirmRestore by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.settingsMessage) {
        uiState.settingsMessage?.let {
            snackbarHostState.showSnackbar(it)
            onMessageShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("频道与订阅设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SubscriptionCard(
                    subscriptionUrl = uiState.subscriptionUrl,
                    isSyncing = uiState.isSyncing,
                    onUrlChange = onSubscriptionUrlChange,
                    onSync = onSync,
                    onRestore = { confirmRestore = true },
                )
            }
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                    Text(
                        text = "播放源管理",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "每个频道至少保留一条线路，修改会立即保存。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(uiState.channels, key = TvChannel::id) { channel ->
                ChannelSourcesCard(
                    channel = channel,
                    onAdd = { editor = SourceEditorState(channel.id, null, "") },
                    onEdit = { index, url -> editor = SourceEditorState(channel.id, index, url) },
                    onRemove = { index -> onRemoveSource(channel.id, index) },
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    editor?.let { state ->
        SourceEditorDialog(
            state = state,
            onDismiss = { editor = null },
            onConfirm = { value ->
                editor = null
                if (state.sourceIndex == null) {
                    onAddSource(state.channelId, value)
                } else {
                    onUpdateSource(state.channelId, state.sourceIndex, value)
                }
            },
        )
    }

    if (confirmRestore) {
        AlertDialog(
            onDismissRequest = { confirmRestore = false },
            title = { Text("恢复内置频道？") },
            text = { Text("当前订阅和手工修改的播放源将被清除。") },
            confirmButton = {
                TextButton(onClick = {
                    confirmRestore = false
                    onRestoreBuiltIn()
                }) { Text("恢复") }
            },
            dismissButton = {
                TextButton(onClick = { confirmRestore = false }) { Text("取消") }
            },
        )
    }
}

@Composable
private fun SubscriptionCard(
    subscriptionUrl: String,
    isSyncing: Boolean,
    onUrlChange: (String) -> Unit,
    onSync: () -> Unit,
    onRestore: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("网络订阅", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                text = "支持 M3U/EXTINF 和 OpenTV JSON 格式；同名频道会合并为多条线路。同步后会替换当前频道列表。",
                modifier = Modifier.padding(top = 6.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = subscriptionUrl,
                onValueChange = onUrlChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                label = { Text("订阅地址") },
                placeholder = { Text("https://example.com/channels.json") },
                singleLine = true,
                enabled = !isSyncing,
            )
            if (isSyncing) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(onClick = onSync, enabled = !isSyncing && subscriptionUrl.isNotBlank()) {
                    Icon(Icons.Rounded.CloudDownload, contentDescription = null)
                    Text("同步", modifier = Modifier.padding(start = 8.dp))
                }
                OutlinedButton(onClick = onRestore, enabled = !isSyncing) {
                    Icon(Icons.Rounded.Restore, contentDescription = null)
                    Text("恢复内置", modifier = Modifier.padding(start = 8.dp))
                }
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, top = 12.dp, end = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(channel.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "${channel.urls.size} 条播放线路",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onAdd) {
                Icon(Icons.Rounded.Add, contentDescription = "为 ${channel.name} 添加播放源")
            }
        }
        channel.urls.forEachIndexed { index, url ->
            HorizontalDivider()
            ListItem(
                headlineContent = { Text("线路 ${index + 1}") },
                supportingContent = {
                    Text(
                        text = displayUrl(url),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                trailingContent = {
                    Row {
                        IconButton(onClick = { onEdit(index, url) }) {
                            Icon(Icons.Rounded.Edit, contentDescription = "编辑线路 ${index + 1}")
                        }
                        IconButton(onClick = { onRemove(index) }, enabled = channel.urls.size > 1) {
                            Icon(Icons.Rounded.Delete, contentDescription = "删除线路 ${index + 1}")
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun SourceEditorDialog(
    state: SourceEditorState,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var value by remember(state) { mutableStateOf(state.value) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (state.sourceIndex == null) "添加播放源" else "编辑播放源") },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("播放地址") },
                placeholder = { Text("https://example.com/live.m3u8") },
                minLines = 2,
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(value) }, enabled = value.isNotBlank()) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

private fun displayUrl(url: String): String {
    val uri = Uri.parse(url)
    return buildString {
        append(uri.host ?: url)
        if (!uri.path.isNullOrBlank()) append(uri.path)
    }
}
