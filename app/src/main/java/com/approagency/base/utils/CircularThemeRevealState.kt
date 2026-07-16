package com.approagency.base.utils

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.layer.GraphicsLayer

@Stable
class CircularThemeRevealState internal constructor(
    internal val graphicsLayer: GraphicsLayer,
    initialDarkTheme: Boolean
) {
    internal var targetDarkTheme by mutableStateOf(initialDarkTheme)
    internal var requestId by mutableIntStateOf(0)
    internal var containerPosition by mutableStateOf(Offset.Zero)
    internal var origin by mutableStateOf(Offset.Zero)
    internal var isAnimating by mutableStateOf(false)

    fun toggle() {
        if (!isAnimating) {
            targetDarkTheme = !targetDarkTheme
            requestId++
        }
    }

    fun changeTo(isDarkTheme: Boolean) {
        if (!isAnimating && targetDarkTheme != isDarkTheme) {
            targetDarkTheme = isDarkTheme
            requestId++
        }
    }
}