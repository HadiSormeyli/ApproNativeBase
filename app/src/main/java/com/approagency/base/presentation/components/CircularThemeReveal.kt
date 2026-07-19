package com.approagency.base.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntSize
import com.approagency.base.utils.CircularThemeRevealState
import kotlin.math.hypot
import kotlin.math.roundToInt

@Composable
fun CircularThemeReveal(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    state: CircularThemeRevealState = rememberCircularThemeRevealState(isDarkTheme),
    durationMillis: Int = 900,
    easing: Easing = FastOutSlowInEasing,
    overlayAlpha: Float = 1f,
    onTransitionStart: (Boolean) -> Unit = {},
    onTransitionEnd: (Boolean) -> Unit = {},
    content: @Composable BoxScope.(CircularThemeRevealState) -> Unit
) {
    var snapshot by remember { mutableStateOf<ImageBitmap?>(null) }
    val progress = remember { Animatable(1f) }

    LaunchedEffect(isDarkTheme) {
        if (!state.isAnimating) {
            state.targetDarkTheme = isDarkTheme
        }
    }

    LaunchedEffect(state.requestId) {
        if (state.targetDarkTheme == isDarkTheme) {
            return@LaunchedEffect
        }

        val targetDarkTheme = state.targetDarkTheme
        val startProgress = if (targetDarkTheme) 1f else 0f
        val endProgress = if (targetDarkTheme) 0f else 1f

        snapshot = state.graphicsLayer.toImageBitmap()
        state.isAnimating = true
        progress.snapTo(startProgress)

        onTransitionStart(targetDarkTheme)
        onThemeChange(targetDarkTheme)

        progress.animateTo(
            targetValue = endProgress,
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = easing
            )
        )

        snapshot = null
        state.isAnimating = false
        onTransitionEnd(targetDarkTheme)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned {
                state.containerPosition = it.positionInRoot()
            }
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawWithContent {
                    state.graphicsLayer.record {
                        this@drawWithContent.drawContent()
                    }

                    drawLayer(state.graphicsLayer)
                }
        ) {
            content(state)
        }

        Canvas(
            modifier = Modifier.matchParentSize()
        ) {
            val image = snapshot ?: return@Canvas
            val origin = state.origin

            val maximumRadius = maxOf(
                hypot(origin.x, origin.y),
                hypot(size.width - origin.x, origin.y),
                hypot(origin.x, size.height - origin.y),
                hypot(size.width - origin.x, size.height - origin.y)
            )

            val radius = maximumRadius * progress.value

            val path = Path().apply {
                addOval(
                    Rect(
                        center = origin,
                        radius = radius
                    )
                )
            }

            drawIntoCanvas { canvas ->
                canvas.save()

                canvas.clipPath(
                    path = path,
                    clipOp = if (state.targetDarkTheme) {
                        ClipOp.Intersect
                    } else {
                        ClipOp.Difference
                    }
                )

                canvas.drawImageRect(
                    image = image,
                    srcSize = IntSize(
                        width = image.width,
                        height = image.height
                    ),
                    dstSize = IntSize(
                        width = size.width.roundToInt(),
                        height = size.height.roundToInt()
                    ),
                    paint = Paint().apply {
                        alpha = overlayAlpha
                    }
                )

                canvas.restore()
            }
        }
    }
}


@Composable
fun rememberCircularThemeRevealState(
    isDarkTheme: Boolean
): CircularThemeRevealState {
    val graphicsLayer = rememberGraphicsLayer()
    return remember(graphicsLayer) {
        CircularThemeRevealState(
            graphicsLayer = graphicsLayer,
            initialDarkTheme = isDarkTheme
        )
    }
}