package com.approagency.base.config

import android.net.Uri
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.graphics.Color
import androidx.core.net.toUri
import com.approagency.base.model.ui.LegalConfig
import com.approagency.base.model.ui.ShimmerConfig
import com.approagency.base.theme.ApproShapes
import com.approagency.base.theme.ApproTypography
import com.approagency.base.theme.ThemeMode
import com.approagency.base.theme.createDarkColorScheme
import com.approagency.base.theme.createLightColorScheme
import java.util.Locale

data class ApproConfig(
    val packageName: String,
    val flavor: Flavor,
    val paymentRsaKey: String,
    val versionName: String,
    val versionCode: Int,
    val debug: Boolean,
    val logEnabled: Boolean = debug,
    val storeLink: String? = null,
    val deepLink: String = "",
    val legalConfig: LegalConfig,
    val deepLinks: List<String> = listOf(deepLink),
    val isPaymentAvailable: Boolean = paymentRsaKey.isNotEmpty(),
    val defaultLocale: Locale = Locale.forLanguageTag("fa-IR"),
    val lightColorSchema: ColorScheme = createLightColorScheme(Color(0xFF6750A4)),
    val darkColorSchema: ColorScheme = createDarkColorScheme(Color(0xFFD0BCFF)),
    val typography: Typography = ApproTypography,
    val shapes: Shapes = ApproShapes,
    val shimmerConfig: ShimmerConfig? = null,
    val defaultThemeMode: ThemeMode = ThemeMode.SYSTEM,
    val providers: @Composable (isDarkMode: Boolean) -> Array<ProvidedValue<*>> = { emptyArray() },
    val extra: Map<String, Any?> = emptyMap()
) {
    fun isInternalLink(uri: Uri): Boolean {
        return deepLinks
            .filter(String::isNotBlank)
            .any { link ->
                val configuredUri = link.toUri()

                configuredUri.scheme.equals(uri.scheme, ignoreCase = true) &&
                        (
                                configuredUri.host.isNullOrBlank() ||
                                        configuredUri.host.equals(uri.host, ignoreCase = true)
                                ) &&
                        (
                                configuredUri.path.isNullOrBlank() ||
                                        uri.path.orEmpty().startsWith(
                                            configuredUri.path.orEmpty().trimEnd('/')
                                        )
                                )
            }
    }

    fun isMyket() = flavor == Flavor.MYKET
    fun isBazaar() = flavor == Flavor.BAZAR
    fun isGooglePlay() = flavor == Flavor.GOOGLE_PLAY
}
