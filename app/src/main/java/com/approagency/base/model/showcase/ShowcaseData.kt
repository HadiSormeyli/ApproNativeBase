package com.approagency.base.model.showcase

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize

data class ShowcaseData(
    val size: IntSize,
    val position: Offset,
    val message: ShowcaseMsg? = null
)
