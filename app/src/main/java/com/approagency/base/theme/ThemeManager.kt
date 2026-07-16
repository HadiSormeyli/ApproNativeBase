package com.approagency.base.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.approagency.base.local.preference.PreferencesHelper
import com.approagency.base.local.preference.PreferencesHelper.Keys

class ThemeManager(
    private val defaultMode: ThemeMode = ThemeMode.SYSTEM
) {
    private val _themeMode = mutableStateOf(
        ThemeMode.fromString(
            PreferencesHelper.read(
                Keys.THEME_MODE,
                defaultMode.name
            )
        )
    )

    val themeMode: State<ThemeMode>
        get() = _themeMode

    suspend fun setThemeMode(
        mode: ThemeMode
    ) {
        _themeMode.value = mode
        PreferencesHelper.write(
            Keys.THEME_MODE,
            mode.name
        )
    }

    fun getDefaultMode(): ThemeMode {
        return defaultMode
    }

    @Composable
    fun isDarkMode(): Boolean {
        return when (_themeMode.value) {
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
            ThemeMode.SYSTEM -> isSystemInDarkTheme()
        }
    }
}