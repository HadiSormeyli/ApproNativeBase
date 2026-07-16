package com.approagency.base.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.approagency.base.R

val danaFontFamily = FontFamily(
    Font(R.font.dana_fa, weight = FontWeight.Light),
    Font(R.font.dana_regular, weight = FontWeight.Normal),
    Font(R.font.dana_medium, weight = FontWeight.Medium),
    Font(R.font.dana_bold, weight = FontWeight.SemiBold),
)

val ApproTypography: Typography
    get() = Typography(
        displaySmall = TextStyle(
            fontFamily = danaFontFamily,
            fontWeight = FontWeight.Light,
            fontSize = 36.sp
        ),
        displayMedium = TextStyle(
            fontFamily = danaFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 45.sp
        ),
        displayLarge = TextStyle(
            fontFamily = danaFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 57.sp
        ),

        headlineSmall = TextStyle(
            fontFamily = danaFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = danaFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = danaFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 32.sp
        ),

        titleSmall = TextStyle(
            fontFamily = danaFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        ),
        titleMedium = TextStyle(
            fontFamily = danaFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        ),
        titleLarge = TextStyle(
            fontFamily = danaFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp
        ),

        bodySmall = TextStyle(
            fontFamily = danaFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = danaFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = danaFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp
        ),

        labelSmall = TextStyle(
            fontFamily = danaFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp
        ),
        labelMedium = TextStyle(
            fontFamily = danaFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            letterSpacing = 0.5.sp
        ),
        labelLarge = TextStyle(
            fontFamily = danaFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    )