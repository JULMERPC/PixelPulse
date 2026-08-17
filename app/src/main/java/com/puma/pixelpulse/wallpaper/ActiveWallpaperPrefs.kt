package com.puma.pixelpulse.wallpaper

import android.content.Context
import org.json.JSONObject
import java.io.File

object ActiveWallpaperPrefs {

    private const val FILE_NAME = "active_wallpaper.json"

    private fun getConfigFile(context: Context): File =
        File(context.filesDir, FILE_NAME)

    fun setActiveWallpaper(
        context: Context,
        wallpaperUri: String,
        thumbnailPath: String?,
        name: String,
        muted: Boolean = true,
        volume: Float = 0f,
        playbackSpeed: Float = 1f,
        loop: Boolean = true,
        trimStartMs: Long = 0L,
        trimEndMs: Long = 0L,
        backgroundColor: Long = 0xFF000000,
        scaleMode: String = "CENTER_CROP"
    ) {
        val json = JSONObject().apply {
            put("wallpaper_uri", wallpaperUri)
            put("thumbnail_path", thumbnailPath ?: "")
            put("wallpaper_name", name)
            put("muted", muted)
            put("volume", volume.toDouble())
            put("playback_speed", playbackSpeed.toDouble())
            put("loop", loop)
            put("trim_start", trimStartMs)
            put("trim_end", trimEndMs)
            put("background_color", backgroundColor)
            put("scale_mode", scaleMode)
        }
        getConfigFile(context).writeText(json.toString())
    }

    private fun readConfig(context: Context): JSONObject {
        val file = getConfigFile(context)
        return if (file.exists()) {
            try {
                JSONObject(file.readText())
            } catch (e: Exception) {
                JSONObject()
            }
        } else {
            JSONObject()
        }
    }

    fun getWallpaperUri(context: Context): String? {
        val value = readConfig(context).optString("wallpaper_uri", "")
        return value.ifEmpty { null }
    }

    fun getThumbnailPath(context: Context): String? {
        val value = readConfig(context).optString("thumbnail_path", "")
        return value.ifEmpty { null }
    }

    fun getWallpaperName(context: Context): String? {
        val value = readConfig(context).optString("wallpaper_name", "")
        return value.ifEmpty { null }
    }

    fun isMuted(context: Context): Boolean =
        readConfig(context).optBoolean("muted", true)

    fun getVolume(context: Context): Float =
        readConfig(context).optDouble("volume", 0.0).toFloat()

    fun getPlaybackSpeed(context: Context): Float =
        readConfig(context).optDouble("playback_speed", 1.0).toFloat()

    fun isLoop(context: Context): Boolean =
        readConfig(context).optBoolean("loop", true)

    fun getTrimStartMs(context: Context): Long =
        readConfig(context).optLong("trim_start", 0L)

    fun getTrimEndMs(context: Context): Long =
        readConfig(context).optLong("trim_end", 0L)

    fun getBackgroundColor(context: Context): Long =
        readConfig(context).optLong("background_color", 0xFF000000L)

    fun getScaleMode(context: Context): String =
        readConfig(context).optString("scale_mode", "CENTER_CROP")

    fun clear(context: Context) {
        val file = getConfigFile(context)
        if (file.exists()) file.delete()
    }
}
