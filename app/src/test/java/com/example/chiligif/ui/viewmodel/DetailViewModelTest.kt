package com.example.chiligif.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.chiligif.data.model.GifDto
import com.example.chiligif.data.model.ImageVariantDto
import com.example.chiligif.data.model.ImagesDto
import com.example.chiligif.data.repository.GiphyRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {
    
    private lateinit var repository: GiphyRepository
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: DetailViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    private val mockGif = GifDto(
        id = "123",
        title = "Test GIF",
        images = ImagesDto(
            original = ImageVariantDto(url = "http://test.com/gif.gif")
        ),
        rating = "g",
        username = "testuser"
    )
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        savedStateHandle = SavedStateHandle(mapOf("gifId" to "123"))
    }
    
    @Test
    fun `initial state is Loading`() = runTest {
        // Given
        coEvery { repository.getGifById("123") } coAnswers {
            kotlinx.coroutines.delay(100)
            mockGif
        }
        
        // When
        viewModel = DetailViewModel(repository, savedStateHandle)
        
        // Then
        viewModel.uiState.test {
            assertTrue(awaitItem() is DetailUiState.Loading)
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun `successful GIF fetch updates state to Success`() = runTest {
        // Given
        coEvery { repository.getGifById("123") } returns mockGif
        
        // When
        viewModel = DetailViewModel(repository, savedStateHandle)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is DetailUiState.Success)
            assertEquals(mockGif, state.gif)
        }
    }
    
    @Test
    fun `failed GIF fetch updates state to Error`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { repository.getGifById("123") } throws Exception(errorMessage)
        
        // When
        viewModel = DetailViewModel(repository, savedStateHandle)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is DetailUiState.Error)
            assertEquals(errorMessage, state.message)
        }
    }
    
    @Test
    fun `retry calls repository again`() = runTest {
        // Given
        coEvery { repository.getGifById("123") } throws Exception("First failure") andThen mockGif
        viewModel = DetailViewModel(repository, savedStateHandle)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.retry()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        coVerify(exactly = 2) { repository.getGifById("123") }
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is DetailUiState.Success)
        }
    }
    
    @Test
    fun `ViewModel throws exception when gifId is not provided`() {
        // Given
        val invalidSavedStateHandle = SavedStateHandle()
        
        // When/Then
        try {
            DetailViewModel(repository, invalidSavedStateHandle)
            throw AssertionError("Expected IllegalStateException to be thrown")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("gifId") == true)
        }
    }
}

