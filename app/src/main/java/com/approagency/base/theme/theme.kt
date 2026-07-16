package com.approagency.base.theme

import android.view.View
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.text.layoutDirection
import com.approagency.base.presentation.BaseActivity
import java.util.Locale

val LocalBaseActivity = staticCompositionLocalOf<BaseActivity> {
    error("No BaseActivity provided")
}

@Composable
fun BaseActivity.ApproTheme(
    content: @Composable () -> Unit
) {
    val layoutDirection = remember(language) {
        if (Locale.forLanguageTag(language).layoutDirection == View.LAYOUT_DIRECTION_RTL) {
            LayoutDirection.Rtl
        } else {
            LayoutDirection.Ltr
        }
    }

    val themeMode = themeManager.themeMode.value

    val isDarkMode = themeManager.isDarkMode()

    val colorSchema = if (isDarkMode) config.darkColorSchema else config.lightColorSchema

    val providers = config.providers(isDarkMode)

    CompositionLocalProvider(
        LocalBaseActivity provides this,
        LocalLayoutDirection provides layoutDirection,
        *providers
    ) {
        MaterialTheme(
            colorScheme = colorSchema,
            typography = config.typography,
            shapes = config.shapes,
            content = content
        )
    }
}

fun createLightColorScheme(
    primaryColor: Color
) = lightColorScheme(
    primary = primaryColor,
    onPrimary = Color.White,
    primaryContainer = primaryColor.copy(alpha = 0.2f),
    onPrimaryContainer = primaryColor,

    secondary = primaryColor,
    onSecondary = Color.White,
    secondaryContainer = primaryColor.copy(alpha = 0.15f),
    onSecondaryContainer = primaryColor,

    tertiary = primaryColor,
    onTertiary = Color.White,
    tertiaryContainer = primaryColor.copy(alpha = 0.12f),
    onTertiaryContainer = primaryColor,

    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onError = Color.White,
    onErrorContainer = Color(0xFF410002),

    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),

    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),

    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),

    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),

    inverseSurface = Color(0xFF313033),
    inverseOnSurface = Color(0xFFF4EFF4),
    inversePrimary = primaryColor,

    scrim = Color.Black,
    surfaceTint = primaryColor,

    surfaceBright = Color(0xFFFFFBFE)
)

fun createDarkColorScheme(
    primaryColor: Color
) = darkColorScheme(
    primary = primaryColor,
    onPrimary = Color.Black,
    primaryContainer = primaryColor.copy(alpha = 0.35f),
    onPrimaryContainer = Color.White,

    secondary = primaryColor,
    onSecondary = Color.Black,
    secondaryContainer = primaryColor.copy(alpha = 0.25f),
    onSecondaryContainer = Color.White,

    tertiary = primaryColor,
    onTertiary = Color.Black,
    tertiaryContainer = primaryColor.copy(alpha = 0.2f),
    onTertiaryContainer = Color.White,

    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),

    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),

    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),

    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),

    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F),

    inverseSurface = Color(0xFFE6E1E5),
    inverseOnSurface = Color(0xFF313033),
    inversePrimary = primaryColor,

    scrim = Color.Black,
    surfaceTint = primaryColor,

    surfaceBright = Color(0xFF37353A)
)