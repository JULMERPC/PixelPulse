package com.puma.pixelpulse.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puma.pixelpulse.data.local.UserPreferences
import com.puma.pixelpulse.domain.usecase.RemoveWallpaperUseCase
import com.puma.pixelpulse.wallpaper.ActiveWallpaperPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val removeWallpaperUseCase: RemoveWallpaperUseCase
) : ViewModel() {

    private val _activeWallpaperName = MutableStateFlow<String?>(
        ActiveWallpaperPrefs.getWallpaperName(context)
    )
    val activeWallpaperName: StateFlow<String?> = _activeWallpaperName

    private val _showRemoveDialog = MutableStateFlow(false)
    val showRemoveDialog: StateFlow<Boolean> = _showRemoveDialog

    val themeMode: StateFlow<UserPreferences.ThemeMode> = UserPreferences.getThemeMode(context)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences.ThemeMode.SYSTEM
        )

    val dynamicColor: StateFlow<Boolean> = UserPreferences.getDynamicColor(context)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val defaultVolume: StateFlow<Float> = UserPreferences.getDefaultVolume(context)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.5f
        )

    val defaultSpeed: StateFlow<Float> = UserPreferences.getDefaultSpeed(context)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 1f
        )

    val defaultLoop: StateFlow<Boolean> = UserPreferences.getDefaultLoop(context)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val defaultMuted: StateFlow<Boolean> = UserPreferences.getDefaultMuted(context)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    fun setThemeMode(mode: UserPreferences.ThemeMode) {
        viewModelScope.launch {
            UserPreferences.setThemeMode(context, mode)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            UserPreferences.setDynamicColor(context, enabled)
        }
    }

    fun setDefaultVolume(volume: Float) {
        viewModelScope.launch {
            UserPreferences.setDefaultVolume(context, volume)
        }
    }

    fun setDefaultSpeed(speed: Float) {
        viewModelScope.launch {
            UserPreferences.setDefaultSpeed(context, speed)
        }
    }

    fun setDefaultLoop(enabled: Boolean) {
        viewModelScope.launch {
            UserPreferences.setDefaultLoop(context, enabled)
        }
    }

    fun setDefaultMuted(muted: Boolean) {
        viewModelScope.launch {
            UserPreferences.setDefaultMuted(context, muted)
        }
    }

    fun requestRemoveWallpaper() {
        _showRemoveDialog.value = true
    }

    fun confirmRemoveWallpaper() {
        _showRemoveDialog.value = false
        viewModelScope.launch {
            removeWallpaperUseCase()
            _activeWallpaperName.value = null
        }
    }

    fun cancelRemoveWallpaper() {
        _showRemoveDialog.value = false
    }
}
