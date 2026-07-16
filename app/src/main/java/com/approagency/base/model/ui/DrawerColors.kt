package com.approagency.base.model.ui

import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class DrawerColors(
    val containerColor: Color,
    val contentColor: Color,
    val itemContainerColor: Color,
    val itemContentColor: Color,
    val selectedItemContainerColor: Color,
    val selectedItemContentColor: Color,
    val dividerColor: Color,
    val badgeContainerColor: Color,
    val badgeContentColor: Color,
    val disabledAlpha: Float
) {
    companion object {
        @Composable
        fun defaultColors(
            containerColor: Color = MaterialTheme.colorScheme.surface,
            contentColor: Color = MaterialTheme.colorScheme.onSurface,
            itemContainerColor: Color = Color.Transparent,
            itemContentColor: Color = MaterialTheme.colorScheme.onSurface,
            selectedItemContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
            selectedItemContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
            dividerColor: Color = DividerDefaults.color.copy(alpha = 0.5f),
            badgeContainerColor: Color = MaterialTheme.colorScheme.error,
            badgeContentColor: Color = MaterialTheme.colorScheme.onError,
            disabledAlpha: Float = 0.38f
        ) = DrawerColors(
            containerColor = containerColor,
            contentColor = contentColor,
            itemContainerColor = itemContainerColor,
            itemContentColor = itemContentColor,
            selectedItemContainerColor = selectedItemContainerColor,
            selectedItemContentColor = selectedItemContentColor,
            dividerColor = dividerColor,
            badgeContainerColor = badgeContainerColor,
            badgeContentColor = badgeContentColor,
            disabledAlpha = disabledAlpha
        )
    }
}
