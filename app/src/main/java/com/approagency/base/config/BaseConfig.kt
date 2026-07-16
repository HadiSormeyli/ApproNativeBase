package com.approagency.base.config

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.graphics.Color
import com.approagency.base.model.ui.ShimmerColors
import com.approagency.base.theme.ApproShapes
import com.approagency.base.theme.ApproTypography
import com.approagency.base.theme.ThemeMode
import com.approagency.base.theme.createDarkColorScheme
import com.approagency.base.theme.createLightColorScheme
import java.util.Locale

data class BaseConfig(
    val applicationPackage: String,
    val flavor: Flavor,
    val storeLink: String,
    val paymentRsaKey: String,
    val appVersionName: String,
    val appVersionCode: Int,
    val isStoreAvailable: Boolean = true,
    val defaultLocale: Locale = Locale.forLanguageTag("fa-IR"),
    val lightColorSchema: ColorScheme = createLightColorScheme(Color(0xFF6750A4)),
    val darkColorSchema: ColorScheme = createDarkColorScheme(Color(0xFFD0BCFF)),
    val typography: Typography = ApproTypography,
    val shapes: Shapes = ApproShapes,
    val debug: Boolean = false,
    val shimmerColors: ShimmerColors? = null,
    val defaultThemeMode: ThemeMode = ThemeMode.SYSTEM,
    val providers: @Composable (isDarkMode: Boolean) -> Array<ProvidedValue<*>> = { emptyArray() },
    val extra: Map<String, Any?> = emptyMap()
)
