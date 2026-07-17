package com.lc5900.tv.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lc5900.tv.R
import com.lc5900.tv.data.TvChannel
import com.lc5900.tv.ui.player.PlayerScreen

@Composable
fun OpenTvApp(viewModel: ChannelViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedChannelId by rememberSaveable { androidx.compose.runtime.mutableStateOf<Int?>(null) }
    var showSettings by rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
    val selectedChannel = uiState.channels.firstOrNull { it.id == selectedChannelId }

    BackHandler(enabled = selectedChannel != null) { selectedChannelId = null }
    BackHandler(enabled = showSettings && selectedChannel == null) { showSettings = false }

    when {
        selectedChannel != null -> {
            PlayerScreen(channel = selectedChannel, onBack = { selectedChannelId = null })
        }
        showSettings -> {
            SettingsScreen(
                uiState = uiState,
                onBack = { showSettings = false },
                onSubscriptionUrlChange = viewModel::updateSubscriptionUrl,
                onSync = viewModel::syncSubscription,
                onRestoreBuiltIn = viewModel::restoreBuiltInCatalog,
                onAddSource = viewModel::addSource,
                onUpdateSource = viewModel::updateSource,
                onRemoveSource = viewModel::removeSource,
                onMessageShown = viewModel::clearSettingsMessage,
            )
        }
        else -> {
            ChannelBrowser(
                uiState = uiState,
                onQueryChange = viewModel::updateQuery,
                onRefresh = viewModel::refresh,
                onOpenSettings = { showSettings = true },
                onChannelClick = { selectedChannelId = it.id },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChannelBrowser(
    uiState: ChannelUiState,
    onQueryChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onOpenSettings: () -> Unit,
    onChannelClick: (TvChannel) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.ic_launcher_brand),
                            contentDescription = null,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp)),
                        )
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text("OpenTV", fontWeight = FontWeight.Bold)
                            Text(
                                text = "开放、纯粹的电视体验",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Rounded.Settings, contentDescription = "设置")
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "刷新频道")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                            MaterialTheme.colorScheme.background,
                        ),
                    ),
                )
                .padding(contentPadding),
        ) {
            when {
                uiState.isLoading -> LoadingState()
                uiState.errorMessage != null -> ErrorState(uiState.errorMessage, onRefresh)
                else -> ChannelContent(uiState, onQueryChange, onChannelClick)
            }
        }
    }
}

@Composable
private fun ChannelContent(
    uiState: ChannelUiState,
    onQueryChange: (String) -> Unit,
    onChannelClick: (TvChannel) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        WelcomeBanner(channelCount = uiState.channels.size)
        OutlinedTextField(
            value = uiState.query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            placeholder = { Text("搜索频道") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(22.dp),
        )

        Row(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary),
            )
            Text(
                text = "${uiState.visibleChannels.size} 个可用频道",
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 280.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(uiState.visibleChannels, key = TvChannel::id) { channel ->
                ChannelCard(channel = channel, onClick = { onChannelClick(channel) })
            }
        }
    }
}

@Composable
private fun WelcomeBanner(channelCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF043B4A), Color(0xFF075C70), Color(0xFF166A9A)),
                ),
            )
            .padding(horizontal = 22.dp, vertical = 20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(58.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.14f),
            ) {
                Icon(
                    imageVector = Icons.Rounded.LiveTv,
                    contentDescription = null,
                    modifier = Modifier.padding(15.dp),
                    tint = Color(0xFF73F2F2),
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
            ) {
                Text(
                    text = "电视，简单一点",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                )
                Text(
                    text = "$channelCount 个直播频道 · 支持多线路自动切换",
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.72f),
                )
            }
            Surface(
                shape = CircleShape,
                color = Color(0xFF4EE0C1).copy(alpha = 0.18f),
            ) {
                Text(
                    text = "LIVE",
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF71F1D3),
                )
            }
        }
    }
}

@Composable
private fun ChannelCard(channel: TvChannel, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp, pressedElevation = 4.dp),
        modifier = Modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
            shape = RoundedCornerShape(26.dp),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(19.dp),
                color = if (channel.id % 2 == 0) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                },
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = channel.id.toString().padStart(2, '0'),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = "CH",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f),
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp),
            ) {
                Text(
                    text = channel.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (channel.urls.size > 1) "${channel.urls.size} 个备用播放源" else "高清直播",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 3.dp,
            ) {
                Icon(
                    Icons.Rounded.PlayArrow,
                    contentDescription = "播放 ${channel.name}",
                    modifier = Modifier.padding(9.dp),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Text("正在加载频道…", modifier = Modifier.padding(top = 16.dp))
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("无法加载频道", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            message,
            modifier = Modifier.padding(vertical = 12.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        androidx.compose.material3.Button(onClick = onRetry) { Text("重试") }
    }
}
