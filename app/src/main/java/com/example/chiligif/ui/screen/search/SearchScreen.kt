package com.example.chiligif.ui.screen.search


import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.chiligif.R
import com.example.chiligif.ui.screen.search.components.CustomSearchBar
import com.example.chiligif.ui.screen.search.components.EmptyState
import com.example.chiligif.ui.screen.search.components.ErrorState
import com.example.chiligif.ui.screen.search.components.GifGrid
import com.example.chiligif.ui.screen.search.components.LoadingState
import com.example.chiligif.ui.screen.search.components.SearchDialog
import com.example.chiligif.ui.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.SearchScreen(
    onGifClick: (String) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val lazyGifItems = viewModel.gifs.collectAsLazyPagingItems()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    var showSearchDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.app_title),
                        fontWeight = FontWeight.Bold,
                        style = if (isLandscape)
                            MaterialTheme.typography.titleMedium
                        else
                            MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    // Language toggle button (shows current language code)
                    TextButton(
                        onClick = onToggleLanguage,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.language_code),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    // Theme toggle button (always visible)
                    IconButton(
                        onClick = onToggleTheme
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = stringResource(id = R.string.toggle_theme),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    if (isLandscape) {
                        // In landscape, show search icon
                        IconButton(
                            onClick = { showSearchDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = stringResource(id = R.string.search),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
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
            // Search TextField - only show in portrait
            if (!isLandscape) {
                CustomSearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
            
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
                            onGifClick = onGifClick,
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    }
                }
            }
        }
    }

    // Search dialog for landscape mode
    if (isLandscape && showSearchDialog) {
        SearchDialog(
            query = searchQuery,
            onQueryChange = { viewModel.setSearchQuery(it) },
            onDismiss = { showSearchDialog = false }
        )
    }
}



