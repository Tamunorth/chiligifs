package com.example.chiligif.ui.screen.search


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.chiligif.ui.screen.search.components.CustomSearchBar
import com.example.chiligif.ui.screen.search.components.EmptyState
import com.example.chiligif.ui.screen.search.components.ErrorState
import com.example.chiligif.ui.screen.search.components.GifGrid
import com.example.chiligif.ui.screen.search.components.LoadingState
import com.example.chiligif.ui.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onGifClick: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val lazyGifItems = viewModel.gifs.collectAsLazyPagingItems()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ChiliGIF",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search TextField
            CustomSearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            
            // Handle loading and error states
            when (val state = lazyGifItems.loadState.refresh) {
                is LoadState.Loading -> {
                    LoadingState()
                }
                is LoadState.Error -> {
                    ErrorState(
                        message = state.error.message ?: "An error occurred",
                        onRetry = { lazyGifItems.retry() }
                    )
                }
                else -> {
                    // Show the grid
                    if (lazyGifItems.itemCount == 0) {
                        EmptyState()
                    } else {
                        GifGrid(
                            lazyGifItems = lazyGifItems,
                            onGifClick = onGifClick
                        )
                    }
                }
            }
        }
    }
}



