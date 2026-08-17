package com.puma.pixelpulse.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.puma.pixelpulse.util.DebugConfig
import com.puma.pixelpulse.util.PerformanceMonitor

@Composable
fun DebugOverlay(modifier: Modifier = Modifier) {
    if (!DebugConfig.isAnyDebugEnabled()) return

    val context = LocalContext.current
    var fps by remember { mutableFloatStateOf(0f) }
    var memoryStats by remember { mutableStateOf<PerformanceMonitor.MemoryStats?>(null) }

    DisposableEffect(Unit) {
        val fpsListener: (Float) -> Unit = { newFps -> fps = newFps }
        val memoryListener: (PerformanceMonitor.MemoryStats) -> Unit = { stats -> memoryStats = stats }

        PerformanceMonitor.addFpsListener(fpsListener)
        PerformanceMonitor.addMemoryListener(memoryListener)

        onDispose {
            PerformanceMonitor.removeFpsListener(fpsListener)
            PerformanceMonitor.removeMemoryListener(memoryListener)
        }
    }

    Column(
        modifier = modifier
            .background(
                Color.Black.copy(alpha = 0.7f),
                RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        if (DebugConfig.showFps) {
            DebugText("FPS: %.1f".format(fps))
        }

        if (DebugConfig.showMemory) {
            memoryStats?.let { stats ->
                DebugText("Heap: %.1f/%.1f MB".format(stats.heapUsedMb, stats.heapMaxMb))
                DebugText("Native: %.1f MB".format(stats.nativeUsedMb))
                DebugText("Free: %.0f MB".format(stats.availableMb))
            }
        }
    }
}

@Composable
private fun DebugText(text: String) {
    Text(
        text = text,
        color = Color.Green,
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier.padding(vertical = 1.dp)
    )
}
