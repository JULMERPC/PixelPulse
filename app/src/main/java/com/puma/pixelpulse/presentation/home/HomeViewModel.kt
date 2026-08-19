package com.puma.pixelpulse.presentation.home

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.puma.pixelpulse.domain.model.Wallpaper
import com.puma.pixelpulse.domain.repository.WallpaperRepository
import com.puma.pixelpulse.domain.usecase.DeleteWallpaperUseCase
import com.puma.pixelpulse.domain.usecase.ImportVideoUseCase
import com.puma.pixelpulse.domain.usecase.ScanVideosUseCase
import com.puma.pixelpulse.wallpaper.ActiveWallpaperPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val repository: WallpaperRepository,
    private val importVideoUseCase: ImportVideoUseCase,
    private val scanVideosUseCase: ScanVideosUseCase,
    private val deleteWallpaperUseCase: DeleteWallpaperUseCase
) : AndroidViewModel(application) {

    val wallpapers: StateFlow<List<Wallpaper>> = repository.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _importState = MutableStateFlow<HomeImportState>(HomeImportState.Idle)
    val importState: StateFlow<HomeImportState> = _importState

    private val _scanState = MutableStateFlow<HomeScanState>(HomeScanState.Idle)
    val scanState: StateFlow<HomeScanState> = _scanState

    private val _wallpaperToDelete = MutableStateFlow<Wallpaper?>(null)
    val wallpaperToDelete: StateFlow<Wallpaper?> = _wallpaperToDelete

    private val _activeWallpaperUri = MutableStateFlow(
        ActiveWallpaperPrefs.getWallpaperUri(application)
    )
    val activeWallpaperUri: StateFlow<String?> = _activeWallpaperUri

    fun importVideo(uri: Uri) {
        viewModelScope.launch {
            _importState.value = HomeImportState.Loading
            importVideoUseCase(uri)
                .onSuccess { wallpaperId ->
                    _importState.value = HomeImportState.Success(wallpaperId)
                }
                .onFailure {
                    _importState.value = HomeImportState.Error(
                        it.message ?: "Import failed"
                    )
                }
        }
    }

    fun scanVideos() {
        viewModelScope.launch {
            _scanState.value = HomeScanState.Loading
            scanVideosUseCase()
                .onSuccess { count ->
                    _scanState.value = HomeScanState.Success(count)
                }
                .onFailure {
                    _scanState.value = HomeScanState.Error(
                        it.message ?: "Scan failed"
                    )
                }
        }
    }

    fun toggleFavorite(wallpaper: Wallpaper) {
        viewModelScope.launch {
            repository.setFavorite(wallpaper.id, !wallpaper.isFavorite)
        }
    }

    fun requestDelete(wallpaper: Wallpaper) {
        _wallpaperToDelete.value = wallpaper
    }

    fun confirmDelete() {
        val wallpaper = _wallpaperToDelete.value ?: return
        _wallpaperToDelete.value = null
        viewModelScope.launch {
            deleteWallpaperUseCase(wallpaper)
            _activeWallpaperUri.value = ActiveWallpaperPrefs.getWallpaperUri(getApplication())
        }
    }

    fun cancelDelete() {
        _wallpaperToDelete.value = null
    }

    fun refreshActiveWallpaper() {
        _activeWallpaperUri.value = ActiveWallpaperPrefs.getWallpaperUri(getApplication())
    }

    fun resetImportState() {
        _importState.value = HomeImportState.Idle
    }

    fun resetScanState() {
        _scanState.value = HomeScanState.Idle
    }
}

sealed class HomeImportState {
    data object Idle : HomeImportState()
    data object Loading : HomeImportState()
    data class Success(val wallpaperId: Long) : HomeImportState()
    data class Error(val message: String) : HomeImportState()
}

sealed class HomeScanState {
    data object Idle : HomeScanState()
    data object Loading : HomeScanState()
    data class Success(val count: Int) : HomeScanState()
    data class Error(val message: String) : HomeScanState()
}
