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
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test

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
        val gifsJob = viewModel.gifs.launchIn(backgroundScope)
        
        // Then
        viewModel.searchQuery.test {
            assertEquals("trending", awaitItem())
        }

        gifsJob.cancel()
    }
    
    @Test
    fun `setSearchQuery updates the search query state`() = runTest {
        // Given
        every { repository.getSearchStream(any()) } returns flowOf(PagingData.from(listOf(mockGif)))
        viewModel = SearchViewModel(repository)
        // Launch gifs flow collection to prevent "Flow constructed but not used" warning
        val gifsJob = viewModel.gifs.launchIn(backgroundScope)
        
        // When
        viewModel.setSearchQuery("cat")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        viewModel.searchQuery.test {
            assertEquals("cat", awaitItem())
        }

        gifsJob.cancel()
    }

    @Test
    fun `search is debounced by 500ms`() = runTest {
        // Given
        every { repository.getSearchStream(any()) } returns flowOf(PagingData.from(listOf(mockGif)))
        viewModel = SearchViewModel(repository)
        val gifsJob = viewModel.gifs.launchIn(backgroundScope)

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

        gifsJob.cancel()
    }
    
    @Test
    fun `blank queries are filtered out`() = runTest {
        // Given
        every { repository.getSearchStream(any()) } returns flowOf(PagingData.from(listOf(mockGif)))
        viewModel = SearchViewModel(repository)
        val gifsJob = viewModel.gifs.launchIn(backgroundScope)


        // When
        viewModel.setSearchQuery("")
        advanceTimeBy(500)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then - Repository should not be called with blank query
        verify(exactly = 0) { repository.getSearchStream("") }

        gifsJob.cancel()
    }
    
    @Test
    fun `repository is called after debounce period`() = runTest {
        // Given
        every { repository.getSearchStream(any()) } returns flowOf(PagingData.from(listOf(mockGif)))
        viewModel = SearchViewModel(repository)
        val gifsJob = viewModel.gifs.launchIn(backgroundScope)


        // When
        viewModel.setSearchQuery("dogs")
        advanceTimeBy(500)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        verify(atLeast = 1) { repository.getSearchStream("dogs") }

        gifsJob.cancel()
    }
}

