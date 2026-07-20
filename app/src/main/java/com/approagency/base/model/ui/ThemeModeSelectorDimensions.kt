package com.approagency.base.model.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp

data class ThemeModeSelectorDimensions(
    val containerPadding: PaddingValues,
    val itemPadding: PaddingValues,
    val itemSpacing: Dp,
    val selectedIconSize: Dp,
    val unselectedIconSize: Dp,
    val labelTopPadding: Dp,
    val indicatorAreaHeight: Dp,
    val indicatorWidth: Dp,
    val indicatorHeight: Dp
)
