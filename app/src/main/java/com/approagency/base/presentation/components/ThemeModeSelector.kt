package com.approagency.base.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.SettingsBrightness
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.approagency.base.model.ui.ThemeModeOption
import com.approagency.base.model.ui.ThemeModeSelectorAnimation
import com.approagency.base.model.ui.ThemeModeSelectorColors
import com.approagency.base.model.ui.ThemeModeSelectorDefaults
import com.approagency.base.model.ui.ThemeModeSelectorDimensions
import com.approagency.base.model.ui.ThemeModeSelectorShapes
import com.approagency.base.model.ui.ThemeModeSelectorTypography
import com.approagency.base.theme.ThemeMode
import com.approagency.base.utils.Icon
import com.approagency.base.model.ui.Icon as AppIcon

@Composable
fun ThemeModeSelector(
    selectedMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,

    options: List<ThemeModeOption>? = null,

    colors: ThemeModeSelectorColors =
        ThemeModeSelectorDefaults.colors(),

    shapes: ThemeModeSelectorShapes =
        ThemeModeSelectorDefaults.shapes(),

    dimensions: ThemeModeSelectorDimensions =
        ThemeModeSelectorDefaults.dimensions(),

    typography: ThemeModeSelectorTypography =
        ThemeModeSelectorDefaults.typography(),

    animation: ThemeModeSelectorAnimation =
        ThemeModeSelectorDefaults.animation(),

    containerTonalElevation: Dp = 1.dp,
    selectedItemElevation: Dp = 3.dp,
    unselectedItemElevation: Dp = 0.dp,

    selectedScale: Float = 1f,
    unselectedScale: Float = 0.94f,

    showLabel: Boolean = true,
    showIndicator: Boolean = true,
    allowReselect: Boolean = false,

    itemHorizontalAlignment: Alignment.Horizontal =
        Alignment.CenterHorizontally,

    labelProvider: (ThemeModeOption) -> String = {
        it.mode.persianLabel
    },

    itemContent: (
    @Composable ColumnScope.(
        option: ThemeModeOption,
        selected: Boolean,
        enabled: Boolean,
        contentColor: Color,
        iconSize: Dp
    ) -> Unit
    )? = null,

    indicatorContent: (
    @Composable BoxScope.(
        option: ThemeModeOption,
        indicatorColor: Color
    ) -> Unit
    )? = null
) {
    val resolvedOptions = options ?: remember {
        listOf(
            ThemeModeOption(
                mode = ThemeMode.SYSTEM,
                icon = AppIcon.Vector(
                    Icons.Rounded.SettingsBrightness
                )
            ),
            ThemeModeOption(
                mode = ThemeMode.DARK,
                icon = AppIcon.Vector(
                    Icons.Rounded.DarkMode
                )
            ),
            ThemeModeOption(
                mode = ThemeMode.LIGHT,
                icon = AppIcon.Vector(
                    Icons.Rounded.LightMode
                )
            )
        )
    }

    Surface(
        modifier = modifier,
        shape = shapes.containerShape,
        color = colors.containerColor,
        tonalElevation = containerTonalElevation
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup()
                .padding(dimensions.containerPadding),
            horizontalArrangement = Arrangement.spacedBy(
                dimensions.itemSpacing
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            resolvedOptions.forEach { option ->
                ThemeModeItem(
                    option = option,
                    selected = selectedMode == option.mode,
                    enabled = enabled,
                    colors = colors,
                    shapes = shapes,
                    dimensions = dimensions,
                    typography = typography,
                    animation = animation,
                    selectedItemElevation = selectedItemElevation,
                    unselectedItemElevation = unselectedItemElevation,
                    selectedScale = selectedScale,
                    unselectedScale = unselectedScale,
                    showLabel = showLabel,
                    showIndicator = showIndicator,
                    itemHorizontalAlignment = itemHorizontalAlignment,
                    labelProvider = labelProvider,
                    itemContent = itemContent,
                    indicatorContent = indicatorContent,
                    onClick = {
                        if (
                            allowReselect ||
                            selectedMode != option.mode
                        ) {
                            onModeSelected(option.mode)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun RowScope.ThemeModeItem(
    option: ThemeModeOption,
    selected: Boolean,
    enabled: Boolean,
    colors: ThemeModeSelectorColors,
    shapes: ThemeModeSelectorShapes,
    dimensions: ThemeModeSelectorDimensions,
    typography: ThemeModeSelectorTypography,
    animation: ThemeModeSelectorAnimation,
    selectedItemElevation: Dp,
    unselectedItemElevation: Dp,
    selectedScale: Float,
    unselectedScale: Float,
    showLabel: Boolean,
    showIndicator: Boolean,
    itemHorizontalAlignment: Alignment.Horizontal,
    labelProvider: (ThemeModeOption) -> String,
    itemContent: (
    @Composable ColumnScope.(
        option: ThemeModeOption,
        selected: Boolean,
        enabled: Boolean,
        contentColor: Color,
        iconSize: Dp
    ) -> Unit
    )?,
    indicatorContent: (
    @Composable BoxScope.(
        option: ThemeModeOption,
        indicatorColor: Color
    ) -> Unit
    )?,
    onClick: () -> Unit
) {
    val targetContainerColor = when {
        !enabled && selected ->
            colors.disabledSelectedContainerColor

        selected ->
            colors.selectedContainerColor

        else ->
            colors.unselectedContainerColor
    }

    val targetContentColor = when {
        !enabled ->
            colors.disabledContentColor

        selected ->
            colors.selectedContentColor

        else ->
            colors.unselectedContentColor
    }

    val containerColor by animateColorAsState(
        targetValue = targetContainerColor,
        animationSpec = animation.colorSpec,
        label = "themeModeContainerColor"
    )

    val contentColor by animateColorAsState(
        targetValue = targetContentColor,
        animationSpec = animation.colorSpec,
        label = "themeModeContentColor"
    )

    val scale by animateFloatAsState(
        targetValue = if (selected) {
            selectedScale
        } else {
            unselectedScale
        },
        animationSpec = animation.scaleSpec,
        label = "themeModeScale"
    )

    val elevation by animateDpAsState(
        targetValue = if (selected) {
            selectedItemElevation
        } else {
            unselectedItemElevation
        },
        animationSpec = animation.elevationSpec,
        label = "themeModeElevation"
    )

    val iconSize by animateDpAsState(
        targetValue = if (selected) {
            dimensions.selectedIconSize
        } else {
            dimensions.unselectedIconSize
        },
        animationSpec = animation.iconSizeSpec,
        label = "themeModeIconSize"
    )

    Surface(
        modifier = Modifier
            .weight(1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick
            ),
        shape = shapes.itemShape,
        color = containerColor,
        tonalElevation = elevation
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.itemPadding),
            horizontalAlignment = itemHorizontalAlignment
        ) {
            if (itemContent != null) {
                itemContent(
                    option,
                    selected,
                    enabled,
                    contentColor,
                    iconSize
                )
            } else {
                DefaultThemeModeItemContent(
                    option = option,
                    selected = selected,
                    contentColor = contentColor,
                    iconSize = iconSize,
                    dimensions = dimensions,
                    typography = typography,
                    showLabel = showLabel,
                    labelProvider = labelProvider
                )
            }

            if (showIndicator) {
                ThemeModeIndicator(
                    option = option,
                    visible = selected,
                    color = colors.indicatorColor,
                    shapes = shapes,
                    dimensions = dimensions,
                    animation = animation,
                    content = indicatorContent
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.DefaultThemeModeItemContent(
    option: ThemeModeOption,
    selected: Boolean,
    contentColor: Color,
    iconSize: Dp,
    dimensions: ThemeModeSelectorDimensions,
    typography: ThemeModeSelectorTypography,
    showLabel: Boolean,
    labelProvider: (ThemeModeOption) -> String
) {
    option.icon.Icon(
        contentDescription = null,
        modifier = Modifier.size(iconSize),
        tint = contentColor
    )

    if (showLabel) {
        Text(
            text = labelProvider(option),
            modifier = Modifier.padding(
                top = dimensions.labelTopPadding
            ),
            color = contentColor,
            style = if (selected) {
                typography.selectedTextStyle
            } else {
                typography.unselectedTextStyle
            },
            maxLines = 1
        )
    }
}

@Composable
private fun ColumnScope.ThemeModeIndicator(
    option: ThemeModeOption,
    visible: Boolean,
    color: Color,
    shapes: ThemeModeSelectorShapes,
    dimensions: ThemeModeSelectorDimensions,
    animation: ThemeModeSelectorAnimation,
    content: (
    @Composable BoxScope.(
        option: ThemeModeOption,
        indicatorColor: Color
    ) -> Unit
    )?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensions.indicatorAreaHeight),
        contentAlignment = Alignment.BottomCenter
    ) {
        androidx.compose.animation.AnimatedVisibility(
            visible = visible,
            enter = animation.indicatorEnter,
            exit = animation.indicatorExit
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                if (content != null) {
                    content(
                        option,
                        color
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .width(dimensions.indicatorWidth)
                            .height(dimensions.indicatorHeight)
                            .clip(shapes.indicatorShape)
                            .background(color)
                    )
                }
            }
        }
    }
}