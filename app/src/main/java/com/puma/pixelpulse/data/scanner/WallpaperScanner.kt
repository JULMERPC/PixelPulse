package com.puma.pixelpulse.data.scanner

import android.content.Context
import android.provider.MediaStore
import com.puma.pixelpulse.domain.model.Wallpaper
import com.puma.pixelpulse.domain.model.WallpaperType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WallpaperScanner @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    fun scanVideos(): List<Wallpaper> {
        val wallpapers = mutableListOf<Wallpaper>()
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DATE_MODIFIED
        )
        val selection = "${MediaStore.Video.Media.MIME_TYPE} IN (?, ?, ?)"
        val selectionArgs = arrayOf("video/mp4", "video/webm", "video/quicktime")
        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
            val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn) ?: "Unknown"
                val duration = cursor.getLong(durationColumn)
                val width = cursor.getInt(widthColumn)
                val height = cursor.getInt(heightColumn)
                val size = cursor.getLong(sizeColumn)
                val dateAdded = cursor.getLong(dateAddedColumn) * 1000
                val dateModified = cursor.getLong(dateModifiedColumn) * 1000
                val contentUri = MediaStore.Video.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL,
                    id
                )

                wallpapers.add(
                    Wallpaper(
                        uri = contentUri.toString(),
                        name = name.substringBeforeLast("."),
                        type = WallpaperType.VIDEO,
                        duration = duration,
                        width = width,
                        height = height,
                        sizeBytes = size,
                        dateAdded = dateAdded,
                        dateModified = dateModified
                    )
                )
            }
        }

        return wallpapers
    }
}
