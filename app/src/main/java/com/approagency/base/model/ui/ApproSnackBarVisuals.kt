package com.approagency.base.model.ui

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals

data class ApproSnackBarVisuals(
    override val message: String,
    val type: SnackBarType = SnackBarType.SIMPLE,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean,
    override val duration: SnackbarDuration,
    val onActionClick: (() -> Unit)? = null
) : SnackbarVisuals