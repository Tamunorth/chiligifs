package com.example.chiligif.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.chiligif.data.api.GiphyApiService
import com.example.chiligif.data.model.GifDto
import retrofit2.HttpException
import java.io.IOException

class GiphyPagingSource(
    private val apiService: GiphyApiService,
    private val apiKey: String,
    private val query: String
) : PagingSource<Int, GifDto>() {
    
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GifDto> {
        // Start from page 0 if undefined
        val page = params.key ?: 0
        val offset = page * params.loadSize
        
        return try {
            // Use trending endpoint if query is empty or blank, otherwise use search
            val response = if (query.isBlank()) {
                apiService.trending(
                    apiKey = apiKey,
                    limit = params.loadSize,
                    offset = offset
                )
            } else {
                apiService.search(
                    apiKey = apiKey,
                    query = query,
                    limit = params.loadSize,
                    offset = offset
                )
            }
            
            // Filter out GIFs that don't have any valid image URLs
            val gifs = response.data.filter { gif ->
                gif.images.original?.url != null ||
                gif.images.downsized?.url != null ||
                gif.images.downsizedMedium?.url != null ||
                gif.images.fixedWidth?.url != null ||
                gif.images.fixedWidthSmall?.url != null
            }.distinctBy { it.id }
            
            val nextKey = if (gifs.isEmpty()) {
                null
            } else {
                page + 1
            }
            
            LoadResult.Page(
                data = gifs,
                prevKey = if (page == 0) null else page - 1,
                nextKey = nextKey
            )
        } catch (e: IOException) {
            // Network error
            LoadResult.Error(e)
        } catch (e: HttpException) {
            // HTTP error
            LoadResult.Error(e)
        } catch (e: Exception) {
            // Other errors
            LoadResult.Error(e)
        }
    }
    
    override fun getRefreshKey(state: PagingState<Int, GifDto>): Int? {
        // Try to find the page key of the closest page to the most recently accessed index.
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}

