package com.example.chiligif.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.chiligif.data.model.GifDto
import com.example.chiligif.data.repository.GiphyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: GiphyRepository
) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    /**
     * The paginated GIF data stream.
     * This flow automatically updates when the search query changes (after debounce).
     * Empty query shows trending GIFs.
     * Results are cached in the ViewModel scope to survive configuration changes.
     */
    val gifs: Flow<PagingData<GifDto>> = _searchQuery
        .debounce(500L) // Wait 500ms after the user stops typing
        .flatMapLatest { query ->
            repository.getSearchStream(query)
        }
        .cachedIn(viewModelScope) // Cache results in the ViewModel scope
    
    /**
     * Updates the search query.
     * The auto-search will trigger after the debounce period.
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
}

