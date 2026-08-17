package com.puma.pixelpulse.presentation.home

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puma.pixelpulse.domain.model.Wallpaper
import com.puma.pixelpulse.domain.repository.WallpaperRepository
import com.puma.pixelpulse.domain.usecase.ImportVideoUseCase
import com.puma.pixelpulse.domain.usecase.ScanVideosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: WallpaperRepository,
    private val importVideoUseCase: ImportVideoUseCase,
    private val scanVideosUseCase: ScanVideosUseCase
) : ViewModel() {

    val wallpapers: StateFlow<List<Wallpaper>> = repository.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _importState = MutableStateFlow<HomeImportState>(HomeImportState.Idle)
    val importState: StateFlow<HomeImportState> = _importState

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
            scanVideosUseCase()
        }
    }

    fun toggleFavorite(wallpaper: Wallpaper) {
        viewModelScope.launch {
            repository.setFavorite(wallpaper.id, !wallpaper.isFavorite)
        }
    }

    fun resetImportState() {
        _importState.value = HomeImportState.Idle
    }
}

sealed class HomeImportState {
    data object Idle : HomeImportState()
    data object Loading : HomeImportState()
    data class Success(val wallpaperId: Long) : HomeImportState()
    data class Error(val message: String) : HomeImportState()
}
