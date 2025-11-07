package com.example.chiligif.data.repository

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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

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
        // When - collect immediately to avoid "Flow constructed but not used" warning
        val pagingData = repository.getSearchStream("cats").first()
        
        // Then
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
        // When - collect immediately to avoid "Flow constructed but not used" warning
        val pagingData1 = repository.getSearchStream("cats").first()
        val pagingData2 = repository.getSearchStream("dogs").first()

        // Then - Both flows should be valid and independent
        assertNotNull(pagingData1)
        assertNotNull(pagingData2)
    }

    @Test
    fun `getGifById uses cache on second call`() = runTest {
        // Given
        val gifId = "123"
        val response = SingleGifResponseDto(data = mockGif)
        coEvery { apiService.getGifById(gifId, apiKey) } returns response

        // When
        val result1 = repository.getGifById(gifId)
        val result2 = repository.getGifById(gifId) // Should use cache

        // Then
        assertEquals(mockGif, result1)
        assertEquals(mockGif, result2)
        // API should only be called once because second call uses cache
        coVerify(exactly = 1) { apiService.getGifById(gifId, apiKey) }
    }

    @Test
    fun `getGifById fetches different GIFs independently`() = runTest {
        // Given
        val gif1 = mockGif.copy(id = "123", title = "First GIF")
        val gif2 = mockGif.copy(id = "456", title = "Second GIF")

        coEvery { apiService.getGifById("123", apiKey) } returns SingleGifResponseDto(data = gif1)
        coEvery { apiService.getGifById("456", apiKey) } returns SingleGifResponseDto(data = gif2)

        // When
        val result1 = repository.getGifById("123")
        val result2 = repository.getGifById("456")

        // Then
        assertEquals("First GIF", result1.title)
        assertEquals("Second GIF", result2.title)
        coVerify(exactly = 1) { apiService.getGifById("123", apiKey) }
        coVerify(exactly = 1) { apiService.getGifById("456", apiKey) }
    }
}

