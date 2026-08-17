package com.puma.pixelpulse.domain.model

data class Wallpaper(
    val id: Long = 0,
    val uri: String,
    val name: String,
    val type: WallpaperType,
    val thumbnailUri: String? = null,
    val duration: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
    val sizeBytes: Long = 0L,
    val dateAdded: Long = System.currentTimeMillis(),
    val dateModified: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val playbackSpeed: Float = 1f,
    val scaleMode: ScaleMode = ScaleMode.CENTER_CROP,
    val muted: Boolean = true,
    val lastUsedAt: Long = 0L,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = 0L,
    val backgroundColor: Long = 0xFF000000
)

enum class WallpaperType {
    VIDEO,
    IMAGE,
    SHADER,
    PARTICLE,
    INTERACTIVE
}

enum class ScaleMode {
    FIT,
    CENTER_CROP,
    STRETCH,
    ORIGINAL
}
