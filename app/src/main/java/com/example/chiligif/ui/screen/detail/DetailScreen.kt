package com.example.chiligif.ui.screen.detail

import android.content.res.Configuration
import android.util.Log
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.chiligif.R
import com.example.chiligif.data.model.GifDto
import com.example.chiligif.ui.viewmodel.DetailUiState
import com.example.chiligif.ui.viewmodel.DetailViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.DetailScreen(
    onBackClick: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.gif_details),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is DetailUiState.Loading -> {
                    LoadingContent()
                }
                is DetailUiState.Success -> {
                    DetailContent(
                        gif = state.gif,
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                }
                is DetailUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = { viewModel.retry() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.DetailContent(
    gif: GifDto,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val context = LocalContext.current
    val urlHandler = LocalUriHandler.current
    val scrollState = rememberScrollState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Remember formatters so they aren't re-created constantly
    val inputFormat = remember {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    }
    val outputFormat = remember {
        SimpleDateFormat("MMM dd, yyyy", Locale.US)
    }

    // Get the first available URL from multiple sources (prefer higher quality for detail view)
    val gifUrl = gif.images.original?.url
        ?: gif.images.downsized?.url
        ?: gif.images.downsizedMedium?.url
        ?: gif.images.fixedWidth?.url
        ?: gif.images.fixedWidthSmall?.url
    
    // If no valid URL is found, show error state
    if (gifUrl == null) {
        ErrorContent(
            message = stringResource(id = R.string.no_valid_images),
            onRetry = {}
        )
        return
    }

    if (isLandscape) {
        // Landscape: Side by side layout
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // GIF Card - Left side
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(gifUrl)
                        .memoryCacheKey(gif.id) // Use GIF ID as cache key
                        .diskCacheKey(gif.id)
                        .crossfade(true)
                        .build(),
                    contentDescription = gif.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .sharedElement(
                            sharedContentState = rememberSharedContentState(key = "gif-${gif.id}"),
                            animatedVisibilityScope = animatedVisibilityScope
                        ),
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                )
            }

            // Details - Right side (scrollable)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                if (gif.title.isNotBlank()) {
                    Text(
                        text = gif.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Details Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.details),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        // Username
                        if (!gif.username.isNullOrBlank()) {
                            DetailRow(
                                label = stringResource(id = R.string.creator),
                                value = gif.username
                            )
                        }

                        // Rating
                        gif.rating?.let {
                            DetailRow(
                                label = stringResource(id = R.string.rating),
                                value = it.uppercase()
                            )
                        }

                        // Dimensions
                        gif.images.original?.let { original ->
                            if (original.width != null && original.height != null) {
                                DetailRow(
                                    label = stringResource(id = R.string.dimensions),
                                    value = "${original.width} × ${original.height}"
                                )
                            }
                        }

                        // File Size
                        gif.images.original?.size?.let { size ->
                            val sizeInMB = size.toLongOrNull()?.let {
                                it / (1024.0 * 1024.0)
                            }
                            if (sizeInMB != null) {
                                DetailRow(
                                    label = stringResource(id = R.string.file_size),
                                    value = String.format(Locale.US, "%.2f MB", sizeInMB)
                                )
                            }
                        }

                        // Import Date
                        gif.importDatetime?.let { datetime ->
                            val formattedDate = try {
                                val date = inputFormat.parse(datetime)
                                // Handle both success and null-parse
                                date?.let { outputFormat.format(it) } ?: datetime
                            } catch (e: Exception) {
                                // Fallback if parsing throws an exception
                                Log.e("DetailContent", "Date parsing error: ${e.localizedMessage}")
                                datetime
                            }

                            DetailRow(
                                label = stringResource(id = R.string.added),
                                value = formattedDate
                            )
                        }

                        // GIF ID
                        DetailRow(
                            label = stringResource(id = R.string.id),
                            value = gif.id
                        )
                    }
                }

                // URL Card (if available)
                if (!gif.url.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.source_url),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                modifier = Modifier.clickable(
                                    onClick = {
                                        urlHandler.openUri(gif.url)
                                    }
                                ),
                                text = gif.url,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                                    .copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Portrait: Original vertical layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // GIF Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(gifUrl)
                        .memoryCacheKey(gif.id) // Use GIF ID as cache key
                        .diskCacheKey(gif.id)
                        .crossfade(true)
                        .build(),
                    contentDescription = gif.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .sharedElement(
                            sharedContentState = rememberSharedContentState(key = "gif-${gif.id}"),
                            animatedVisibilityScope = animatedVisibilityScope
                        ),
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                )
            }

            // Title
            if (gif.title.isNotBlank()) {
                Text(
                    text = gif.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.details),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    // Username
                    if (!gif.username.isNullOrBlank()) {
                        DetailRow(
                            label = stringResource(id = R.string.creator),
                            value = gif.username
                        )
                    }

                    // Rating
                    gif.rating?.let {
                        DetailRow(
                            label = stringResource(id = R.string.rating),
                            value = it.uppercase()
                        )
                    }

                    // Dimensions
                    gif.images.original?.let { original ->
                        if (original.width != null && original.height != null) {
                            DetailRow(
                                label = stringResource(id = R.string.dimensions),
                                value = "${original.width} × ${original.height}"
                            )
                        }
                    }

                    // File Size
                    gif.images.original?.size?.let { size ->
                        val sizeInMB = size.toLongOrNull()?.let {
                            it / (1024.0 * 1024.0)
                        }
                        if (sizeInMB != null) {
                            DetailRow(
                                label = stringResource(id = R.string.file_size),
                                value = String.format(Locale.US, "%.2f MB", sizeInMB)
                            )
                        }
                    }

                    // Import Date
                    gif.importDatetime?.let { datetime ->
                        val formattedDate = try {
                            val date = inputFormat.parse(datetime)
                            // Handle both success and null-parse
                            date?.let { outputFormat.format(it) } ?: datetime
                        } catch (e: Exception) {
                            // Fallback if parsing throws an exception
                            Log.e("DetailContent", "Date parsing error: ${e.localizedMessage}")
                            datetime
                        }

                        DetailRow(
                            label = stringResource(id = R.string.added),
                            value = formattedDate
                        )
                    }

                    // GIF ID
                    DetailRow(
                        label = stringResource(id = R.string.id),
                        value = gif.id
                    )
                }
            }

            // URL Card (if available)
            if (!gif.url.isNullOrBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.source_url),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            modifier = Modifier.clickable(
                                onClick = {
                                    urlHandler.openUri(gif.url)
                                }
                            ),
                            text = gif.url,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                .copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
                .copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = stringResource(id = R.string.loading_gif_details),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = stringResource(id = R.string.error_emoji),
                style = MaterialTheme.typography.displayLarge
            )
            Text(
                text = stringResource(id = R.string.error_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onRetry) {
                Text(stringResource(id = R.string.try_again))
            }
        }
    }
}
