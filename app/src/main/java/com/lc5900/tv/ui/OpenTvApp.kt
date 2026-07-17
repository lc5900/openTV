package com.lc5900.tv.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.lc5900.tv.ui.components.GradientButton
import com.lc5900.tv.ui.components.OpenTvBackground
import com.lc5900.tv.ui.components.OpenTvColors
import com.lc5900.tv.ui.player.PlayerScreen

@Composable
fun OpenTvApp(viewModel: ChannelViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedChannelId by rememberSaveable { androidx.compose.runtime.mutableStateOf<Int?>(null) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    val selectedChannel = uiState.channels.firstOrNull { it.id == selectedChannelId }

    BackHandler(enabled = selectedChannel != null) { selectedChannelId = null }
    BackHandler(enabled = showSettings && selectedChannel == null) { showSettings = false }

    when {
        selectedChannel != null -> PlayerScreen(selectedChannel) { selectedChannelId = null }
        showSettings -> SettingsScreen(
            uiState = uiState,
            onBack = { showSettings = false },
            onSubscriptionUrlChange = viewModel::updateSubscriptionUrl,
            onSync = viewModel::syncSubscription,
            onRestoreBuiltIn = viewModel::restoreBuiltInCatalog,
            onAddChannel = viewModel::addChannel,
            onAddSource = viewModel::addSource,
            onUpdateSource = viewModel::updateSource,
            onRemoveSource = viewModel::removeSource,
            onMessageShown = viewModel::clearSettingsMessage,
        )
        else -> ChannelBrowser(
            uiState = uiState,
            onQueryChange = viewModel::updateQuery,
            onRefresh = viewModel::refresh,
            onOpenSettings = { showSettings = true },
            onChannelClick = { selectedChannelId = it.id },
        )
    }
}

@Composable
private fun ChannelBrowser(
    uiState: ChannelUiState,
    onQueryChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onOpenSettings: () -> Unit,
    onChannelClick: (TvChannel) -> Unit,
) {
    var onlyMultiSource by rememberSaveable { mutableStateOf(false) }
    OpenTvBackground(Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> LoadingState()
            uiState.errorMessage != null -> ErrorState(uiState.errorMessage, onRefresh, onOpenSettings)
            else -> {
                val channels = uiState.visibleChannels.filter { !onlyMultiSource || it.urls.size > 1 }
                LazyColumn(
                    modifier = Modifier
                        .widthIn(max = 720.dp)
                        .fillMaxSize()
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item { HomeHeader(onRefresh, onOpenSettings) }
                    item { WelcomeBanner(uiState.channels) }
                    item {
                        SearchBar(
                            value = uiState.query,
                            onlyMultiSource = onlyMultiSource,
                            onValueChange = onQueryChange,
                            onFilterClick = { onlyMultiSource = !onlyMultiSource },
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("全部频道", style = MaterialTheme.typography.titleLarge)
                            Text(
                                "${channels.size} 个频道",
                                style = MaterialTheme.typography.labelMedium,
                                color = OpenTvColors.TextSecondary,
                            )
                        }
                    }
                    if (channels.isEmpty()) {
                        item { EmptyState(onlyMultiSource) }
                    } else {
                        items(channels, key = TvChannel::id) { channel ->
                            ChannelRow(channel) { onChannelClick(channel) }
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(onRefresh: () -> Unit, onOpenSettings: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            HeaderIcon(Icons.Rounded.Refresh, "刷新频道", onRefresh)
            Spacer(Modifier.size(8.dp))
            HeaderIcon(Icons.Rounded.Settings, "设置", onOpenSettings)
        }
        Row(
            modifier = Modifier.padding(start = 18.dp, top = 18.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_brand),
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(22.dp)),
            )
            Column(modifier = Modifier.padding(start = 18.dp)) {
                Text(
                    "OpenTV",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    "开放、纯粹的电视体验",
                    modifier = Modifier.padding(top = 4.dp),
                    color = OpenTvColors.TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun HeaderIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
) {
    Surface(shape = CircleShape, color = OpenTvColors.SurfaceHigh.copy(alpha = 0.88f)) {
        IconButton(onClick = onClick, modifier = Modifier.size(44.dp)) {
            Icon(icon, contentDescription = description, tint = Color.White)
        }
    }
}

@Composable
private fun WelcomeBanner(channels: List<TvChannel>) {
    val groupCount = channels.map { it.group.ifBlank { "其他" } }.distinct().size
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(OpenTvColors.HeroGradient)
            .padding(horizontal = 18.dp, vertical = 17.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.12f),
            ) {
                Icon(
                    Icons.Rounded.LiveTv,
                    contentDescription = null,
                    modifier = Modifier.padding(11.dp),
                    tint = Color(0xFFC59DFF),
                )
            }
            Column(modifier = Modifier.padding(start = 14.dp)) {
                Text("欢迎使用 OpenTV", fontWeight = FontWeight.Bold)
                Text(
                    "共 ${channels.size} 个频道  |  $groupCount 个分组  |  多线路支持",
                    modifier = Modifier.padding(top = 3.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.78f),
                )
            }
        }
    }
}

@Composable
private fun SearchBar(
    value: String,
    onlyMultiSource: Boolean,
    onValueChange: (String) -> Unit,
    onFilterClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("搜索频道或分组…") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(17.dp),
            colors = TextFieldDefaults.colors(
                focusedTextColor = OpenTvColors.TextPrimary,
                unfocusedTextColor = OpenTvColors.TextPrimary,
                cursorColor = OpenTvColors.Cyan,
                focusedPlaceholderColor = OpenTvColors.TextSecondary,
                unfocusedPlaceholderColor = OpenTvColors.TextSecondary,
                focusedLeadingIconColor = OpenTvColors.TextSecondary,
                unfocusedLeadingIconColor = OpenTvColors.TextSecondary,
                focusedContainerColor = OpenTvColors.Surface,
                unfocusedContainerColor = OpenTvColors.Surface,
                disabledContainerColor = OpenTvColors.Surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
        )
        Surface(
            modifier = Modifier.size(54.dp),
            shape = RoundedCornerShape(17.dp),
            color = if (onlyMultiSource) OpenTvColors.Purple else OpenTvColors.Surface,
        ) {
            IconButton(onClick = onFilterClick) {
                Icon(Icons.Rounded.FilterAlt, contentDescription = "仅显示多线路频道")
            }
        }
    }
}

@Composable
private fun ChannelRow(channel: TvChannel, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(15.dp))
            .clickable(onClick = onClick),
        color = OpenTvColors.Surface.copy(alpha = 0.94f),
        shape = RoundedCornerShape(15.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                channel.id.toString().padStart(3, '0'),
                style = MaterialTheme.typography.labelLarge,
                color = OpenTvColors.TextPrimary,
            )
            Surface(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .size(42.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (channel.id % 2 == 0) Color(0xFF174C73) else Color(0xFF46246E),
            ) {
                Icon(
                    Icons.Rounded.LiveTv,
                    contentDescription = null,
                    modifier = Modifier.padding(9.dp),
                    tint = if (channel.id % 2 == 0) OpenTvColors.Cyan else Color(0xFFD1A5FF),
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        channel.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Surface(
                        modifier = Modifier.padding(start = 8.dp),
                        shape = RoundedCornerShape(5.dp),
                        color = OpenTvColors.Red.copy(alpha = 0.26f),
                    ) {
                        Text(
                            "直播中",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFFA1A7),
                        )
                    }
                }
                Text(
                    channel.group.ifBlank { "直播频道" },
                    style = MaterialTheme.typography.bodySmall,
                    color = OpenTvColors.TextSecondary,
                )
            }
            Surface(
                shape = RoundedCornerShape(11.dp),
                color = Color(0xFF211842),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("${channel.urls.size} 条", color = Color(0xFFC9A8FF), fontWeight = FontWeight.Bold)
                    Text("线路", style = MaterialTheme.typography.labelSmall, color = OpenTvColors.TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun EmptyState(filtered: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Rounded.Search, contentDescription = null, tint = OpenTvColors.TextSecondary)
        Text(
            if (filtered) "没有多线路频道" else "没有找到匹配的频道",
            modifier = Modifier.padding(top = 12.dp),
            color = OpenTvColors.TextSecondary,
        )
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_brand),
            contentDescription = null,
            modifier = Modifier.size(92.dp),
        )
        Text("OpenTV", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 18.dp))
        Text("正在加载频道…", color = OpenTvColors.TextSecondary, modifier = Modifier.padding(top = 12.dp))
        CircularProgressIndicator(
            modifier = Modifier
                .padding(top = 24.dp)
                .size(34.dp),
            color = OpenTvColors.Purple,
            trackColor = OpenTvColors.SurfaceHigh,
        )
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit, onOpenSettings: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(modifier = Modifier.size(88.dp), shape = CircleShape, color = OpenTvColors.Red.copy(alpha = 0.14f)) {
            Icon(
                Icons.Rounded.LiveTv,
                contentDescription = null,
                modifier = Modifier.padding(22.dp),
                tint = OpenTvColors.Red,
            )
        }
        Text("读取频道失败", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 22.dp))
        Text(
            message,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            color = OpenTvColors.TextSecondary,
        )
        GradientButton("重试", onRetry, Modifier.fillMaxWidth())
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .clickable(onClick = onOpenSettings),
            shape = RoundedCornerShape(15.dp),
            color = OpenTvColors.Surface,
        ) {
            Text("打开设置", modifier = Modifier.padding(15.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
