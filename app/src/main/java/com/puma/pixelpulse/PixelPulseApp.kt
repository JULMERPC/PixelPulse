package com.puma.pixelpulse

import android.app.Application
import com.puma.pixelpulse.util.DebugConfig
import com.puma.pixelpulse.util.PerformanceMonitor
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PixelPulseApp : Application() {

    override fun onCreate() {
        super.onCreate()
        DebugConfig.initialize(this)

        if (DebugConfig.isAnyDebugEnabled()) {
            PerformanceMonitor.start()
        }
    }
}
