package com.example.chiligif.data.paging

import androidx.paging.PagingSource
import com.example.chiligif.data.api.GiphyApiService
import com.example.chiligif.data.model.GifDto
import com.example.chiligif.data.model.ImageVariantDto
import com.example.chiligif.data.model.ImagesDto
import com.example.chiligif.data.model.PaginationDto
import com.example.chiligif.data.model.SearchResponseDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class GiphyPagingSourceTest {
    
    private lateinit var apiService: GiphyApiService
    private lateinit var pagingSource: GiphyPagingSource
    private val apiKey = "test_api_key"
    private val query = "cats"
    
    private val mockGifs = listOf(
        GifDto(
            id = "1",
            title = "Cat GIF 1",
            images = ImagesDto(
                original = ImageVariantDto(url = "http://test.com/1.gif")
            ),
            rating = "g"
        ),
        GifDto(
            id = "2",
            title = "Cat GIF 2",
            images = ImagesDto(
                original = ImageVariantDto(url = "http://test.com/2.gif")
            ),
            rating = "g"
        )
    )
    
    @Before
    fun setup() {
        apiService = mockk()
        pagingSource = GiphyPagingSource(apiService, apiKey, query)
    }
    
    @Test
    fun `load returns Page when successful`() = runTest {
        // Given
        val response = SearchResponseDto(
            data = mockGifs,
            pagination = PaginationDto(totalCount = 100, count = 2, offset = 0)
        )
        coEvery {
            apiService.search(
                apiKey = apiKey,
                query = query,
                limit = any(),
                offset = 0
            )
        } returns response

        // When
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = 0,
                loadSize = 20,
                placeholdersEnabled = false
            )
        )

        // Then
        assertTrue(result is PagingSource.LoadResult.Page)

        val page = result as PagingSource.LoadResult.Page

        assertEquals(mockGifs, page.data)
        assertEquals(null, page.prevKey)
        assertEquals(1, page.nextKey)
    }

    @Test
    fun `load returns Page with null nextKey when data is empty`() = runTest {
        // Given
        val response = SearchResponseDto(
            data = emptyList(),
            pagination = PaginationDto(totalCount = 0, count = 0, offset = 0)
        )
        coEvery {
            apiService.search(
                apiKey = apiKey,
                query = query,
                limit = any(),
                offset = 0
            )
        } returns response
        
        // When
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = 0,
                loadSize = 20,
                placeholdersEnabled = false
            )
        )
        
        // Then
        assertTrue(result is PagingSource.LoadResult.Page)

        val page = result as PagingSource.LoadResult.Page

        assertTrue(result.data.isEmpty())
        assertEquals(null, result.nextKey)
    }
    
    @Test
    fun `load returns Error when IOException occurs`() = runTest {
        // Given
        val exception = IOException("Network error")
        coEvery {
            apiService.search(
                apiKey = any(),
                query = any(),
                limit = any(),
                offset = any()
            )
        } throws exception
        
        // When
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = 0,
                loadSize = 20,
                placeholdersEnabled = false
            )
        )
        
        // Then
        assertTrue(result is PagingSource.LoadResult.Error)
        assertEquals(exception, result.throwable)
    }
    
    @Test
    fun `load returns Error when HttpException occurs`() = runTest {
        // Given
        val exception = HttpException(
            Response.error<Any>(404, okhttp3.ResponseBody.create(null, "Not found"))
        )
        coEvery {
            apiService.search(
                apiKey = any(),
                query = any(),
                limit = any(),
                offset = any()
            )
        } throws exception
        
        // When
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = 0,
                loadSize = 20,
                placeholdersEnabled = false
            )
        )
        
        // Then
        assertTrue(result is PagingSource.LoadResult.Error)
        assertTrue(result.throwable is HttpException)
    }
    
    @Test
    fun `load calculates correct offset for page 2`() = runTest {
        // Given
        val pageSize = 20
        val page = 2
        val expectedOffset = page * pageSize // 40
        
        val response = SearchResponseDto(
            data = mockGifs,
            pagination = PaginationDto(totalCount = 100, count = 2, offset = expectedOffset)
        )
        
        coEvery {
            apiService.search(
                apiKey = apiKey,
                query = query,
                limit = pageSize,
                offset = expectedOffset
            )
        } returns response
        
        // When
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = page,
                loadSize = pageSize,
                placeholdersEnabled = false
            )
        )
        
        // Then
        assertTrue(result is PagingSource.LoadResult.Page)
        assertEquals(mockGifs, result.data)
        assertEquals(1, result.prevKey)
        assertEquals(3, result.nextKey)
    }
    
    @Test
    fun `getRefreshKey returns correct key based on anchorPosition`() {
        // Given
        val state = mockk<androidx.paging.PagingState<Int, GifDto>>()
        val anchorPosition = 25
        
        coEvery { state.anchorPosition } returns anchorPosition
        coEvery { state.closestPageToPosition(anchorPosition) } returns 
            androidx.paging.PagingSource.LoadResult.Page(
                data = mockGifs,
                prevKey = 1,
                nextKey = 3
            )
        
        // When
        val refreshKey = pagingSource.getRefreshKey(state)
        
        // Then
        // Should return prevKey + 1 = 2
        assertEquals(2, refreshKey)
    }
    
    @Test
    fun `getRefreshKey returns null when anchorPosition is null`() {
        // Given
        val state = mockk<androidx.paging.PagingState<Int, GifDto>>()
        coEvery { state.anchorPosition } returns null
        
        // When
        val refreshKey = pagingSource.getRefreshKey(state)
        
        // Then
        assertEquals(null, refreshKey)
    }
}

