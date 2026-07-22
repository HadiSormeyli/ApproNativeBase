package com.approagency.base.theme

import android.view.View
import androidx.compose.material3.ColorScheme
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
import com.approagency.base.utils.generatePalettes
import com.approagency.base.utils.hslToColor
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
): ColorScheme {
    val palettes = generatePalettes(primaryColor)

    val primary = palettes.primary
    val secondary = palettes.secondary
    val tertiary = palettes.tertiary
    val neutral = palettes.neutral
    val neutralVariant = palettes.neutralVariant
    val error = palettes.error

    return lightColorScheme(
        primary = primary.tone(40),
        onPrimary = primary.tone(100),
        primaryContainer = primary.tone(90),
        onPrimaryContainer = primary.tone(10),
        inversePrimary = primary.tone(80),

        secondary = secondary.tone(40),
        onSecondary = secondary.tone(100),
        secondaryContainer = secondary.tone(90),
        onSecondaryContainer = secondary.tone(10),

        tertiary = tertiary.tone(40),
        onTertiary = tertiary.tone(100),
        tertiaryContainer = tertiary.tone(90),
        onTertiaryContainer = tertiary.tone(10),

        background = neutral.tone(98),
        onBackground = neutral.tone(10),

        surface = neutral.tone(98),
        onSurface = neutral.tone(10),

        surfaceVariant = neutralVariant.tone(90),
        onSurfaceVariant = neutralVariant.tone(30),

        surfaceTint = primary.tone(40),

        inverseSurface = neutral.tone(20),
        inverseOnSurface = neutral.tone(95),

        error = error.tone(40),
        onError = error.tone(100),
        errorContainer = error.tone(90),
        onErrorContainer = error.tone(10),

        outline = neutralVariant.tone(50),
        outlineVariant = neutralVariant.tone(80),

        scrim = Color.Black,

        surfaceBright = neutral.tone(98),
        surfaceDim = neutral.tone(87),

        surfaceContainerLowest = neutral.tone(100),
        surfaceContainerLow = neutral.tone(96),
        surfaceContainer = neutral.tone(94),
        surfaceContainerHigh = neutral.tone(92),
        surfaceContainerHighest = neutral.tone(90),

        primaryFixed = primary.tone(90),
        primaryFixedDim = primary.tone(80),
        onPrimaryFixed = primary.tone(10),
        onPrimaryFixedVariant = primary.tone(30),

        secondaryFixed = secondary.tone(90),
        secondaryFixedDim = secondary.tone(80),
        onSecondaryFixed = secondary.tone(10),
        onSecondaryFixedVariant = secondary.tone(30),

        tertiaryFixed = tertiary.tone(90),
        tertiaryFixedDim = tertiary.tone(80),
        onTertiaryFixed = tertiary.tone(10),
        onTertiaryFixedVariant = tertiary.tone(30)
    )
}

fun createDarkColorScheme(
    primaryColor: Color
): ColorScheme {
    val palettes = generatePalettes(primaryColor)

    val primary = palettes.primary
    val secondary = palettes.secondary
    val tertiary = palettes.tertiary
    val neutral = palettes.neutral
    val neutralVariant = palettes.neutralVariant
    val error = palettes.error

    return darkColorScheme(
        primary = primaryColor,
        onPrimary = primary.tone(20),
        primaryContainer = primary.tone(30),
        onPrimaryContainer = primary.tone(90),
        inversePrimary = primary.tone(40),

        secondary = secondary.tone(80),
        onSecondary = secondary.tone(20),
        secondaryContainer = secondary.tone(30),
        onSecondaryContainer = secondary.tone(90),

        tertiary = tertiary.tone(80),
        onTertiary = tertiary.tone(20),
        tertiaryContainer = tertiary.tone(30),
        onTertiaryContainer = tertiary.tone(90),

        background = neutral.tone(6),
        onBackground = neutral.tone(90),

        surface = neutral.tone(6),
        onSurface = neutral.tone(90),

        surfaceVariant = neutralVariant.tone(30),
        onSurfaceVariant = neutralVariant.tone(80),

        surfaceTint = primary.tone(80),

        inverseSurface = neutral.tone(90),
        inverseOnSurface = neutral.tone(20),

        error = error.tone(80),
        onError = error.tone(20),
        errorContainer = error.tone(30),
        onErrorContainer = error.tone(90),

        outline = neutralVariant.tone(60),
        outlineVariant = neutralVariant.tone(30),

        scrim = Color.Black,

        surfaceBright = neutral.tone(24),
        surfaceDim = neutral.tone(6),

        surfaceContainerLowest = neutral.tone(4),
        surfaceContainerLow = neutral.tone(10),
        surfaceContainer = neutral.tone(12),
        surfaceContainerHigh = neutral.tone(17),
        surfaceContainerHighest = neutral.tone(22),

        primaryFixed = primary.tone(90),
        primaryFixedDim = primary.tone(80),
        onPrimaryFixed = primary.tone(10),
        onPrimaryFixedVariant = primary.tone(30),

        secondaryFixed = secondary.tone(90),
        secondaryFixedDim = secondary.tone(80),
        onSecondaryFixed = secondary.tone(10),
        onSecondaryFixedVariant = secondary.tone(30),

        tertiaryFixed = tertiary.tone(90),
        tertiaryFixedDim = tertiary.tone(80),
        onTertiaryFixed = tertiary.tone(10),
        onTertiaryFixedVariant = tertiary.tone(30)
    )
}
