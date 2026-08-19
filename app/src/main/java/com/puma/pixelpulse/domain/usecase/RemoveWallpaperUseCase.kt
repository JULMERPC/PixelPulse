package com.puma.pixelpulse.domain.usecase

import android.app.WallpaperManager
import android.content.Context
import com.puma.pixelpulse.wallpaper.ActiveWallpaperPrefs
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class RemoveWallpaperUseCase @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    operator fun invoke() {
        ActiveWallpaperPrefs.clear(context)

        val wallpaperManager = WallpaperManager.getInstance(context)
        wallpaperManager.clear()
    }
}
