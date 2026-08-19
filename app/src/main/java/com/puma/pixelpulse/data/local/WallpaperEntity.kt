package com.puma.pixelpulse.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallpapers")
data class WallpaperEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uri: String,
    val name: String,
    val type: String,
    val thumbnailUri: String? = null,
    val duration: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
    val sizeBytes: Long = 0L,
    val dateAdded: Long = System.currentTimeMillis(),
    val dateModified: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val playbackSpeed: Float = 1f,
    val scaleMode: String = "CENTER_CROP",
    val muted: Boolean = true,
    val loop: Boolean = true,
    val lastUsedAt: Long = 0L,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = 0L,
    val backgroundColor: Long = 0xFF000000
)
