package com.example.chiligif.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.chiligif.data.api.GiphyApiService
import com.example.chiligif.data.model.GifDto
import com.example.chiligif.data.paging.GiphyPagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
        private const val MAX_CACHE_SIZE = 200 // Cache up to 200 GIFs
    }

    // In-memory cache for GIFs (LRU-like behavior)
    private val gifCache = LinkedHashMap<String, GifDto>(MAX_CACHE_SIZE, 0.75f, true)
    private val cacheMutex = Mutex()
    
    /**
     * Returns a Flow of PagingData for the search results.
     * This will automatically handle pagination as the user scrolls.
     * Also caches the GIFs as they're loaded.
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
        ).flow.map { pagingData ->
            // Cache each GIF as it flows through
            pagingData.map { gif ->
                cacheGif(gif)
                gif
            }
        }
    }
    
    /**
     * Fetches a single GIF by its ID.
     * First checks the cache, then falls back to API if not found.
     * Throws an exception if the request fails.
     */
    suspend fun getGifById(gifId: String): GifDto {
        // Check cache first
        val cachedGif = getCachedGif(gifId)
        if (cachedGif != null) {
            return cachedGif
        }

        // If not in cache, fetch from API
        val gif = apiService.getGifById(gifId = gifId, apiKey = apiKey).data

        // Cache the fetched GIF
        cacheGif(gif)

        return gif
    }

    /**
     * Stores a GIF in the cache.
     * If cache exceeds MAX_CACHE_SIZE, oldest entries are removed.
     */
    private suspend fun cacheGif(gif: GifDto) {
        cacheMutex.withLock {
            // Remove oldest entry if cache is full
            if (gifCache.size >= MAX_CACHE_SIZE) {
                val oldestKey = gifCache.keys.firstOrNull()
                oldestKey?.let { gifCache.remove(it) }
            }
            gifCache[gif.id] = gif
        }
    }

    /**
     * Retrieves a GIF from the cache if it exists.
     */
    private suspend fun getCachedGif(gifId: String): GifDto? {
        return cacheMutex.withLock {
            gifCache[gifId]
        }
    }
}

