package com.lc5900.tv.ui.player

import android.annotation.SuppressLint
import android.net.Uri
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.SignalCellularAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.lc5900.tv.data.TvChannel
import com.lc5900.tv.ui.components.OpenTvColors

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun PlayerScreen(channel: TvChannel, onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var sourceIndex by remember(channel.id) { mutableIntStateOf(0) }
    var isBuffering by remember(channel.id) { mutableStateOf(true) }
    var errorMessage by remember(channel.id) { mutableStateOf<String?>(null) }
    var showSources by remember(channel.id) { mutableStateOf(true) }

    val player = remember(channel.id) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(channel.urls.first()))
            playWhenReady = true
            prepare()
        }
    }

    fun playSource(index: Int, message: String? = null) {
        sourceIndex = index
        isBuffering = true
        errorMessage = message
        player.setMediaItem(MediaItem.fromUri(channel.urls[index]))
        player.prepare()
        player.play()
    }

    DisposableEffect(player, lifecycleOwner, channel.id) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
                if (playbackState == Player.STATE_READY) errorMessage = null
            }

            override fun onPlayerError(error: PlaybackException) {
                val nextIndex = sourceIndex + 1
                if (nextIndex < channel.urls.size) {
                    playSource(nextIndex, "当前线路不可用，正在尝试下一条线路…")
                } else {
                    isBuffering = false
                    errorMessage = "所有播放线路均不可用，请稍后重试"
                }
            }
        }
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) player.pause()
        }
        player.addListener(listener)
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            player.removeListener(listener)
            player.release()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding(),
    ) {
        PlayerTopBar(
            channel = channel,
            sourceIndex = sourceIndex,
            showSources = showSources,
            onBack = onBack,
            onToggleSources = { showSources = !showSources },
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            AndroidView(
                factory = { viewContext ->
                    PlayerView(viewContext).apply {
                        this.player = player
                        useController = true
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        keepScreenOn = true
                        setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                    }
                },
                update = { it.player = player },
                modifier = Modifier.fillMaxSize(),
            )
            if (isBuffering) {
                Surface(shape = CircleShape, color = Color.Black.copy(alpha = 0.64f)) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(16.dp)
                            .size(34.dp),
                        color = OpenTvColors.Purple,
                    )
                }
            }
            errorMessage?.let { message ->
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(12.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.82f),
                ) {
                    Text(
                        message,
                        modifier = Modifier.padding(12.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
        if (showSources) {
            SourcePanel(
                channel = channel,
                sourceIndex = sourceIndex,
                onSelect = { index -> if (index != sourceIndex) playSource(index) },
                onDismiss = { showSources = false },
            )
        }
    }
}

@Composable
private fun PlayerTopBar(
    channel: TvChannel,
    sourceIndex: Int,
    showSources: Boolean,
    onBack: () -> Unit,
    onToggleSources: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "返回频道列表", tint = Color.White)
        }
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                channel.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Text(
                "播放源 ${sourceIndex + 1}/${channel.urls.size}",
                style = MaterialTheme.typography.bodySmall,
                color = OpenTvColors.TextSecondary,
            )
        }
        Surface(
            modifier = Modifier.clickable(onClick = onToggleSources),
            shape = RoundedCornerShape(14.dp),
            color = OpenTvColors.SurfaceHigh,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("线路", color = Color.White)
                Icon(
                    if (showSources) Icons.Rounded.KeyboardArrowDown else Icons.Rounded.KeyboardArrowUp,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun SourcePanel(
    channel: TvChannel,
    sourceIndex: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var dragOffset by remember { mutableStateOf(0f) }
    val dismissThreshold = with(LocalDensity.current) { 72.dp.toPx() }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .graphicsLayer { translationY = dragOffset },
        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
        color = OpenTvColors.BackgroundElevated,
    ) {
        Column(modifier = Modifier.padding(top = 6.dp, bottom = 10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 28.dp)
                    .pointerInput(dismissThreshold) {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                if (dragOffset >= dismissThreshold) onDismiss() else dragOffset = 0f
                            },
                            onDragCancel = { dragOffset = 0f },
                        ) { change, dragAmount ->
                            change.consume()
                            dragOffset = (dragOffset + dragAmount).coerceAtLeast(0f)
                        }
                    }
                    .clickable(onClickLabel = "隐藏线路面板", onClick = onDismiss),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 42.dp, height = 4.dp)
                        .background(OpenTvColors.Outline, CircleShape),
                )
            }
            Text(
                "选择播放源",
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 7.dp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            LazyColumn(
                modifier = Modifier.heightIn(max = 280.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                itemsIndexed(channel.urls) { index, url ->
                    SourceRow(
                        index = index,
                        url = url,
                        selected = index == sourceIndex,
                        onClick = { onSelect(index) },
                    )
                }
            }
            Text(
                "当前线路播放失败时，将自动尝试下一条线路",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 10.dp),
                style = MaterialTheme.typography.bodySmall,
                color = OpenTvColors.TextSecondary,
            )
        }
    }
}

@Composable
private fun SourceRow(index: Int, url: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(13.dp),
        color = if (selected) Color(0xFF171D2D) else Color.Transparent,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(modifier = Modifier.size(28.dp), shape = CircleShape, color = OpenTvColors.SurfaceHigh) {
                Box(contentAlignment = Alignment.Center) {
                    Text("${index + 1}", style = MaterialTheme.typography.labelMedium)
                }
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 11.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "线路 ${index + 1}${if (index == 0) "（主线路）" else "（备用）"}",
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (selected) {
                        Surface(
                            modifier = Modifier.padding(start = 8.dp),
                            shape = RoundedCornerShape(6.dp),
                            color = OpenTvColors.Purple.copy(alpha = 0.35f),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Rounded.Check, null, modifier = Modifier.size(12.dp), tint = Color(0xFFD1B9FF))
                                Text("当前播放", style = MaterialTheme.typography.labelSmall, color = Color(0xFFD1B9FF))
                            }
                        }
                    }
                }
                Text(
                    displayUrl(url),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = OpenTvColors.TextSecondary,
                )
            }
            Icon(Icons.Rounded.SignalCellularAlt, contentDescription = "线路信号", tint = OpenTvColors.Green)
        }
    }
}

private fun displayUrl(url: String): String {
    val uri = Uri.parse(url)
    return buildString {
        append(uri.host ?: url)
        if (!uri.path.isNullOrBlank()) append(uri.path)
    }
}
