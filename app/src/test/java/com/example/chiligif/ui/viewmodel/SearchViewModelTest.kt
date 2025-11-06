package com.example.chiligif.ui.viewmodel

import androidx.paging.PagingData
import app.cash.turbine.test
import com.example.chiligif.data.model.GifDto
import com.example.chiligif.data.model.ImageVariantDto
import com.example.chiligif.data.model.ImagesDto
import com.example.chiligif.data.repository.GiphyRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    
    private lateinit var repository: GiphyRepository
    private lateinit var viewModel: SearchViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    private val mockGif = GifDto(
        id = "1",
        title = "Test GIF",
        images = ImagesDto(
            original = ImageVariantDto(url = "http://test.com/gif.gif")
        ),
        rating = "g"
    )
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
    }
    
    @Test
    fun `initial search query is trending`() = runTest {
        // Given
        every { repository.getSearchStream(any()) } returns flowOf(PagingData.from(listOf(mockGif)))
        
        // When
        viewModel = SearchViewModel(repository)
        
        // Then
        viewModel.searchQuery.test {
            assertEquals("trending", awaitItem())
        }
    }
    
    @Test
    fun `setSearchQuery updates the search query state`() = runTest {
        // Given
        every { repository.getSearchStream(any()) } returns flowOf(PagingData.from(listOf(mockGif)))
        viewModel = SearchViewModel(repository)
        
        // When
        viewModel.setSearchQuery("cat")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        viewModel.searchQuery.test {
            assertEquals("cat", awaitItem())
        }
    }
    
    @Test
    fun `search is debounced by 500ms`() = runTest {
        // Given
        every { repository.getSearchStream(any()) } returns flowOf(PagingData.from(listOf(mockGif)))
        viewModel = SearchViewModel(repository)
        
        // When - Set query multiple times within 500ms
        viewModel.setSearchQuery("c")
        advanceTimeBy(100)
        viewModel.setSearchQuery("ca")
        advanceTimeBy(100)
        viewModel.setSearchQuery("cat")
        
        // Advance past the debounce time
        advanceTimeBy(500)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then - Repository should only be called once with the final query
        verify(exactly = 1) { repository.getSearchStream("cat") }
    }
    
    @Test
    fun `blank queries are filtered out`() = runTest {
        // Given
        every { repository.getSearchStream(any()) } returns flowOf(PagingData.from(listOf(mockGif)))
        viewModel = SearchViewModel(repository)
        
        // When
        viewModel.setSearchQuery("")
        advanceTimeBy(500)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then - Repository should not be called with blank query
        verify(exactly = 0) { repository.getSearchStream("") }
    }
    
    @Test
    fun `repository is called after debounce period`() = runTest {
        // Given
        every { repository.getSearchStream(any()) } returns flowOf(PagingData.from(listOf(mockGif)))
        viewModel = SearchViewModel(repository)
        
        // When
        viewModel.setSearchQuery("dogs")
        advanceTimeBy(500)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        verify(atLeast = 1) { repository.getSearchStream("dogs") }
    }
}

