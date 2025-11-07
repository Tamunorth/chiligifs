package com.example.chiligif.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.chiligif.data.api.GiphyApiService
import com.example.chiligif.data.model.GifDto
import com.example.chiligif.data.paging.GiphyPagingSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GiphyRepository @Inject constructor(
    private val apiService: GiphyApiService,
    private val apiKey: String
) {
    
    companion object {
        private const val PAGE_SIZE = 20
        private const val INITIAL_LOAD_SIZE = 40
    }
    
    /**
     * Returns a Flow of PagingData for the search results.
     * This will automatically handle pagination as the user scrolls.
     */
    fun getSearchStream(query: String): Flow<PagingData<GifDto>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = INITIAL_LOAD_SIZE,
                enablePlaceholders = false,
                prefetchDistance = 5
            ),
            pagingSourceFactory = {
                GiphyPagingSource(
                    apiService = apiService,
                    apiKey = apiKey,
                    query = query
                )
            }
        ).flow
    }
    
    /**
     * Fetches a single GIF by its ID.
     * Throws an exception if the request fails.
     */
    suspend fun getGifById(gifId: String): GifDto {
        return apiService.getGifById(gifId = gifId, apiKey = apiKey).data
    }
}

