package com.approagency.base.model

import androidx.compose.ui.graphics.Color
import com.approagency.base.utils.withTone

class TonalPalette(
    private val seedColor: Color
) {
    fun tone(value: Int): Color {
        return seedColor.withTone(value.toFloat())
    }
}