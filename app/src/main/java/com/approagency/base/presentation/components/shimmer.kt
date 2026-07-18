package com.approagency.base.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.approagency.base.config.ApproConfig
import com.approagency.base.model.ui.ShimmerColors
import com.approagency.base.utils.shimmerLoadingAnimation
import org.koin.compose.koinInject

@Composable
fun ShimmerText(
    modifier: Modifier = Modifier,
    width: Dp = 56.dp,
    height: Dp = 16.dp,
    shape: Shape = MaterialTheme.shapes.small,
    colors: List<Color> = defaultShimmerColors().text
) {
    Box(
        modifier = modifier
            .size(width, height)
            .clip(shape)
            .shimmerLoadingAnimation(colors)
    )
}

@Composable
fun ShimmerImage(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    shape: Shape = MaterialTheme.shapes.medium,
    colors: List<Color> = defaultShimmerColors().image
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .shimmerLoadingAnimation(colors)
    )
}

@Composable
fun ShimmerContainer(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    colors: List<Color> = defaultShimmerColors().image
) {
    Box(
        modifier = modifier
            .clip(shape)
            .shimmerLoadingAnimation(colors)
    )
}

@Composable
fun ShimmerIcon(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    shape: Shape = MaterialTheme.shapes.medium,
    colors: List<Color> = defaultShimmerColors().image
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .shimmerLoadingAnimation(colors)
    )
}

@Composable
fun ShimmerIconButton(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    shape: Shape = MaterialTheme.shapes.large,
    colors: List<Color> = defaultShimmerColors().image
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .shimmerLoadingAnimation(colors)
    )
}

@Composable
fun defaultShimmerColors(): ShimmerColors {
    val color = MaterialTheme.colorScheme.surfaceVariant

    return koinInject<ApproConfig>().shimmerColors ?: ShimmerColors(
        image = listOf(
            color.copy(alpha = 0.3f),
            color.copy(alpha = 0.5f),
            color.copy(alpha = 0.7f),
            color.copy(alpha = 0.5f),
            color.copy(alpha = 0.3f)
        ),
        text = listOf(
            color.copy(alpha = 0.1f),
            color.copy(alpha = 0.3f),
            color.copy(alpha = 0.5f),
            color.copy(alpha = 0.3f),
            color.copy(alpha = 0.1f)
        )
    )
}