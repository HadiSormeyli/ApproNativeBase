package com.approagency.base.model.ui

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.vector.ImageVector

sealed interface Icon {
    data class Resource(@param:DrawableRes val id: Int) : Icon
    data class Vector(val imageVector: ImageVector) : Icon
}