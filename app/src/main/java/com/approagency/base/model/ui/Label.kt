package com.approagency.base.model.ui

import androidx.annotation.StringRes
import androidx.compose.ui.text.AnnotatedString

sealed interface Label {
    data class Resource(@param:StringRes val id: Int) : Label
    data class Text(val value: String) : Label
    data class Annotated(val value: AnnotatedString) : Label
}