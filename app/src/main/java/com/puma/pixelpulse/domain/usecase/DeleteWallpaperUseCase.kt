package com.puma.pixelpulse.domain.usecase

import android.content.Context
import com.puma.pixelpulse.data.media.ThumbnailGenerator
import com.puma.pixelpulse.domain.model.Wallpaper
import com.puma.pixelpulse.domain.repository.WallpaperRepository
import com.puma.pixelpulse.wallpaper.ActiveWallpaperPrefs
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DeleteWallpaperUseCase @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val repository: WallpaperRepository,
    private val thumbnailGenerator: ThumbnailGenerator
) {
    suspend operator fun invoke(wallpaper: Wallpaper) {
        thumbnailGenerator.deleteThumbnail(wallpaper.thumbnailUri)

        val activeUri = ActiveWallpaperPrefs.getWallpaperUri(context)
        if (activeUri == wallpaper.uri) {
            ActiveWallpaperPrefs.clear(context)
        }

        repository.delete(wallpaper)
    }
}
