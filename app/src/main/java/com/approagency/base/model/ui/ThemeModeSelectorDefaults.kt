package com.approagency.base.model.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object ThemeModeSelectorDefaults {

    @Composable
    fun colors(
        containerColor: Color =
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),

        selectedContainerColor: Color =
            MaterialTheme.colorScheme.primaryContainer,

        unselectedContainerColor: Color =
            Color.Transparent,

        disabledSelectedContainerColor: Color =
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),

        selectedContentColor: Color =
            MaterialTheme.colorScheme.onPrimaryContainer,

        unselectedContentColor: Color =
            MaterialTheme.colorScheme.onSurfaceVariant,

        disabledContentColor: Color =
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),

        indicatorColor: Color =
            selectedContentColor
    ): ThemeModeSelectorColors {
        return ThemeModeSelectorColors(
            containerColor = containerColor,
            selectedContainerColor = selectedContainerColor,
            unselectedContainerColor = unselectedContainerColor,
            disabledSelectedContainerColor = disabledSelectedContainerColor,
            selectedContentColor = selectedContentColor,
            unselectedContentColor = unselectedContentColor,
            disabledContentColor = disabledContentColor,
            indicatorColor = indicatorColor
        )
    }

    fun shapes(
        containerShape: Shape = RoundedCornerShape(24.dp),
        itemShape: Shape = RoundedCornerShape(18.dp),
        indicatorShape: Shape = RoundedCornerShape(50)
    ): ThemeModeSelectorShapes {
        return ThemeModeSelectorShapes(
            containerShape = containerShape,
            itemShape = itemShape,
            indicatorShape = indicatorShape
        )
    }

    fun dimensions(
        containerPadding: PaddingValues = PaddingValues(6.dp),
        itemPadding: PaddingValues = PaddingValues(
            horizontal = 8.dp,
            vertical = 12.dp
        ),
        itemSpacing: Dp = 4.dp,
        selectedIconSize: Dp = 26.dp,
        unselectedIconSize: Dp = 24.dp,
        labelTopPadding: Dp = 6.dp,
        indicatorAreaHeight: Dp = 10.dp,
        indicatorWidth: Dp = 24.dp,
        indicatorHeight: Dp = 3.dp
    ): ThemeModeSelectorDimensions {
        return ThemeModeSelectorDimensions(
            containerPadding = containerPadding,
            itemPadding = itemPadding,
            itemSpacing = itemSpacing,
            selectedIconSize = selectedIconSize,
            unselectedIconSize = unselectedIconSize,
            labelTopPadding = labelTopPadding,
            indicatorAreaHeight = indicatorAreaHeight,
            indicatorWidth = indicatorWidth,
            indicatorHeight = indicatorHeight
        )
    }

    @Composable
    fun typography(
        selectedTextStyle: TextStyle =
            MaterialTheme.typography.labelLarge,

        unselectedTextStyle: TextStyle =
            MaterialTheme.typography.labelMedium
    ): ThemeModeSelectorTypography {
        return ThemeModeSelectorTypography(
            selectedTextStyle = selectedTextStyle,
            unselectedTextStyle = unselectedTextStyle
        )
    }

    fun animation(
        colorSpec: FiniteAnimationSpec<Color> = tween(
            durationMillis = 250
        ),
        scaleSpec: FiniteAnimationSpec<Float> = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        elevationSpec: FiniteAnimationSpec<Dp> = tween(
            durationMillis = 250
        ),
        iconSizeSpec: FiniteAnimationSpec<Dp> = tween(
            durationMillis = 250
        ),
        indicatorEnter: EnterTransition =
            fadeIn(
                animationSpec = tween(200)
            ) + expandHorizontally(
                animationSpec = tween(250),
                expandFrom = Alignment.CenterHorizontally
            ),
        indicatorExit: ExitTransition =
            fadeOut(
                animationSpec = tween(150)
            ) + shrinkHorizontally(
                animationSpec = tween(200),
                shrinkTowards = Alignment.CenterHorizontally
            )
    ): ThemeModeSelectorAnimation {
        return ThemeModeSelectorAnimation(
            colorSpec = colorSpec,
            scaleSpec = scaleSpec,
            elevationSpec = elevationSpec,
            iconSizeSpec = iconSizeSpec,
            indicatorEnter = indicatorEnter,
            indicatorExit = indicatorExit
        )
    }
}