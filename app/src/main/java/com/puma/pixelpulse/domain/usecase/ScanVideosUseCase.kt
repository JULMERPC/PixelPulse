package com.puma.pixelpulse.domain.usecase

import com.puma.pixelpulse.data.scanner.WallpaperScanner
import com.puma.pixelpulse.domain.model.Wallpaper
import com.puma.pixelpulse.domain.repository.WallpaperRepository
import javax.inject.Inject

class ScanVideosUseCase @Inject constructor(
    private val scanner: WallpaperScanner,
    private val repository: WallpaperRepository
) {

    suspend operator fun invoke(): Result<Int> {
        return try {
            val scanned = scanner.scanVideos()
            var imported = 0

            for (wallpaper in scanned) {
                val existing = repository.getByUri(wallpaper.uri)
                if (existing == null) {
                    repository.insert(wallpaper)
                    imported++
                }
            }

            Result.success(imported)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
