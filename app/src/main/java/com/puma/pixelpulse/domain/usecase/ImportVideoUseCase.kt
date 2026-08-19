package com.puma.pixelpulse.domain.usecase

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.puma.pixelpulse.data.local.UserPreferences
import com.puma.pixelpulse.data.media.ThumbnailGenerator
import com.puma.pixelpulse.domain.model.Wallpaper
import com.puma.pixelpulse.domain.model.WallpaperType
import com.puma.pixelpulse.domain.repository.WallpaperRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ImportVideoUseCase @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val repository: WallpaperRepository,
    private val thumbnailGenerator: ThumbnailGenerator
) {

    suspend operator fun invoke(videoUri: Uri): Result<Long> {
        val existing = repository.getByUri(videoUri.toString())
        if (existing != null) {
            return Result.failure(IllegalArgumentException("Video already imported"))
        }

        try {
            context.contentResolver.takePersistableUriPermission(
                videoUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) {
            // Permission may already be granted
        }

        val metadata = extractMetadata(videoUri)
            ?: return Result.failure(IllegalStateException("Cannot read video metadata"))

        val thumbnailPath = thumbnailGenerator.generateThumbnail(videoUri)

        val defaultMuted = UserPreferences.getDefaultMutedOnce(context)
        val defaultSpeed = UserPreferences.getDefaultSpeedOnce(context)
        val defaultLoop = UserPreferences.getDefaultLoopOnce(context)

        val wallpaper = Wallpaper(
            uri = videoUri.toString(),
            name = metadata.name,
            type = WallpaperType.VIDEO,
            thumbnailUri = thumbnailPath,
            duration = metadata.duration,
            width = metadata.width,
            height = metadata.height,
            sizeBytes = metadata.sizeBytes,
            dateAdded = System.currentTimeMillis(),
            dateModified = System.currentTimeMillis(),
            muted = defaultMuted,
            playbackSpeed = defaultSpeed,
            loop = defaultLoop
        )

        val id = repository.insert(wallpaper)
        return Result.success(id)
    }

    private fun extractMetadata(uri: Uri): VideoMetadata? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)

            val name = context.contentResolver.query(
                uri,
                arrayOf(android.provider.OpenableColumns.DISPLAY_NAME),
                null, null, null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(0)?.substringBeforeLast(".")
                } else null
            } ?: "Unknown"

            val duration = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION
            )?.toLongOrNull() ?: 0L

            val width = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH
            )?.toIntOrNull() ?: 0

            val height = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT
            )?.toIntOrNull() ?: 0

            retriever.release()

            val sizeBytes = context.contentResolver.query(
                uri,
                arrayOf(android.provider.OpenableColumns.SIZE),
                null, null, null
            )?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getLong(0) else 0L
            } ?: 0L

            VideoMetadata(name, duration, width, height, sizeBytes)
        } catch (e: Exception) {
            null
        }
    }

    private data class VideoMetadata(
        val name: String,
        val duration: Long,
        val width: Int,
        val height: Int,
        val sizeBytes: Long
    )
}
