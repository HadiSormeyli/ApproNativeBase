package com.approagency.base.presentation.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun Loading(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    size: Dp = 40.dp,
    strokeWidth: Dp = 4.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp)
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(size),
            color = color,
            strokeWidth = strokeWidth,
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
fun ThreeDottedLoading(
    modifier: Modifier = Modifier,
    dotSize: Dp = 8.dp,
    spacing: Dp = 4.dp,
    idleColor: Color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.35f),
    flashColor: Color = MaterialTheme.colorScheme.onPrimary,
    durationMillis: Int = 600,
    delayMillis: Int = 180
) {
    val transition = rememberInfiniteTransition(label = "threeDottedLoading")
    val dot1Color by transition.animateColor(
        initialValue = idleColor,
        targetValue = flashColor,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dot2Color by transition.animateColor(
        initialValue = idleColor,
        targetValue = flashColor,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, delayMillis = delayMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val dot3Color by transition.animateColor(
        initialValue = idleColor,
        targetValue = flashColor,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, delayMillis = delayMillis * 2, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(dot1Color, dot2Color, dot3Color).forEach { color ->
            Canvas(modifier = Modifier.size(dotSize)) {
                drawCircle(color = color)
            }
        }
    }
}

@Composable
fun Refresh(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Refresh,
    tint: Color = MaterialTheme.colorScheme.primary,
    size: Dp = 48.dp,
    contentDescription: String? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp)
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        SimpleIconButton(
            modifier = Modifier.size(size),
            imageVector = icon,
            tint = tint,
            contentDescription = contentDescription,
            onClick = onClick
        )
    }
}

@Composable
fun DottedLoadingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    shape: Shape = MaterialTheme.shapes.medium,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
    dotSize: Dp = 8.dp,
    dotSpacing: Dp = 4.dp
) {
    Button(
        modifier = modifier,
        enabled = enabled && !isLoading,
        shape = shape,
        colors = colors,
        contentPadding = contentPadding,
        onClick = onClick
    ) {
        if (isLoading) {
            ThreeDottedLoading(
                dotSize = dotSize,
                spacing = dotSpacing,
                idleColor = colors.contentColor.copy(alpha = 0.35f),
                flashColor = colors.contentColor
            )
        } else {
            Text(
                text = text,
                style = textStyle
            )
        }
    }
}

@Composable
fun LoadingDialog(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit = {},
    dismissOnBackPress: Boolean = false,
    dismissOnClickOutside: Boolean = false,
    shape: Shape = MaterialTheme.shapes.medium,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    border: BorderStroke? = BorderStroke(
        width = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    ),
    contentPadding: PaddingValues = PaddingValues(16.dp),
    indicatorSize: Dp = 48.dp,
    strokeWidth: Dp = 4.dp
) {
    if (!isLoading) return

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = dismissOnBackPress,
            dismissOnClickOutside = dismissOnClickOutside
        )
    ) {
        Surface(
            modifier = modifier,
            shape = shape,
            color = containerColor,
            contentColor = contentColor,
            border = border
        ) {
            Box(
                modifier = Modifier.padding(contentPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(indicatorSize),
                    color = contentColor,
                    strokeWidth = strokeWidth,
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}