package com.approagency.base.model.ui

import com.approagency.base.theme.ThemeMode

data class ThemeModeOption(
    val mode: ThemeMode,
    val icon: Icon,
    val text: () -> String = { mode.persianLabel }
)
