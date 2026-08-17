package com.puma.pixelpulse.util

import android.content.Context
import android.content.SharedPreferences

object DebugConfig {

    private const val PREFS_NAME = "debug_config"
    private const val KEY_DEBUG_MODE = "debug_mode"
    private const val KEY_SHOW_FPS = "show_fps"
    private const val KEY_SHOW_MEMORY = "show_memory"
    private const val KEY_LOG_PERFORMANCE = "log_performance"

    private var debugPrefs: SharedPreferences? = null

    fun initialize(context: Context) {
        debugPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var isDebugMode: Boolean
        get() = debugPrefs?.getBoolean(KEY_DEBUG_MODE, false) ?: false
        set(value) {
            debugPrefs?.edit()?.putBoolean(KEY_DEBUG_MODE, value)?.apply()
        }

    var showFps: Boolean
        get() = debugPrefs?.getBoolean(KEY_SHOW_FPS, false) ?: false
        set(value) {
            debugPrefs?.edit()?.putBoolean(KEY_SHOW_FPS, value)?.apply()
        }

    var showMemory: Boolean
        get() = debugPrefs?.getBoolean(KEY_SHOW_MEMORY, false) ?: false
        set(value) {
            debugPrefs?.edit()?.putBoolean(KEY_SHOW_MEMORY, value)?.apply()
        }

    var logPerformance: Boolean
        get() = debugPrefs?.getBoolean(KEY_LOG_PERFORMANCE, false) ?: false
        set(value) {
            debugPrefs?.edit()?.putBoolean(KEY_LOG_PERFORMANCE, value)?.apply()
        }

    fun isAnyDebugEnabled(): Boolean {
        return isDebugMode || showFps || showMemory || logPerformance
    }
}
