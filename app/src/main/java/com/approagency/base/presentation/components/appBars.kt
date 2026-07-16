package com.approagency.base.presentation.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.approagency.base.theme.LocalBaseActivity

@Composable
fun SimpleAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
    navigationIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    navigationPainter: Painter? = null,
    navigationContentDescription: String? = null,
    navigationIconSize: Dp = 32.dp,
    navigationIconTint: Color = MaterialTheme.colorScheme.onSurface,
    horizontalSpacing: Dp = 4.dp,
    textStyle: TextStyle = MaterialTheme.typography.titleLarge,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        onBackClick?.let {
            if (navigationPainter != null) {
                SimpleIconButton(
                    painter = navigationPainter,
                    modifier = Modifier.size(navigationIconSize),
                    contentDescription = navigationContentDescription,
                    tint = navigationIconTint,
                    onClick = it
                )
            } else {
                SimpleIconButton(
                    imageVector = navigationIcon,
                    modifier = Modifier.size(navigationIconSize),
                    contentDescription = navigationContentDescription,
                    tint = navigationIconTint,
                    onClick = it
                )
            }
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = textStyle,
            color = textColor
        )
        action?.invoke()
    }
}

@Composable
fun DrawerAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
    horizontalSpacing: Dp = 8.dp,
    textStyle: TextStyle = MaterialTheme.typography.titleLarge,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    drawerIconTint: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SimpleAppBar(
            title = title,
            modifier = Modifier.weight(1f),
            onBackClick = onBackClick,
            action = action,
            textStyle = textStyle,
            textColor = textColor
        )
        DrawerIconButton(tint = drawerIconTint)
    }
}

@Composable
fun DrawerIconButton(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    openIcon: ImageVector = Icons.Default.Menu,
    closeIcon: ImageVector = Icons.Default.Close,
    openContentDescription: String? = null,
    closeContentDescription: String? = null,
    animationDuration: Int = 250
) {
    val activity = LocalBaseActivity.current
    val isOpen = activity.drawerState.targetValue == DrawerValue.Open
    val rotation by animateFloatAsState(
        targetValue = if (isOpen) 180f else 0f,
        animationSpec = tween(animationDuration),
        label = "drawerRotation"
    )
    Crossfade(
        targetState = isOpen,
        animationSpec = tween(animationDuration),
        label = "drawerIcon"
    ) { opened ->
        SimpleIconButton(
            modifier = modifier
                .size(size)
                .rotate(rotation),
            imageVector = if (opened) closeIcon else openIcon,
            contentDescription = if (opened) closeContentDescription else openContentDescription,
            tint = tint,
            onClick = {
                if (opened) activity.closeDrawer() else activity.openDrawer()
            }
        )
    }
}