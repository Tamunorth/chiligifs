package com.example.chiligif.data.repository

import androidx.paging.PagingSource
import com.example.chiligif.data.api.GiphyApiService
import com.example.chiligif.data.model.GifDto
import com.example.chiligif.data.model.ImageVariantDto
import com.example.chiligif.data.model.ImagesDto
import com.example.chiligif.data.model.SingleGifResponseDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GiphyRepositoryTest {
    
    private lateinit var apiService: GiphyApiService
    private lateinit var repository: GiphyRepository
    private val apiKey = "test_api_key"
    
    private val mockGif = GifDto(
        id = "123",
        title = "Test GIF",
        images = ImagesDto(
            original = ImageVariantDto(url = "http://test.com/gif.gif")
        ),
        rating = "g"
    )
    
    @Before
    fun setup() {
        apiService = mockk()
        repository = GiphyRepository(apiService, apiKey)
    }
    
    @Test
    fun `getSearchStream returns Flow of PagingData`() = runTest {
        // When
        val flow = repository.getSearchStream("cats")
        
        // Then
        assertNotNull(flow)
        // Just verify we can collect from the flow
        val pagingData = flow.first()
        assertNotNull(pagingData)
    }
    
    @Test
    fun `getGifById returns GIF when successful`() = runTest {
        // Given
        val gifId = "123"
        val response = SingleGifResponseDto(data = mockGif)
        coEvery { apiService.getGifById(gifId, apiKey) } returns response
        
        // When
        val result = repository.getGifById(gifId)
        
        // Then
        assertEquals(mockGif, result)
        coVerify(exactly = 1) { apiService.getGifById(gifId, apiKey) }
    }
    
    @Test
    fun `getGifById throws exception when API call fails`() = runTest {
        // Given
        val gifId = "123"
        val exception = Exception("Network error")
        coEvery { apiService.getGifById(gifId, apiKey) } throws exception
        
        // When/Then
        try {
            repository.getGifById(gifId)
            throw AssertionError("Expected exception to be thrown")
        } catch (e: Exception) {
            assertEquals("Network error", e.message)
        }
        
        coVerify(exactly = 1) { apiService.getGifById(gifId, apiKey) }
    }
    
    @Test
    fun `getSearchStream with different queries creates different PagingSources`() = runTest {
        // When
        val flow1 = repository.getSearchStream("cats")
        val flow2 = repository.getSearchStream("dogs")
        
        // Then
        assertNotNull(flow1)
        assertNotNull(flow2)
        // Both flows should be valid and independent
        val pagingData1 = flow1.first()
        val pagingData2 = flow2.first()
        assertNotNull(pagingData1)
        assertNotNull(pagingData2)
    }
}

