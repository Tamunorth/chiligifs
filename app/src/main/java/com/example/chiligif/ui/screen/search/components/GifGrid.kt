package com.example.chiligif.ui.screen.search.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.chiligif.R
import com.example.chiligif.data.model.GifDto
import com.example.chiligif.data.model.ImageVariantDto
import com.example.chiligif.data.model.ImagesDto
import kotlinx.coroutines.flow.flowOf


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.GifGrid(
    lazyGifItems: LazyPagingItems<GifDto>,
    onGifClick: (GifDto) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            count = lazyGifItems.itemCount,
            key = { index -> lazyGifItems[index]?.id ?: index }
        ) { index ->
            lazyGifItems[index]?.let { gif ->
                GifGridItem(
                    gif = gif,
                    onClick = { onGifClick(gif) },
                    animatedVisibilityScope = animatedVisibilityScope
                )
            }
        }

        // Handle loading and error for pagination
        item {
            when (lazyGifItems.loadState.append) {
                is LoadState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is LoadState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(onClick = { lazyGifItems.retry() }) {
                            Text(stringResource(id = R.string.retry))
                        }
                    }
                }

                else -> {}
            }
        }
    }
}


@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun GifGridPreview() {
    val sampleGifs = listOf(
        GifDto(
            id = "id1", title = "Funny Cat", images = ImagesDto(
                fixedWidth = ImageVariantDto(
                    url = "https://media.giphy.com/media/JIX9t2j0ZTN9S/giphy.gif"
                ),
                fixedWidthSmall = null,
                downsized = null,
                downsizedMedium = null,
                original = null
            )
        ),
        GifDto(
            id = "id2", title = "Dancing Dog", images = ImagesDto(
                fixedWidth = ImageVariantDto(
                    url = "https://media.giphy.com/media/3o6Zt481isNVuQI1l6/giphy.gif"
                ),
            )
        ),
        GifDto(
            id = "id3", title = "Silly Mome", images = ImagesDto(
                fixedWidth = ImageVariantDto(
                    url = "https://media.giphy.com/media/l0MYt5jPR6QX5pnqM/giphy.gif"
                ),
            )
        ),
        GifDto(
            id = "id4", title = "Reaction GIF", images = ImagesDto(
                fixedWidth = ImageVariantDto(
                    url = "https://media.giphy.com/media/26BRv0ThflsHCqDrG/giphy.gif"
                ),
            )
        )
    )

    val pagingData = PagingData.from(sampleGifs)

    val gifFlow = flowOf(pagingData)

    val lazyGifItems = gifFlow.collectAsLazyPagingItems()

    androidx.compose.animation.SharedTransitionLayout {
        androidx.compose.animation.AnimatedVisibility(visible = true) {
            GifGrid(
                lazyGifItems = lazyGifItems,
                onGifClick = {},
                animatedVisibilityScope = this
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.GifGridItem(
    gif: GifDto,
    onClick: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val context = LocalContext.current

    // Get the first available URL from multiple sources
    val gifUrl = gif.images.fixedWidth?.url
        ?: gif.images.fixedWidthSmall?.url
        ?: gif.images.downsized?.url
        ?: gif.images.downsizedMedium?.url
        ?: gif.images.original?.url
        ?: return // Skip rendering if no valid URL found

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(gifUrl)
                    .memoryCacheKey(gif.id) // Use GIF ID as cache key
                    .diskCacheKey(gif.id)
                    .crossfade(true)
                    .build(),
                contentDescription = gif.title,
                contentScale = ContentScale.Crop,
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

            // Overlay with title
            if (gif.title.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = gif.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
