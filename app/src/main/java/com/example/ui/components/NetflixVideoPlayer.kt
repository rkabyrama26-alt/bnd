package com.example.ui.components

import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.ZoomOutMap
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.R
import com.example.ui.theme.HeartRed
import com.example.ui.theme.LiveDotRed
import com.example.ui.theme.PlayerPillBg
import com.example.ui.theme.TajawalFontFamily
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun NetflixVideoPlayer(
    streamUrl: String?,
    title: String,
    isLive: Boolean,
    liveBadgeText: String,
    isStretched: Boolean,
    isFullscreen: Boolean,
    onToggleStretch: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    var isPlaying by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var volume by remember { mutableFloatStateOf(1f) }
    var currentPos by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var bufferedPos by remember { mutableLongStateOf(0L) }
    var controlsVisible by remember { mutableStateOf(!isLive) }
    var interactionTick by remember { mutableLongStateOf(0L) }

    // Update media source whenever streamUrl changes
    LaunchedEffect(streamUrl) {
        if (streamUrl.isNullOrBlank()) {
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            isBuffering = false
            isPlaying = false
        } else {
            isBuffering = true
            val itemBuilder = MediaItem.Builder().setUri(streamUrl)
            if (streamUrl.contains(".m3u8", ignoreCase = true)) {
                itemBuilder.setMimeType(MimeTypes.APPLICATION_M3U8)
            }
            exoPlayer.setMediaItem(itemBuilder.build())
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
            if (isLive) {
                controlsVisible = false
            }
        }
    }

    // Listen to ExoPlayer events
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
                if (!playing) {
                    controlsVisible = true
                } else {
                    interactionTick++
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
                if (playbackState == Player.STATE_ENDED) {
                    controlsVisible = true
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                isBuffering = false
            }
        }
        exoPlayer.addListener(listener)

        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Poll playback progress for VOD
    LaunchedEffect(exoPlayer, isLive) {
        while (true) {
            if (!isLive) {
                currentPos = exoPlayer.currentPosition.coerceAtLeast(0L)
                duration = exoPlayer.duration.coerceAtLeast(0L)
                bufferedPos = exoPlayer.bufferedPosition.coerceAtLeast(0L)
            }
            delay(350)
        }
    }

    // Auto-hide controls after 3s while playing
    LaunchedEffect(controlsVisible, isPlaying, interactionTick) {
        if (controlsVisible && isPlaying) {
            delay(3000)
            controlsVisible = false
        }
    }

    val containerModifier = if (isFullscreen) {
        modifier.fillMaxSize().background(Color.Black)
    } else {
        modifier.fillMaxWidth().aspectRatio(16f / 9f).background(Color.Black)
    }

    Box(
        modifier = containerModifier
            .pointerInput(isPlaying) {
                detectTapGestures(
                    onTap = {
                        if (!isPlaying) {
                            controlsVisible = true
                        } else {
                            controlsVisible = !controlsVisible
                            if (controlsVisible) interactionTick++
                        }
                    }
                )
            }
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    player = exoPlayer
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(android.graphics.Color.BLACK)
                }
            },
            update = { view ->
                view.resizeMode = if (isStretched) {
                    AspectRatioFrameLayout.RESIZE_MODE_FILL
                } else {
                    AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Buffering spinner (#E50914)
        if (isBuffering) {
            CircularProgressIndicator(
                color = HeartRed,
                trackColor = Color.White.copy(alpha = 0.15f),
                strokeWidth = 3.dp,
                modifier = Modifier
                    .size(44.dp)
                    .align(Alignment.Center)
            )
        }

        // Netflix-style overlay controls
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.Black.copy(alpha = 0.75f),
                            0.25f to Color.Transparent,
                            0.60f to Color.Transparent,
                            1.0f to Color.Black.copy(alpha = 0.85f)
                        )
                    )
            ) {
                // Top bar: Back button + Title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BackCircleButton(
                        onClick = onBackClick,
                        size = 38.dp,
                        testTag = "player_back_button"
                    )
                    Text(
                        text = title.ifBlank { "—" },
                        color = Color.White,
                        fontFamily = TajawalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.8f),
                                offset = Offset(0f, 2f),
                                blurRadius = 6f
                            )
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Big center Play button for VOD when paused
                if (!isLive && !isPlaying && !isBuffering) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.18f))
                            .border(2.dp, Color.White.copy(alpha = 0.55f), CircleShape)
                            .clickable {
                                exoPlayer.play()
                                interactionTick++
                            }
                            .testTag("player_big_play"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = stringResource(R.string.play_pause),
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                // Bottom controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Progress bar (VOD only)
                    if (!isLive) {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            val total = duration.coerceAtLeast(1L).toFloat()
                            val playedPct = (currentPos / total).coerceIn(0f, 1f)
                            val bufferPct = (bufferedPos / total).coerceIn(0f, 1f)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp)
                                    .height(20.dp)
                                    .pointerInput(duration) {
                                        detectTapGestures { offset ->
                                            if (duration > 0L) {
                                                val pct = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                                                exoPlayer.seekTo((pct * duration).toLong())
                                                interactionTick++
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                ) {
                                    drawRoundRect(
                                        color = Color.White.copy(alpha = 0.25f),
                                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                                    )
                                    drawRoundRect(
                                        color = Color.White.copy(alpha = 0.35f),
                                        size = Size(size.width * bufferPct, size.height),
                                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                                    )
                                    drawRoundRect(
                                        color = HeartRed,
                                        size = Size(size.width * playedPct, size.height),
                                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                                    )
                                    drawCircle(
                                        color = HeartRed,
                                        radius = 7.dp.toPx(),
                                        center = Offset(size.width * playedPct, size.height / 2f)
                                    )
                                }
                            }
                        }
                    }

                    // Pill control bar
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(40.dp))
                                .background(PlayerPillBg)
                                .border(
                                    1.dp,
                                    Color.White.copy(alpha = 0.08f),
                                    RoundedCornerShape(40.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                                    interactionTick++
                                },
                                modifier = Modifier.size(34.dp).testTag("player_play_pause")
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = stringResource(R.string.play_pause),
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            if (!isLive) {
                                IconButton(
                                    onClick = {
                                        exoPlayer.seekTo((exoPlayer.currentPosition - 10_000L).coerceAtLeast(0L))
                                        interactionTick++
                                    },
                                    modifier = Modifier.size(34.dp).testTag("player_rewind")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Replay10,
                                        contentDescription = stringResource(R.string.rewind_10),
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val target = exoPlayer.currentPosition + 10_000L
                                        val maxDur = if (duration > 0L) duration else target
                                        exoPlayer.seekTo(target.coerceAtMost(maxDur))
                                        interactionTick++
                                    },
                                    modifier = Modifier.size(34.dp).testTag("player_forward")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Forward10,
                                        contentDescription = stringResource(R.string.forward_10),
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = if (isLive) liveBadgeText else "${formatMillis(currentPos)} / ${formatMillis(duration)}",
                                color = if (isLive) LiveDotRed else Color.White,
                                fontFamily = TajawalFontFamily,
                                fontWeight = if (isLive) FontWeight.Black else FontWeight.Bold,
                                fontSize = 11.sp,
                                maxLines = 1
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            IconButton(
                                onClick = {
                                    isMuted = !isMuted
                                    exoPlayer.volume = if (isMuted) 0f else volume
                                    interactionTick++
                                },
                                modifier = Modifier.size(32.dp).testTag("player_mute")
                            ) {
                                Icon(
                                    imageVector = if (isMuted || volume == 0f) {
                                        Icons.AutoMirrored.Rounded.VolumeOff
                                    } else {
                                        Icons.AutoMirrored.Rounded.VolumeUp
                                    },
                                    contentDescription = stringResource(R.string.mute),
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Slider(
                                value = if (isMuted) 0f else volume,
                                onValueChange = {
                                    volume = it
                                    isMuted = it == 0f
                                    exoPlayer.volume = it
                                    interactionTick++
                                },
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.White,
                                    activeTrackColor = Color.White.copy(alpha = 0.8f),
                                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .width(56.dp)
                                    .height(24.dp)
                            )

                            IconButton(
                                onClick = {
                                    onToggleStretch()
                                    interactionTick++
                                },
                                modifier = Modifier.size(32.dp).testTag("player_stretch")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ZoomOutMap,
                                    contentDescription = stringResource(R.string.stretch_video),
                                    tint = if (isStretched) HeartRed else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    onToggleFullscreen()
                                    interactionTick++
                                },
                                modifier = Modifier.size(32.dp).testTag("player_fullscreen")
                            ) {
                                Icon(
                                    imageVector = if (isFullscreen) {
                                        Icons.Rounded.FullscreenExit
                                    } else {
                                        Icons.Rounded.Fullscreen
                                    },
                                    contentDescription = stringResource(R.string.fullscreen),
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatMillis(ms: Long): String {
    if (ms <= 0L) return "00:00"
    val totalSec = ms / 1000L
    val h = totalSec / 3600L
    val m = (totalSec % 3600L) / 60L
    val s = totalSec % 60L
    return if (h > 0) {
        String.format("%d:%02d:%02d", h, m, s)
    } else {
        String.format("%02d:%02d", m, s)
    }
}
