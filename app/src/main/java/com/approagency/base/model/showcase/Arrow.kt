package com.approagency.base.model.showcase

import androidx.compose.ui.graphics.Color

data class Arrow(
    val targetFrom: Side = Side.Bottom,
    val curved: Boolean = false,
    val animationDuration: Int = 1000,
    val head: Head? = Head.TRIANGLE,
    val headSize: Float = 16f,
    val color: Color = Color.White,
    val animSize: Boolean = true
)
