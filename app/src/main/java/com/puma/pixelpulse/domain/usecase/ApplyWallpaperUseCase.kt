package com.puma.pixelpulse.domain.usecase

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.puma.pixelpulse.domain.model.Wallpaper
import com.puma.pixelpulse.domain.repository.WallpaperRepository
import com.puma.pixelpulse.wallpaper.ActiveWallpaperPrefs
import com.puma.pixelpulse.wallpaper.PixelPulseWallpaperService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

class ApplyWallpaperUseCase @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val repository: WallpaperRepository
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    operator fun invoke(wallpaper: Wallpaper): Intent {
        scope.launch {
            repository.markAsUsed(wallpaper.id)
        }

        ActiveWallpaperPrefs.setActiveWallpaper(
            context = context,
            wallpaperUri = wallpaper.uri,
            thumbnailPath = wallpaper.thumbnailUri,
            name = wallpaper.name,
            muted = wallpaper.muted,
            volume = if (wallpaper.muted) 0f else 1f,
            playbackSpeed = wallpaper.playbackSpeed,
            loop = true,
            trimStartMs = wallpaper.trimStartMs,
            trimEndMs = wallpaper.trimEndMs,
            backgroundColor = wallpaper.backgroundColor,
            scaleMode = wallpaper.scaleMode.name
        )

        val componentName = ComponentName(context, PixelPulseWallpaperService::class.java)

        return Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
            putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                componentName
            )
        }
    }
}
