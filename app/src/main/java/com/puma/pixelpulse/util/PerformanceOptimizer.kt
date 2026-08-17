package com.puma.pixelpulse.util

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.LruCache
import java.io.File
import java.lang.ref.SoftReference
import kotlin.math.min

object PerformanceOptimizer {

    private const val TAG = "PerformanceOptimizer"

    private const val TARGET_FPS = 30
    private const val FRAME_INTERVAL_MS = 1000L / TARGET_FPS

    private const val MAX_BITMAP_CACHE_SIZE_KB = 16 * 1024
    private const val MAX_THUMBNAIL_CACHE_SIZE = 5

    private val bitmapCache = object : LruCache<String, Bitmap>(MAX_BITMAP_CACHE_SIZE_KB) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    private var decodeThread: HandlerThread? = null
    private var decodeHandler: Handler? = null

    fun initialize() {
        decodeThread = HandlerThread("BitmapDecodeThread").apply { start() }
        decodeHandler = Handler(decodeThread!!.looper)
        Log.d(TAG, "Initialized decode thread")
    }

    fun shutdown() {
        decodeThread?.quitSafely()
        decodeThread = null
        decodeHandler = null
        bitmapCache.evictAll()
        Log.d(TAG, "Shutdown decode thread and cleared cache")
    }

    fun getOptimalBitmapSampleSize(
        file: File,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(file.absolutePath, options)

        var sampleSize = 1
        while (options.outHeight / sampleSize / 2 >= reqHeight &&
            options.outWidth / sampleSize / 2 >= reqWidth
        ) {
            sampleSize *= 2
        }

        return sampleSize
    }

    fun decodeBitmapFromFile(
        file: File,
        reqWidth: Int,
        reqHeight: Int,
        useCache: Boolean = true
    ): Bitmap? {
        val cacheKey = "${file.absolutePath}_${reqWidth}_${reqHeight}"

        if (useCache) {
            bitmapCache.get(cacheKey)?.let {
                Log.d(TAG, "Bitmap cache hit: $cacheKey")
                return it
            }
        }

        try {
            val sampleSize = getOptimalBitmapSampleSize(file, reqWidth, reqHeight)
            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }

            val bitmap = BitmapFactory.decodeFile(file.absolutePath, options) ?: return null

            if (useCache) {
                bitmapCache.put(cacheKey, bitmap)
                Log.d(TAG, "Cached bitmap: $cacheKey (${bitmap.byteCount / 1024}KB)")
            }

            return bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding bitmap", e)
            return null
        }
    }

    fun decodeBitmapAsync(
        file: File,
        reqWidth: Int,
        reqHeight: Int,
        onBitmapReady: (Bitmap?) -> Unit
    ) {
        decodeHandler?.post {
            val bitmap = decodeBitmapFromFile(file, reqWidth, reqHeight)
            onBitmapReady(bitmap)
        } ?: run {
            val bitmap = decodeBitmapFromFile(file, reqWidth, reqHeight)
            onBitmapReady(bitmap)
        }
    }

    fun isBatteryLow(context: Context): Boolean {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        return level <= 20
    }

    fun getBatteryLevel(context: Context): Int {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    fun getAvailableMemoryKb(context: Context): Long {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        return memInfo.availMem / 1024
    }

    fun shouldReduceQuality(context: Context): Boolean {
        return isBatteryLow(context) || getAvailableMemoryKb(context) < 100 * 1024
    }

    fun getScaledDimensions(
        originalWidth: Int,
        originalHeight: Int,
        maxWidth: Int,
        maxHeight: Int
    ): Pair<Int, Int> {
        val ratio = min(
            maxWidth.toFloat() / originalWidth.toFloat(),
            maxHeight.toFloat() / originalHeight.toFloat()
        )

        if (ratio >= 1f) {
            return Pair(originalWidth, originalHeight)
        }

        return Pair(
            (originalWidth * ratio).toInt(),
            (originalHeight * ratio).toInt()
        )
    }

    fun recycleBitmapSafe(bitmap: Bitmap?) {
        bitmap?.let {
            if (!it.isRecycled) {
                it.recycle()
            }
        }
    }

    fun getFrameIntervalMs(): Long = FRAME_INTERVAL_MS

    fun getTargetFps(): Int = TARGET_FPS
}
