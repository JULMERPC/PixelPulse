package com.puma.pixelpulse.presentation.library

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puma.pixelpulse.domain.model.Wallpaper
import com.puma.pixelpulse.domain.repository.WallpaperRepository
import com.puma.pixelpulse.domain.usecase.ApplyWallpaperUseCase
import com.puma.pixelpulse.domain.usecase.ImportVideoUseCase
import com.puma.pixelpulse.domain.usecase.ScanVideosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LibraryFilter {
    ALL, VIDEOS, FAVORITES
}

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: WallpaperRepository,
    private val importVideoUseCase: ImportVideoUseCase,
    private val scanVideosUseCase: ScanVideosUseCase,
    private val applyWallpaperUseCase: ApplyWallpaperUseCase
) : ViewModel() {

    private val _activeFilter = MutableStateFlow(LibraryFilter.ALL)
    val activeFilter: StateFlow<LibraryFilter> = _activeFilter

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState

    val wallpapers: StateFlow<List<Wallpaper>> = combine(
        repository.getAll(),
        repository.getFavorites(),
        _activeFilter
    ) { all, favorites, filter ->
        when (filter) {
            LibraryFilter.ALL -> all
            LibraryFilter.VIDEOS -> all.filter { it.type.name == "VIDEO" }
            LibraryFilter.FAVORITES -> favorites
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setFilter(filter: LibraryFilter) {
        _activeFilter.value = filter
    }

    fun importVideo(uri: Uri) {
        viewModelScope.launch {
            _importState.value = ImportState.Loading
            importVideoUseCase(uri)
                .onSuccess { _importState.value = ImportState.Success }
                .onFailure { _importState.value = ImportState.Error(it.message ?: "Import failed") }
        }
    }

    fun scanVideos() {
        viewModelScope.launch {
            _scanState.value = ScanState.Loading
            scanVideosUseCase()
                .onSuccess { count ->
                    _scanState.value = ScanState.Success(count)
                }
                .onFailure {
                    _scanState.value = ScanState.Error(it.message ?: "Scan failed")
                }
        }
    }

    fun applyWallpaper(wallpaper: Wallpaper): Intent {
        return applyWallpaperUseCase(wallpaper)
    }

    fun toggleFavorite(wallpaper: Wallpaper) {
        viewModelScope.launch {
            repository.setFavorite(wallpaper.id, !wallpaper.isFavorite)
        }
    }

    fun deleteWallpaper(wallpaper: Wallpaper) {
        viewModelScope.launch {
            repository.delete(wallpaper)
        }
    }

    fun resetImportState() {
        _importState.value = ImportState.Idle
    }

    fun resetScanState() {
        _scanState.value = ScanState.Idle
    }
}

sealed class ImportState {
    data object Idle : ImportState()
    data object Loading : ImportState()
    data object Success : ImportState()
    data class Error(val message: String) : ImportState()
}

sealed class ScanState {
    data object Idle : ScanState()
    data object Loading : ScanState()
    data class Success(val count: Int) : ScanState()
    data class Error(val message: String) : ScanState()
}
