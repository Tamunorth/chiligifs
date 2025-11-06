package com.example.chiligif.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chiligif.data.model.GifDto
import com.example.chiligif.data.repository.GiphyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DetailUiState {
    data object Loading : DetailUiState()
    data class Success(val gif: GifDto) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: GiphyRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val gifId: String = checkNotNull(savedStateHandle["gifId"]) {
        "gifId is required for DetailViewModel"
    }
    
    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()
    
    init {
        loadGifDetails()
    }
    
    private fun loadGifDetails() {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            try {
                val gif = repository.getGifById(gifId)
                _uiState.value = DetailUiState.Success(gif)
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error(
                    message = e.message ?: "Failed to load GIF details"
                )
            }
        }
    }
    
    /**
     * Retry loading the GIF details after an error.
     */
    fun retry() {
        loadGifDetails()
    }
}

