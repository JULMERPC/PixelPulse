package com.puma.pixelpulse.presentation.recents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puma.pixelpulse.domain.model.Wallpaper
import com.puma.pixelpulse.domain.repository.WallpaperRepository
import com.puma.pixelpulse.domain.usecase.DeleteWallpaperUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecentsViewModel @Inject constructor(
    private val repository: WallpaperRepository,
    private val deleteWallpaperUseCase: DeleteWallpaperUseCase
) : ViewModel() {

    val recents: StateFlow<List<Wallpaper>> = repository.getRecentWallpapers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _wallpaperToDelete = MutableStateFlow<Wallpaper?>(null)
    val wallpaperToDelete: StateFlow<Wallpaper?> = _wallpaperToDelete

    fun requestDelete(wallpaper: Wallpaper) {
        _wallpaperToDelete.value = wallpaper
    }

    fun confirmDelete() {
        val wallpaper = _wallpaperToDelete.value ?: return
        _wallpaperToDelete.value = null
        viewModelScope.launch {
            deleteWallpaperUseCase(wallpaper)
        }
    }

    fun cancelDelete() {
        _wallpaperToDelete.value = null
    }
}
