package com.puma.pixelpulse.wallpaper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import com.puma.pixelpulse.domain.model.ScaleMode
import com.puma.pixelpulse.util.DebugConfig
import com.puma.pixelpulse.util.PerformanceMonitor
import com.puma.pixelpulse.util.PerformanceOptimizer
import java.io.File

class PixelPulseWallpaperService : WallpaperService() {

    companion object {
        private const val TAG = "PixelPulseWallpaper"
    }

    override fun onCreateEngine(): Engine = WallpaperEngine()

    inner class WallpaperEngine : Engine() {

        private var surfaceHolder: SurfaceHolder? = null
        private var exoPlayer: ExoPlayer? = null
        private var visible = false
        private var bitmap: Bitmap? = null
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true
        }

        private var lastFrameTime = 0L
        private var frameIntervalMs = PerformanceOptimizer.getFrameIntervalMs()

        private var cachedSurfaceWidth = 0
        private var cachedSurfaceHeight = 0

        private var engineCreateTime = 0L
        private var isPlayerPrepared = false
        private var isPlayerStarted = false
        private var useCanvasRendering = false

        private var backgroundColor = Color.BLACK
        private var scaleMode = ScaleMode.CENTER_CROP

        private var lastAppliedScaleMode: ScaleMode? = null
        private var lastAppliedBgColor = Color.BLACK
        private var lastAppliedUri = ""
        private var canvasReady = false

        private val mainHandler = Handler(Looper.getMainLooper())
        private var trimEndRunnable: Runnable? = null

        private var videoWidth = 0
        private var videoHeight = 0

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            engineCreateTime = System.currentTimeMillis()
            PerformanceOptimizer.initialize()
            DebugConfig.initialize(applicationContext)
            log("Engine created")
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            surfaceHolder = holder
            canvasReady = false
            log("Surface created")
            loadSettings()
            log("Surface created: waiting for onSurfaceChanged for dimensions")
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            surfaceHolder = holder
            cachedSurfaceWidth = width
            cachedSurfaceHeight = height
            log("Surface changed: ${width}x${height}")

            val needsInit = exoPlayer == null ||
                    lastAppliedScaleMode != scaleMode ||
                    lastAppliedUri != (ActiveWallpaperPrefs.getWallpaperUri(applicationContext) ?: "") ||
                    lastAppliedBgColor != backgroundColor

            if (needsInit) {
                log("Initializing player on surfaceChanged (dimensions now available)")
                lastAppliedScaleMode = scaleMode
                lastAppliedBgColor = backgroundColor
                lastAppliedUri = ActiveWallpaperPrefs.getWallpaperUri(applicationContext) ?: ""
                canvasReady = false
                initializePlayer()
            } else if (useCanvasRendering && canvasReady) {
                log("Canvas mode, re-rendering existing bitmap")
                mainHandler.post { renderThumbnail() }
            } else if (exoPlayer != null) {
                if (isPlayerPrepared) {
                    log("Resuming player")
                    resumePlayback()
                }
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            log("Visibility changed: $visible")
            if (visible) {
                loadSettings()
                val currentUri = ActiveWallpaperPrefs.getWallpaperUri(applicationContext) ?: ""
                val settingsChanged = scaleMode != lastAppliedScaleMode ||
                        backgroundColor != lastAppliedBgColor ||
                        currentUri != lastAppliedUri

                if (settingsChanged) {
                    log("Settings changed - reinit. scaleMode=$scaleMode (was $lastAppliedScaleMode)")
                    lastAppliedScaleMode = scaleMode
                    lastAppliedBgColor = backgroundColor
                    lastAppliedUri = currentUri
                    canvasReady = false
                    initializePlayer()
                } else if (useCanvasRendering && canvasReady) {
                    log("Canvas mode, re-rendering")
                    mainHandler.post { renderThumbnail() }
                } else if (exoPlayer != null) {
                    if (isPlayerPrepared) {
                        log("Resuming player")
                        resumePlayback()
                    }
                } else {
                    log("No player, reinit")
                    canvasReady = false
                    initializePlayer()
                }
            } else {
                pausePlayback()
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            visible = false
            canvasReady = false
            releasePlayer()
            surfaceHolder = null
            lastAppliedScaleMode = null
            lastAppliedUri = ""
            videoWidth = 0
            videoHeight = 0
            log("Surface destroyed")
        }

        override fun onDestroy() {
            super.onDestroy()
            releasePlayer()
            clearThumbnail()
            PerformanceOptimizer.shutdown()
            log("Engine destroyed, lifetime: ${System.currentTimeMillis() - engineCreateTime}ms")
        }

        private fun loadSettings() {
            val context = applicationContext
            backgroundColor = ActiveWallpaperPrefs.getBackgroundColor(context).toInt()
            val modeStr = ActiveWallpaperPrefs.getScaleMode(context)
            scaleMode = try {
                ScaleMode.valueOf(modeStr)
            } catch (e: IllegalArgumentException) {
                Log.w(TAG, "Unknown scaleMode '$modeStr', defaulting to CENTER_CROP")
                ScaleMode.CENTER_CROP
            }
            if (scaleMode == ScaleMode.FIT) {
                scaleMode = ScaleMode.ORIGINAL
            }
            log("Settings loaded: scaleMode=$scaleMode")
        }

        private fun initializePlayer() {
            releasePlayer()
            useCanvasRendering = false

            when (scaleMode) {
                ScaleMode.STRETCH -> {
                    log("STRETCH mode: Canvas rendering with ExoPlayer decoded frames")
                    useCanvasRendering = true
                    isPlayerPrepared = false
                    initializeExoPlayerForCanvas()
                }
                ScaleMode.ORIGINAL -> {
                    log("ORIGINAL mode: ExoPlayer with SCALE_TO_FIT")
                    initializeExoPlayerForSurface()
                }
                ScaleMode.FIT -> {
                    log("FIT mode: ExoPlayer with SCALE_TO_FIT")
                    initializeExoPlayerForSurface()
                }
                ScaleMode.CENTER_CROP -> {
                    log("CENTER_CROP mode: ExoPlayer with SCALE_TO_FIT_WITH_CROPPING")
                    initializeExoPlayerForSurface()
                }
            }
        }

        private fun initializeExoPlayerForSurface() {
            val context = applicationContext
            val videoUri = ActiveWallpaperPrefs.getWallpaperUri(context) ?: run {
                log("No wallpaper URI, falling back to canvas")
                fallBackToCanvas()
                return
            }
            val holder = surfaceHolder ?: run {
                log("No surface holder available, falling back to canvas")
                fallBackToCanvas()
                return
            }

            isPlayerPrepared = false
            isPlayerStarted = false

            log("Initializing ExoPlayer: uri=$videoUri, scaleMode=$scaleMode")

            try {
                val player = ExoPlayer.Builder(context).build()

                player.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_READY -> {
                                isPlayerPrepared = true
                                log("ExoPlayer STATE_READY, took ${System.currentTimeMillis() - engineCreateTime}ms")

                                val trimStart = ActiveWallpaperPrefs.getTrimStartMs(context)
                                if (trimStart > 0) {
                                    player.seekTo(trimStart)
                                    log("Seeked to trim start: ${trimStart}ms")
                                }

                                val isMuted = ActiveWallpaperPrefs.isMuted(context)
                                val baseVolume = ActiveWallpaperPrefs.getVolume(context)
                                val vol = if (isMuted) 0f else baseVolume.coerceAtLeast(0.1f)
                                player.volume = vol

                                val speed = ActiveWallpaperPrefs.getPlaybackSpeed(context)
                                if (speed != 1.0f) {
                                    player.playbackParameters = PlaybackParameters(speed)
                                }

                                player.play()
                                isPlayerStarted = true
                                log("ExoPlayer started (scaleMode=$scaleMode)")

                                val isLooping = ActiveWallpaperPrefs.isLoop(context)
                                if (!isLooping) {
                                    setupTrimEndListener(player)
                                }
                            }
                            Player.STATE_ENDED -> {
                                log("ExoPlayer STATE_ENDED")
                                isPlayerStarted = false
                                val isLooping = ActiveWallpaperPrefs.isLoop(context)
                                if (isLooping) {
                                    val trimStart = ActiveWallpaperPrefs.getTrimStartMs(context)
                                    player.seekTo(if (trimStart > 0) trimStart else 0L)
                                    player.play()
                                    isPlayerStarted = true
                                }
                            }
                        }
                    }

                    override fun onVideoSizeChanged(videoSize: VideoSize) {
                        videoWidth = videoSize.width
                        videoHeight = videoSize.height
                        log("Video size: ${videoWidth}x${videoHeight}")
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        log("ExoPlayer error: ${error.message}")
                        fallBackToCanvas()
                    }
                })

                val clippingBuilder = MediaItem.ClippingConfiguration.Builder()
                val trimStart = ActiveWallpaperPrefs.getTrimStartMs(context)
                val trimEnd = ActiveWallpaperPrefs.getTrimEndMs(context)
                if (trimStart > 0) clippingBuilder.setStartPositionMs(trimStart)
                if (trimEnd > 0) clippingBuilder.setEndPositionMs(trimEnd)

                val mediaItem = MediaItem.Builder()
                    .setUri(android.net.Uri.parse(videoUri))
                    .setClippingConfiguration(clippingBuilder.build())
                    .build()

                player.setMediaItem(mediaItem)
                player.setVideoSurfaceHolder(holder)

                when (scaleMode) {
                    ScaleMode.CENTER_CROP -> {
                        player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
                        log("Set VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING")
                    }
                    else -> {
                        player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT)
                        log("Set VIDEO_SCALING_MODE_SCALE_TO_FIT")
                    }
                }

                player.repeatMode = Player.REPEAT_MODE_OFF
                player.prepare()

                exoPlayer = player
                log("ExoPlayer initialized (async)")

            } catch (e: Exception) {
                log("ExoPlayer init failed: ${e.message}")
                fallBackToCanvas()
            }
        }

        private fun initializeExoPlayerForCanvas() {
            val context = applicationContext
            val videoUri = ActiveWallpaperPrefs.getWallpaperUri(context) ?: run {
                log("No wallpaper URI for canvas mode")
                return
            }
            val holder = surfaceHolder ?: run {
                log("No surface holder for canvas mode")
                return
            }

            isPlayerPrepared = false
            isPlayerStarted = false

            log("Initializing ExoPlayer for Canvas rendering: uri=$videoUri")

            try {
                val player = ExoPlayer.Builder(context).build()

                player.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_READY -> {
                                isPlayerPrepared = true
                                log("ExoPlayer Canvas: STATE_READY")

                                val trimStart = ActiveWallpaperPrefs.getTrimStartMs(context)
                                if (trimStart > 0) {
                                    player.seekTo(trimStart)
                                }

                                val isMuted = ActiveWallpaperPrefs.isMuted(context)
                                val baseVolume = ActiveWallpaperPrefs.getVolume(context)
                                val vol = if (isMuted) 0f else baseVolume.coerceAtLeast(0.1f)
                                player.volume = vol

                                val speed = ActiveWallpaperPrefs.getPlaybackSpeed(context)
                                if (speed != 1.0f) {
                                    player.playbackParameters = PlaybackParameters(speed)
                                }

                                player.play()
                                isPlayerStarted = true
                                log("ExoPlayer Canvas: started")

                                val isLooping = ActiveWallpaperPrefs.isLoop(context)
                                if (!isLooping) {
                                    setupTrimEndListener(player)
                                }
                            }
                            Player.STATE_ENDED -> {
                                isPlayerStarted = false
                                val isLooping = ActiveWallpaperPrefs.isLoop(context)
                                if (isLooping) {
                                    val trimStart = ActiveWallpaperPrefs.getTrimStartMs(context)
                                    player.seekTo(if (trimStart > 0) trimStart else 0L)
                                    player.play()
                                    isPlayerStarted = true
                                }
                            }
                        }
                    }

                    override fun onVideoSizeChanged(videoSize: VideoSize) {
                        videoWidth = videoSize.width
                        videoHeight = videoSize.height
                        log("Canvas: Video size: ${videoWidth}x${videoHeight}")
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        log("ExoPlayer Canvas error: ${error.message}")
                    }
                })

                val clippingBuilder = MediaItem.ClippingConfiguration.Builder()
                val trimStart = ActiveWallpaperPrefs.getTrimStartMs(context)
                val trimEnd = ActiveWallpaperPrefs.getTrimEndMs(context)
                if (trimStart > 0) clippingBuilder.setStartPositionMs(trimStart)
                if (trimEnd > 0) clippingBuilder.setEndPositionMs(trimEnd)

                val mediaItem = MediaItem.Builder()
                    .setUri(android.net.Uri.parse(videoUri))
                    .setClippingConfiguration(clippingBuilder.build())
                    .build()

                player.setMediaItem(mediaItem)
                player.setVideoSurfaceHolder(holder)

                player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT)
                player.repeatMode = Player.REPEAT_MODE_OFF
                player.prepare()

                exoPlayer = player
                log("ExoPlayer Canvas initialized")

            } catch (e: Exception) {
                log("ExoPlayer Canvas init failed: ${e.message}")
            }
        }

        private fun fallBackToCanvas() {
            useCanvasRendering = true
            isPlayerPrepared = false
            isPlayerStarted = false
            loadThumbnailForCanvas()
        }

        private fun setupTrimEndListener(player: ExoPlayer) {
            val trimEndMs = ActiveWallpaperPrefs.getTrimEndMs(applicationContext)
            if (trimEndMs <= 0) return

            val runnable = object : Runnable {
                override fun run() {
                    if (exoPlayer == null || !isPlayerPrepared) return
                    try {
                        val pos = player.currentPosition
                        if (pos >= trimEndMs) {
                            val trimStart = ActiveWallpaperPrefs.getTrimStartMs(applicationContext)
                            player.seekTo(if (trimStart > 0) trimStart else 0)
                            log("Trim end reached, looping from ${trimStart}ms")
                        }
                        if (exoPlayer != null) {
                            mainHandler.postDelayed(this, 100)
                        }
                    } catch (e: Exception) {
                        log("Trim check error: ${e.message}")
                    }
                }
            }
            trimEndRunnable = runnable
            mainHandler.postDelayed(runnable, 100)
        }

        private fun resumePlayback() {
            if (!isPlayerPrepared) return
            try {
                if (exoPlayer?.playbackState == Player.STATE_READY && exoPlayer?.isPlaying != true) {
                    exoPlayer?.play()
                    isPlayerStarted = true
                    log("Player resumed")
                }
            } catch (e: Exception) {
                log("Resume error: ${e.message}")
            }
        }

        private fun pausePlayback() {
            if (!isPlayerPrepared) return
            try {
                if (exoPlayer?.isPlaying == true) {
                    exoPlayer?.pause()
                    isPlayerStarted = false
                    log("Player paused")
                }
            } catch (e: Exception) {
                log("Pause error: ${e.message}")
            }
        }

        private fun releasePlayer() {
            isPlayerPrepared = false
            isPlayerStarted = false
            trimEndRunnable?.let { mainHandler.removeCallbacks(it) }
            trimEndRunnable = null
            try {
                exoPlayer?.release()
            } catch (e: Exception) {
                log("Release error: ${e.message}")
            }
            exoPlayer = null
        }

        private fun loadThumbnailForCanvas() {
            val thumbnailPath = ActiveWallpaperPrefs.getThumbnailPath(applicationContext) ?: run {
                log("No thumbnail path set")
                return
            }
            val file = File(thumbnailPath)
            if (!file.exists()) {
                log("Thumbnail file not found: $thumbnailPath")
                return
            }

            val targetWidth = if (cachedSurfaceWidth > 0) cachedSurfaceWidth else 1920
            val targetHeight = if (cachedSurfaceHeight > 0) cachedSurfaceHeight else 1080

            val decodeStart = System.currentTimeMillis()
            PerformanceOptimizer.decodeBitmapAsync(file, targetWidth, targetHeight) { decodedBitmap ->
                val decodeTime = System.currentTimeMillis() - decodeStart
                log("Thumbnail decoded in ${decodeTime}ms, size: ${decodedBitmap?.width}x${decodedBitmap?.height}")

                bitmap?.let { PerformanceOptimizer.recycleBitmapSafe(it) }
                bitmap = decodedBitmap
                canvasReady = true
                if (decodedBitmap != null && !decodedBitmap.isRecycled && surfaceHolder != null) {
                    mainHandler.post { renderThumbnail() }
                }
            }
        }

        private fun clearThumbnail() {
            bitmap?.let { PerformanceOptimizer.recycleBitmapSafe(it) }
            bitmap = null
        }

        private fun renderThumbnail() {
            if (!useCanvasRendering) return

            val now = System.currentTimeMillis()
            if (now - lastFrameTime < frameIntervalMs) return
            lastFrameTime = now

            PerformanceMonitor.onFrameRendered()

            val holder = surfaceHolder
            if (holder == null) { log("renderThumbnail: surfaceHolder is null"); return }
            val surface = holder.surface
            if (surface == null || !surface.isValid) { log("renderThumbnail: surface is null or invalid"); return }

            val canvas = try { surface.lockCanvas(null) } catch (e: Exception) { null }
            if (canvas == null) { log("renderThumbnail: lockCanvas returned null"); return }
            try {
                val canvasWidth = canvas.width
                val canvasHeight = canvas.height

                if (canvasWidth <= 0 || canvasHeight <= 0) {
                    log("renderThumbnail: invalid canvas dimensions ${canvasWidth}x${canvasHeight}")
                    return
                }

                canvas.drawColor(backgroundColor)

                val bmp = bitmap
                if (bmp == null || bmp.isRecycled) {
                    log("renderThumbnail: bitmap null or recycled")
                    return
                }

                val destRect = WallpaperScaleTransform.calculateDestRect(
                    contentWidth = bmp.width,
                    contentHeight = bmp.height,
                    surfaceWidth = canvasWidth,
                    surfaceHeight = canvasHeight,
                    scaleMode = scaleMode
                )

                canvas.drawBitmap(bmp, null, destRect, paint)
                log("renderThumbnail: OK ${canvasWidth}x${canvasHeight}, bmp=${bmp.width}x${bmp.height}, dest=$destRect, mode=$scaleMode")
            } finally {
                try {
                    surface.unlockCanvasAndPost(canvas)
                } catch (e: Exception) {
                    Log.e(TAG, "Error unlocking canvas", e)
                }
            }
        }

        private fun log(message: String) {
            Log.d(TAG, message)
        }
    }
}
