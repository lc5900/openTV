package com.lc5900.tv.ui.player

import android.annotation.SuppressLint
import android.net.Uri
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.lc5900.tv.data.TvChannel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnsafeOptInUsageError")
@Composable
fun PlayerScreen(channel: TvChannel, onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var sourceIndex by remember(channel.id) { mutableIntStateOf(0) }
    var isBuffering by remember(channel.id) { mutableStateOf(true) }
    var errorMessage by remember(channel.id) { mutableStateOf<String?>(null) }
    var showSourceSelector by remember(channel.id) { mutableStateOf(false) }

    val player = remember(channel.id) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(androidx.media3.common.MediaItem.fromUri(channel.urls.first()))
            playWhenReady = true
            prepare()
        }
    }

    fun playSource(index: Int, message: String? = null) {
        sourceIndex = index
        isBuffering = true
        errorMessage = message
        player.setMediaItem(androidx.media3.common.MediaItem.fromUri(channel.urls[index]))
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
                    playSource(nextIndex, "当前播放源不可用，正在切换备用源…")
                } else {
                    isBuffering = false
                    errorMessage = "所有播放源均不可用，请稍后重试"
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(channel.name)
                        Text(
                            text = "播放源 ${sourceIndex + 1}/${channel.urls.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f),
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "返回频道列表")
                    }
                },
                actions = {
                    TextButton(onClick = { showSourceSelector = true }) {
                        Text(
                            text = "线路 ${sourceIndex + 1}",
                            color = Color.White,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                ),
            )
        },
        containerColor = Color.Black,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                    }
                },
                update = { it.player = player },
                modifier = Modifier.fillMaxSize(),
            )

            if (isBuffering) CircularProgressIndicator(color = Color.White)

            errorMessage?.let { message ->
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.72f))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(message, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }

    if (showSourceSelector) {
        ModalBottomSheet(onDismissRequest = { showSourceSelector = false }) {
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                Text(
                    text = "选择播放线路",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.titleLarge,
                )
                channel.urls.forEachIndexed { index, url ->
                    ListItem(
                        headlineContent = { Text("线路 ${index + 1}") },
                        supportingContent = {
                            Text(Uri.parse(url).host ?: "直播播放源")
                        },
                        leadingContent = {
                            RadioButton(
                                selected = index == sourceIndex,
                                onClick = null,
                            )
                        },
                        modifier = Modifier.clickable {
                            showSourceSelector = false
                            if (index != sourceIndex) playSource(index)
                        },
                    )
                }
            }
        }
    }
}
