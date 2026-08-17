package com.puma.pixelpulse.data.media

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThumbnailGenerator @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    fun generateThumbnail(videoUri: Uri): String? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, videoUri)
            val bitmap = retriever.getFrameAtTime(1_000_000)
            retriever.release()

            if (bitmap != null) {
                saveThumbnail(bitmap)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun saveThumbnail(bitmap: Bitmap): String {
        val dir = File(context.filesDir, "thumbnails")
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, "thumb_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        bitmap.recycle()

        return file.absolutePath
    }

    fun deleteThumbnail(path: String?) {
        path?.let {
            val file = File(it)
            if (file.exists()) file.delete()
        }
    }
}
