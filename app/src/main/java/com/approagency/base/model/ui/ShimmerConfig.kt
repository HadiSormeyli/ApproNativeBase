package com.approagency.base.model.ui

import androidx.compose.ui.graphics.Color
import com.approagency.base.config.ApproConstants

data class ShimmerConfig(
    val image: List<Color>,
    val text: List<Color>,
    val animationDuration: Int = ApproConstants.SHIMMER_ANIMATION_DURATION
)
