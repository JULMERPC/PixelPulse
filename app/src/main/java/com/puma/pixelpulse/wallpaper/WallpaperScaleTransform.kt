package com.puma.pixelpulse.wallpaper

import android.graphics.RectF
import com.puma.pixelpulse.domain.model.ScaleMode

object WallpaperScaleTransform {

    data class Transform(
        val destRect: RectF,
        val scaleX: Float,
        val scaleY: Float
    )

    fun calculate(
        contentWidth: Int,
        contentHeight: Int,
        surfaceWidth: Int,
        surfaceHeight: Int,
        scaleMode: ScaleMode
    ): Transform {
        if (contentWidth <= 0 || contentHeight <= 0 ||
            surfaceWidth <= 0 || surfaceHeight <= 0
        ) {
            return Transform(
                destRect = RectF(0f, 0f, surfaceWidth.toFloat(), surfaceHeight.toFloat()),
                scaleX = 1f,
                scaleY = 1f
            )
        }

        val destRect = calculateDestRect(
            contentWidth, contentHeight,
            surfaceWidth, surfaceHeight,
            scaleMode
        )

        val scaleX = if (contentWidth > 0) destRect.width() / contentWidth.toFloat() else 1f
        val scaleY = if (contentHeight > 0) destRect.height() / contentHeight.toFloat() else 1f

        return Transform(destRect = destRect, scaleX = scaleX, scaleY = scaleY)
    }

    fun calculateDestRect(
        contentWidth: Int,
        contentHeight: Int,
        surfaceWidth: Int,
        surfaceHeight: Int,
        scaleMode: ScaleMode
    ): RectF {
        if (contentWidth <= 0 || contentHeight <= 0 ||
            surfaceWidth <= 0 || surfaceHeight <= 0
        ) {
            return RectF(0f, 0f, surfaceWidth.toFloat(), surfaceHeight.toFloat())
        }

        return when (scaleMode) {
            ScaleMode.STRETCH -> {
                RectF(0f, 0f, surfaceWidth.toFloat(), surfaceHeight.toFloat())
            }

            ScaleMode.CENTER_CROP -> {
                val contentRatio = contentWidth.toFloat() / contentHeight.toFloat()
                val surfaceRatio = surfaceWidth.toFloat() / surfaceHeight.toFloat()
                val sw: Float
                val sh: Float
                if (contentRatio > surfaceRatio) {
                    sh = surfaceHeight.toFloat()
                    sw = sh * contentRatio
                } else {
                    sw = surfaceWidth.toFloat()
                    sh = sw / contentRatio
                }
                val ox = (surfaceWidth - sw) / 2f
                val oy = (surfaceHeight - sh) / 2f
                RectF(ox, oy, ox + sw, oy + sh)
            }

            ScaleMode.FIT -> {
                val contentRatio = contentWidth.toFloat() / contentHeight.toFloat()
                val surfaceRatio = surfaceWidth.toFloat() / surfaceHeight.toFloat()
                val sw: Float
                val sh: Float
                if (contentRatio > surfaceRatio) {
                    sw = surfaceWidth.toFloat()
                    sh = sw / contentRatio
                } else {
                    sh = surfaceHeight.toFloat()
                    sw = sh * contentRatio
                }
                val ox = (surfaceWidth - sw) / 2f
                val oy = (surfaceHeight - sh) / 2f
                RectF(ox, oy, ox + sw, oy + sh)
            }

            ScaleMode.ORIGINAL -> {
                val sw = contentWidth.toFloat()
                val sh = contentHeight.toFloat()
                val ox = (surfaceWidth - sw) / 2f
                val oy = (surfaceHeight - sh) / 2f
                RectF(ox, oy, ox + sw, oy + sh)
            }
        }
    }
}
