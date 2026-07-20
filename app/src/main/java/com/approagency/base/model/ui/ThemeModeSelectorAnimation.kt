package com.approagency.base.model.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

data class ThemeModeSelectorAnimation(
    val colorSpec: FiniteAnimationSpec<Color>,
    val scaleSpec: FiniteAnimationSpec<Float>,
    val elevationSpec: FiniteAnimationSpec<Dp>,
    val iconSizeSpec: FiniteAnimationSpec<Dp>,
    val indicatorEnter: EnterTransition,
    val indicatorExit: ExitTransition
)