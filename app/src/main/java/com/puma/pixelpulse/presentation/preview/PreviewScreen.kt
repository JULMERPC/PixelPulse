package com.puma.pixelpulse.presentation.preview

import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.puma.pixelpulse.domain.model.ScaleMode
import com.puma.pixelpulse.domain.model.Wallpaper
import com.puma.pixelpulse.wallpaper.WallpaperScaleTransform
import kotlinx.coroutines.launch

private val PRESET_COLORS = listOf(
    0xFF000000L to "Negro",
    0xFFFFFFFFL to "Blanco",
    0xFF1A1A2EL to "Azul oscuro",
    0xFF16213EL to "Marino",
    0xFF0F3460L to "Navy",
    0xFF533483L to "Púrpura",
    0xFFE94560L to "Rojo",
    0xFF2D6A4FL to "Verde oscuro"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    wallpaperId: Long,
    onBack: () -> Unit,
    onApply: (Intent) -> Unit,
    viewModel: PreviewViewModel = hiltViewModel()
) {
    val wallpaper by viewModel.wallpaper.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val isMuted by viewModel.isMuted.collectAsStateWithLifecycle()
    val loopEnabled by viewModel.loopEnabled.collectAsStateWithLifecycle()
    val playbackSpeed by viewModel.playbackSpeed.collectAsStateWithLifecycle()
    val trimStartMs by viewModel.trimStartMs.collectAsStateWithLifecycle()
    val trimEndMs by viewModel.trimEndMs.collectAsStateWithLifecycle()
    val videoDurationMs by viewModel.videoDurationMs.collectAsStateWithLifecycle()
    val backgroundColor by viewModel.backgroundColor.collectAsStateWithLifecycle()
    val scaleMode by viewModel.scaleMode.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    BackHandler {
        if (sheetState.isVisible) {
            scope.launch { sheetState.hide() }
        } else {
            onBack()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                if (sheetState.isVisible) {
                    scope.launch { sheetState.hide() }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(wallpaperId) {
        viewModel.loadWallpaper(wallpaperId)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        wallpaper?.let { wp ->
            VideoPreview(
                wallpaper = wp,
                isPlaying = isPlaying,
                isMuted = isMuted,
                loopEnabled = loopEnabled,
                playbackSpeed = playbackSpeed,
                trimStartMs = trimStartMs,
                trimEndMs = trimEndMs,
                backgroundColor = backgroundColor,
                scaleMode = scaleMode,
                onDurationReady = { viewModel.updateVideoDuration(it) },
                modifier = Modifier.fillMaxSize()
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.7f),
                            Color.Transparent,
                            Color.Transparent,
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        text = wallpaper?.name ?: "Preview",
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { scope.launch { sheetState.show() } },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                )
            ) {
                Text(
                    text = "Configurar wallpaper",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    if (sheetState.isVisible) {
        ModalBottomSheet(
            onDismissRequest = { scope.launch { sheetState.hide() } },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = null
        ) {
            BottomSheetContent(
                isPlaying = isPlaying,
                isMuted = isMuted,
                loopEnabled = loopEnabled,
                playbackSpeed = playbackSpeed,
                trimStartMs = trimStartMs,
                trimEndMs = trimEndMs,
                videoDurationMs = videoDurationMs,
                backgroundColor = backgroundColor,
                scaleMode = scaleMode,
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onToggleMute = { viewModel.toggleMute() },
                onToggleLoop = { viewModel.toggleLoop() },
                onSetSpeed = { viewModel.setPlaybackSpeed(it) },
                onSetTrimStart = { viewModel.setTrimStart(it) },
                onSetTrimEnd = { viewModel.setTrimEnd(it) },
                onSetBackgroundColor = { viewModel.setBackgroundColor(it) },
                onSetScaleMode = { viewModel.setScaleMode(it) },
                onApply = {
                    viewModel.applyWallpaper()?.let { intent ->
                        onApply(intent)
                    }
                }
            )
        }
    }
}

@Composable
private fun BottomSheetContent(
    isPlaying: Boolean,
    isMuted: Boolean,
    loopEnabled: Boolean,
    playbackSpeed: Float,
    trimStartMs: Long,
    trimEndMs: Long,
    videoDurationMs: Long,
    backgroundColor: Long,
    scaleMode: ScaleMode,
    onTogglePlayPause: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleLoop: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    onSetTrimStart: (Long) -> Unit,
    onSetTrimEnd: (Long) -> Unit,
    onSetBackgroundColor: (Long) -> Unit,
    onSetScaleMode: (ScaleMode) -> Unit,
    onApply: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ControlButton(
                icon = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                label = if (isPlaying) "Pausa" else "Play",
                isActive = isPlaying,
                onClick = onTogglePlayPause
            )
            ControlButton(
                icon = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                label = if (isMuted) "Mute" else "Sonido",
                isActive = !isMuted,
                onClick = onToggleMute
            )
            ControlButton(
                icon = Icons.Filled.Repeat,
                label = "Loop",
                isActive = loopEnabled,
                onClick = onToggleLoop
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle(icon = Icons.Filled.Speed, title = "Velocidad: ${playbackSpeed}x")
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(0.25f, 0.5f, 1f, 1.5f, 2f, 3f).forEach { speed ->
                SpeedChip(
                    speed = speed,
                    isSelected = playbackSpeed == speed,
                    onClick = { onSetSpeed(speed) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (videoDurationMs > 0) {
            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle(
                icon = Icons.Filled.Speed,
                title = "Recortar: ${formatTime(trimStartMs)} - ${formatTime(trimEndMs)}"
            )
            Spacer(modifier = Modifier.height(8.dp))
            val sliderStart = remember(trimStartMs, videoDurationMs) {
                if (videoDurationMs > 0) trimStartMs.toFloat() / videoDurationMs.toFloat() else 0f
            }
            val sliderEnd = remember(trimEndMs, videoDurationMs) {
                if (videoDurationMs > 0) trimEndMs.toFloat() / videoDurationMs.toFloat() else 1f
            }
            RangeSlider(
                value = sliderStart..sliderEnd,
                onValueChange = { range ->
                    val startMs = (range.start * videoDurationMs).toLong()
                    val endMs = (range.endInclusive * videoDurationMs).toLong()
                    if (endMs - startMs >= 1000) {
                        onSetTrimStart(startMs)
                        onSetTrimEnd(endMs)
                    }
                },
                valueRange = 0f..1f
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle(icon = Icons.Filled.CropFree, title = "Modo de escala")
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ScaleModeChip(
                label = "Estirar",
                description = "Cubre toda la pantalla",
                isSelected = scaleMode == ScaleMode.STRETCH,
                onClick = { onSetScaleMode(ScaleMode.STRETCH) },
                modifier = Modifier.weight(1f)
            )
            ScaleModeChip(
                label = "Recortar",
                description = "Centro sin distorsión",
                isSelected = scaleMode == ScaleMode.CENTER_CROP,
                onClick = { onSetScaleMode(ScaleMode.CENTER_CROP) },
                modifier = Modifier.weight(1f)
            )
            ScaleModeChip(
                label = "Ajustar",
                description = "Tamaño original",
                isSelected = scaleMode == ScaleMode.ORIGINAL,
                onClick = { onSetScaleMode(ScaleMode.ORIGINAL) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle(icon = Icons.Filled.ColorLens, title = "Color de fondo")
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PRESET_COLORS.forEach { (color, name) ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(color))
                        .border(
                            width = if (backgroundColor == color) 3.dp else 1.dp,
                            color = if (backgroundColor == color) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        )
                        .clickable { onSetBackgroundColor(color) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onApply,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Aplicar wallpaper",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

@Composable
private fun SectionTitle(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ControlButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SpeedChip(
    speed: Float,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${speed}x",
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ScaleModeChip(
    label: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = description,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun VideoPreview(
    wallpaper: Wallpaper,
    isPlaying: Boolean,
    isMuted: Boolean,
    loopEnabled: Boolean,
    playbackSpeed: Float,
    trimStartMs: Long,
    trimEndMs: Long,
    backgroundColor: Long,
    scaleMode: ScaleMode,
    onDurationReady: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val videoUri = remember(wallpaper.uri) { Uri.parse(wallpaper.uri) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val clippingBuilder = MediaItem.ClippingConfiguration.Builder()
            if (trimStartMs > 0) clippingBuilder.setStartPositionMs(trimStartMs)
            if (trimEndMs > 0) clippingBuilder.setEndPositionMs(trimEndMs)

            val mediaItem = MediaItem.Builder()
                .setUri(videoUri)
                .setClippingConfiguration(clippingBuilder.build())
                .build()

            setMediaItem(mediaItem)

            setVideoScalingMode(getExoPlayerScaleMode(scaleMode))

            repeatMode = if (loopEnabled) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
            volume = if (isMuted) 0f else 1f
            playWhenReady = isPlaying
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        onDurationReady(duration)
                    }
                }
            })
            prepare()
        }
    }

    LaunchedEffect(isPlaying) { exoPlayer.playWhenReady = isPlaying }
    LaunchedEffect(isMuted) { exoPlayer.volume = if (isMuted) 0f else 1f }
    LaunchedEffect(loopEnabled) {
        exoPlayer.repeatMode = if (loopEnabled) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
    }
    LaunchedEffect(playbackSpeed) { exoPlayer.playbackParameters = PlaybackParameters(playbackSpeed) }

    LaunchedEffect(trimStartMs, trimEndMs) {
        val clippingBuilder = MediaItem.ClippingConfiguration.Builder()
        if (trimStartMs > 0) clippingBuilder.setStartPositionMs(trimStartMs)
        if (trimEndMs > 0) clippingBuilder.setEndPositionMs(trimEndMs)
        val mediaItem = MediaItem.Builder()
            .setUri(videoUri)
            .setClippingConfiguration(clippingBuilder.build())
            .build()
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
    }

    LaunchedEffect(scaleMode) {
        exoPlayer.setVideoScalingMode(getExoPlayerScaleMode(scaleMode))
    }

    DisposableEffect(Unit) { onDispose { exoPlayer.release() } }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> exoPlayer.playWhenReady = false
                Lifecycle.Event.ON_RESUME -> exoPlayer.playWhenReady = isPlaying
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false
                resizeMode = getExoPlayerResizeMode(scaleMode)
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        update = { playerView ->
            playerView.resizeMode = getExoPlayerResizeMode(scaleMode)
        },
        modifier = modifier.background(Color(backgroundColor))
    )
}

private fun getExoPlayerScaleMode(scaleMode: ScaleMode): Int {
    return when (scaleMode) {
        ScaleMode.STRETCH -> C.VIDEO_SCALING_MODE_SCALE_TO_FIT
        ScaleMode.CENTER_CROP -> C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
        ScaleMode.FIT, ScaleMode.ORIGINAL -> C.VIDEO_SCALING_MODE_SCALE_TO_FIT
    }
}

private fun getExoPlayerResizeMode(scaleMode: ScaleMode): Int {
    return when (scaleMode) {
        ScaleMode.STRETCH -> AspectRatioFrameLayout.RESIZE_MODE_FILL
        ScaleMode.CENTER_CROP -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        ScaleMode.FIT, ScaleMode.ORIGINAL -> AspectRatioFrameLayout.RESIZE_MODE_FIT
    }
}
