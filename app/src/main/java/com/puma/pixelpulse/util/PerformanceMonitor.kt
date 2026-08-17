package com.puma.pixelpulse.util

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import android.util.Log
import java.lang.ref.SoftReference
import java.util.concurrent.CopyOnWriteArrayList

object PerformanceMonitor {

    private const val TAG = "PerformanceMonitor"
    private const val FPS_UPDATE_INTERVAL_MS = 500L
    private const val MEMORY_UPDATE_INTERVAL_MS = 1000L

    private var monitorThread: HandlerThread? = null
    private var monitorHandler: Handler? = null

    private val fpsListeners = CopyOnWriteArrayList<SoftReference<(Float) -> Unit>>()
    private val memoryListeners = CopyOnWriteArrayList<SoftReference<(MemoryStats) -> Unit>>()

    private var lastFrameTime = 0L
    private var frameCount = 0
    private var currentFps = 0f

    private var isRunning = false

    data class MemoryStats(
        val heapUsedMb: Float,
        val heapMaxMb: Float,
        val nativeUsedMb: Float,
        val totalUsedMb: Float,
        val availableMb: Float
    )

    fun start() {
        if (isRunning) return
        isRunning = true

        monitorThread = HandlerThread("PerformanceMonitor").apply { start() }
        monitorHandler = Handler(monitorThread!!.looper)

        startFpsTracking()
        startMemoryTracking()

        Log.d(TAG, "Performance monitor started")
    }

    fun stop() {
        isRunning = false
        monitorThread?.quitSafely()
        monitorThread = null
        monitorHandler = null
        fpsListeners.clear()
        memoryListeners.clear()
        Log.d(TAG, "Performance monitor stopped")
    }

    fun onFrameRendered() {
        if (!isRunning) return
        val now = SystemClock.elapsedRealtime()
        if (lastFrameTime == 0L) {
            lastFrameTime = now
            return
        }
        frameCount++
    }

    fun addFpsListener(listener: (Float) -> Unit) {
        fpsListeners.add(SoftReference(listener))
    }

    fun removeFpsListener(listener: (Float) -> Unit) {
        fpsListeners.removeAll { it.get() === listener }
    }

    fun addMemoryListener(listener: (MemoryStats) -> Unit) {
        memoryListeners.add(SoftReference(listener))
    }

    fun removeMemoryListener(listener: (MemoryStats) -> Unit) {
        memoryListeners.removeAll { it.get() === listener }
    }

    fun getCurrentFps(): Float = currentFps

    fun getMemoryStats(context: Context): MemoryStats {
        val runtime = Runtime.getRuntime()
        val heapUsed = (runtime.totalMemory() - runtime.freeMemory()) / (1024f * 1024f)
        val heapMax = runtime.maxMemory() / (1024f * 1024f)

        val nativeHeapUsed = Debug.getNativeHeapAllocatedSize() / (1024f * 1024f)

        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        val availableMb = memInfo.availMem / (1024f * 1024f)

        return MemoryStats(
            heapUsedMb = heapUsed,
            heapMaxMb = heapMax,
            nativeUsedMb = nativeHeapUsed,
            totalUsedMb = heapUsed + nativeHeapUsed,
            availableMb = availableMb
        )
    }

    private fun getMemoryStatsRuntime(): MemoryStats {
        val runtime = Runtime.getRuntime()
        val heapUsed = (runtime.totalMemory() - runtime.freeMemory()) / (1024f * 1024f)
        val heapMax = runtime.maxMemory() / (1024f * 1024f)

        val nativeHeapUsed = Debug.getNativeHeapAllocatedSize() / (1024f * 1024f)

        return MemoryStats(
            heapUsedMb = heapUsed,
            heapMaxMb = heapMax,
            nativeUsedMb = nativeHeapUsed,
            totalUsedMb = heapUsed + nativeHeapUsed,
            availableMb = 0f
        )
    }

    private fun startFpsTracking() {
        monitorHandler?.post(object : Runnable {
            override fun run() {
                if (!isRunning) return

                val now = SystemClock.elapsedRealtime()
                val elapsed = now - lastFrameTime

                if (elapsed >= FPS_UPDATE_INTERVAL_MS) {
                    currentFps = if (elapsed > 0) {
                        (frameCount * 1000f) / elapsed
                    } else {
                        0f
                    }

                    frameCount = 0
                    lastFrameTime = now

                    notifyFpsListeners(currentFps)

                    if (DebugConfig.logPerformance) {
                        Log.d(TAG, "FPS: %.1f".format(currentFps))
                    }
                }

                monitorHandler?.postDelayed(this, FPS_UPDATE_INTERVAL_MS)
            }
        })
    }

    private fun startMemoryTracking() {
        monitorHandler?.post(object : Runnable {
            override fun run() {
                if (!isRunning) return

                val stats = getMemoryStatsRuntime()
                notifyMemoryListeners(stats)

                if (DebugConfig.logPerformance) {
                    Log.d(TAG, "Memory: Heap=%.1f/%.1fMB, Native=%.1fMB, Available=%.0fMB".format(
                        stats.heapUsedMb, stats.heapMaxMb, stats.nativeUsedMb, stats.availableMb
                    ))
                }

                monitorHandler?.postDelayed(this, MEMORY_UPDATE_INTERVAL_MS)
            }
        })
    }

    private fun notifyFpsListeners(fps: Float) {
        val iterator = fpsListeners.iterator()
        while (iterator.hasNext()) {
            val ref = iterator.next()
            val listener = ref.get()
            if (listener != null) {
                listener(fps)
            } else {
                iterator.remove()
            }
        }
    }

    private fun notifyMemoryListeners(stats: MemoryStats) {
        val iterator = memoryListeners.iterator()
        while (iterator.hasNext()) {
            val ref = iterator.next()
            val listener = ref.get()
            if (listener != null) {
                listener(stats)
            } else {
                iterator.remove()
            }
        }
    }
}
