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
        private const val MAX_CACHE_SIZE = 100 // Cache up to 100 GIFs
        private const val MAX_CACHE_SIZE_MB = 100 // Maximum cache size in MB
    }

    // In-memory cache for GIFs (LRU-like behavior)
    private val gifCache = LinkedHashMap<String, GifDto>(MAX_CACHE_SIZE, 0.75f, true)
    private val cacheMutex = Mutex()
    private var currentCacheSizeMB = 0.0
    
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
     * Stores a GIF in the cache (synchronous, non-blocking).
     * Can be called immediately when user clicks a GIF.
     */
    fun cacheGifSync(gif: GifDto) {
        synchronized(gifCache) {
            val gifSizeMB = estimateGifSizeMB(gif)

            // Remove oldest entries if cache limits exceeded
            while ((gifCache.size >= MAX_CACHE_SIZE || currentCacheSizeMB + gifSizeMB > MAX_CACHE_SIZE_MB)
                && gifCache.isNotEmpty()
            ) {
                val oldestKey = gifCache.keys.firstOrNull()
                oldestKey?.let {
                    val removedGif = gifCache.remove(it)
                    removedGif?.let { currentCacheSizeMB -= estimateGifSizeMB(it) }
                }
            }

            gifCache[gif.id] = gif
            currentCacheSizeMB += gifSizeMB
        }
    }

    /**
     * Stores a GIF in the cache (async version for background caching).
     * If cache exceeds MAX_CACHE_SIZE or MAX_CACHE_SIZE_MB, oldest entries are removed.
     */
    private suspend fun cacheGif(gif: GifDto) {
        cacheMutex.withLock {
            val gifSizeMB = estimateGifSizeMB(gif)

            // Remove oldest entries if cache limits exceeded
            while ((gifCache.size >= MAX_CACHE_SIZE || currentCacheSizeMB + gifSizeMB > MAX_CACHE_SIZE_MB)
                && gifCache.isNotEmpty()
            ) {
                val oldestKey = gifCache.keys.firstOrNull()
                oldestKey?.let {
                    val removedGif = gifCache.remove(it)
                    removedGif?.let { currentCacheSizeMB -= estimateGifSizeMB(it) }
                }
            }

            gifCache[gif.id] = gif
            currentCacheSizeMB += gifSizeMB
        }
    }

    /**
     * Estimates the size of a GIF in MB based on its image data.
     */
    private fun estimateGifSizeMB(gif: GifDto): Double {
        // Estimate based on original size if available, otherwise use conservative estimate
        val sizeBytes = gif.images.original?.size?.toLongOrNull()
            ?: gif.images.downsized?.size?.toLongOrNull()
            ?: gif.images.downsizedMedium?.size?.toLongOrNull()
            ?: 2_000_000L // Default 2MB estimate if no size info

        return sizeBytes / (1024.0 * 1024.0)
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

