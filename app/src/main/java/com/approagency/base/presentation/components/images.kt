package com.approagency.base.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest

@Composable
fun NetworkImage(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    memoryCachePolicy: CachePolicy = CachePolicy.ENABLED,
    diskCachePolicy: CachePolicy = CachePolicy.ENABLED,
    networkCachePolicy: CachePolicy = CachePolicy.ENABLED,
    crossfade: Boolean = true,
    retryIcon: ImageVector = Icons.Default.Refresh,
    retryIconSize: Dp = 24.dp,
    retryIconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    loading: @Composable (() -> Unit)? = null,
    error: @Composable ((retry: () -> Unit) -> Unit)? = null,
    onSuccess: (() -> Unit)? = null,
    onError: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var requestKey by remember(imageUrl) { mutableIntStateOf(0) }

    key(imageUrl, requestKey) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .crossfade(crossfade)
                .memoryCachePolicy(memoryCachePolicy)
                .diskCachePolicy(diskCachePolicy)
                .networkCachePolicy(networkCachePolicy)
                .build(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            loading = {
                loading?.invoke() ?: ShimmerContainer(modifier)
            },
            error = {
                onError?.invoke()
                val retry = {
                    requestKey++
                    Unit
                }
                error?.invoke(retry) ?: Box(
                    modifier = modifier.clickable(onClick = retry),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = retryIcon,
                        contentDescription = contentDescription,
                        modifier = Modifier.size(retryIconSize),
                        tint = retryIconTint
                    )
                }
            },
            onSuccess = {
                onSuccess?.invoke()
            }
        )
    }
}