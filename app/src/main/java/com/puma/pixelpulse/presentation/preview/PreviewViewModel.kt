package com.puma.pixelpulse.presentation.preview

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.puma.pixelpulse.domain.model.ScaleMode
import com.puma.pixelpulse.domain.model.Wallpaper
import com.puma.pixelpulse.domain.repository.WallpaperRepository
import com.puma.pixelpulse.domain.usecase.ApplyWallpaperUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PreviewViewModel @Inject constructor(
    application: Application,
    private val repository: WallpaperRepository,
    private val applyWallpaperUseCase: ApplyWallpaperUseCase
) : AndroidViewModel(application) {

    private val _wallpaper = MutableStateFlow<Wallpaper?>(null)
    val wallpaper: StateFlow<Wallpaper?> = _wallpaper

    private val _isPlaying = MutableStateFlow(true)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _isMuted = MutableStateFlow(true)
    val isMuted: StateFlow<Boolean> = _isMuted

    private val _loopEnabled = MutableStateFlow(false)
    val loopEnabled: StateFlow<Boolean> = _loopEnabled

    private val _playbackSpeed = MutableStateFlow(1f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed

    private val _trimStartMs = MutableStateFlow(0L)
    val trimStartMs: StateFlow<Long> = _trimStartMs

    private val _trimEndMs = MutableStateFlow(0L)
    val trimEndMs: StateFlow<Long> = _trimEndMs

    private val _videoDurationMs = MutableStateFlow(0L)
    val videoDurationMs: StateFlow<Long> = _videoDurationMs

    private val _backgroundColor = MutableStateFlow(0xFF000000)
    val backgroundColor: StateFlow<Long> = _backgroundColor

    private val _scaleMode = MutableStateFlow(ScaleMode.CENTER_CROP)
    val scaleMode: StateFlow<ScaleMode> = _scaleMode

    fun loadWallpaper(wallpaperId: Long) {
        viewModelScope.launch {
            val wallpaper = repository.getById(wallpaperId)
            _wallpaper.value = wallpaper
            wallpaper?.let {
                _isMuted.value = it.muted
                _loopEnabled.value = it.loop
                _playbackSpeed.value = it.playbackSpeed
                _videoDurationMs.value = it.duration
                _trimStartMs.value = it.trimStartMs
                _trimEndMs.value = if (it.trimEndMs > 0) it.trimEndMs else it.duration
                _backgroundColor.value = it.backgroundColor
                _scaleMode.value = it.scaleMode
            }
        }
    }

    fun togglePlayPause() {
        _isPlaying.value = !_isPlaying.value
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
        updateWallpaperSettings()
    }

    fun toggleLoop() {
        _loopEnabled.value = !_loopEnabled.value
        updateWallpaperSettings()
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        updateWallpaperSettings()
    }

    fun setTrimStart(ms: Long) {
        _trimStartMs.value = ms
        updateWallpaperSettings()
    }

    fun setTrimEnd(ms: Long) {
        _trimEndMs.value = ms
        updateWallpaperSettings()
    }

    fun setBackgroundColor(color: Long) {
        _backgroundColor.value = color
        updateWallpaperSettings()
    }

    fun setScaleMode(newScaleMode: ScaleMode) {
        _scaleMode.value = newScaleMode
        updateWallpaperSettings()
    }

    fun updateVideoDuration(durationMs: Long) {
        if (_videoDurationMs.value == 0L) {
            _videoDurationMs.value = durationMs
            if (_trimEndMs.value == 0L) {
                _trimEndMs.value = durationMs
            }
        }
    }

    fun applyWallpaper(): Intent? {
        val wp = _wallpaper.value ?: return null
        val updated = wp.copy(
            muted = _isMuted.value,
            playbackSpeed = _playbackSpeed.value,
            trimStartMs = _trimStartMs.value,
            trimEndMs = _trimEndMs.value,
            backgroundColor = _backgroundColor.value,
            scaleMode = _scaleMode.value,
            loop = _loopEnabled.value
        )
        return applyWallpaperUseCase(updated)
    }

    private fun updateWallpaperSettings() {
        viewModelScope.launch {
            val wp = _wallpaper.value ?: return@launch
            val updated = wp.copy(
                muted = _isMuted.value,
                playbackSpeed = _playbackSpeed.value,
                trimStartMs = _trimStartMs.value,
                trimEndMs = _trimEndMs.value,
                backgroundColor = _backgroundColor.value,
                scaleMode = _scaleMode.value,
                loop = _loopEnabled.value
            )
            repository.update(updated)
            _wallpaper.value = updated
        }
    }
}
