package com.approagency.base.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.approagency.base.R
import com.approagency.base.model.UiText
import com.approagency.base.model.network.Failure

@Composable
fun ErrorLayout(
    message: UiText?,
    buttonText: String = stringResource(R.string.retry),
    onRetry: () -> Unit,
    modifier: Modifier = Modifier.fillMaxSize(),
    spacing: Dp = 16.dp,
    horizontalPadding: Dp = 16.dp,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    buttonShape: Shape = MaterialTheme.shapes.extraLarge,
    buttonColors: ButtonColors = ButtonDefaults.buttonColors(),
    buttonContentPadding: PaddingValues = ButtonDefaults.ContentPadding
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(
            spacing,
            Alignment.CenterVertically
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = (message ?: Failure.Unknown.text).asString(),
            modifier = Modifier.padding(horizontal = horizontalPadding),
            style = textStyle,
            textAlign = TextAlign.Center
        )

        FilledTextButton(
            text = buttonText,
            shape = buttonShape,
            colors = buttonColors,
            contentPadding = buttonContentPadding,
            onClick = onRetry
        )
    }
}

@Composable
fun ErrorLayout(
    message: String,
    buttonText: String = stringResource(R.string.retry),
    onRetry: () -> Unit,
    modifier: Modifier = Modifier.fillMaxSize(),
    spacing: Dp = 16.dp,
    horizontalPadding: Dp = 16.dp,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    buttonShape: Shape = MaterialTheme.shapes.extraLarge,
    buttonColors: ButtonColors = ButtonDefaults.buttonColors(),
    buttonContentPadding: PaddingValues = ButtonDefaults.ContentPadding
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(
            spacing,
            Alignment.CenterVertically
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = horizontalPadding),
            style = textStyle,
            textAlign = TextAlign.Center
        )

        FilledTextButton(
            text = buttonText,
            shape = buttonShape,
            colors = buttonColors,
            contentPadding = buttonContentPadding,
            onClick = onRetry
        )
    }
}

@Composable
fun SimpleErrorLayout(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier.fillMaxSize(),
    icon: ImageVector = Icons.Default.Refresh,
    iconSize: Dp = 48.dp,
    tint: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    contentDescription: String? = null
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        SimpleIconButton(
            imageVector = icon,
            modifier = Modifier.size(iconSize),
            enabled = enabled,
            colors = colors,
            tint = tint,
            contentDescription = contentDescription,
            onClick = onRetry
        )
    }
}