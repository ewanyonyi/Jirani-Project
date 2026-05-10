package com.jirani.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.jirani.app.R

private val ManropeFamily = FontFamily(
    Font(R.font.manrope_variable, FontWeight.Normal),
    Font(R.font.manrope_variable, FontWeight.Medium),
    Font(R.font.manrope_variable, FontWeight.SemiBold),
    Font(R.font.manrope_variable, FontWeight.Bold),
)

fun provideAppTypography(): Typography {
    val bodyFamily = ManropeFamily
    val displayFamily = ManropeFamily
    val baseline = Typography()

    return Typography(
        displayLarge = TextStyle(
            fontFamily = displayFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 34.sp,
            lineHeight = 40.sp,
            letterSpacing = 0.sp,
        ),
        displayMedium = TextStyle(
            fontFamily = displayFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            letterSpacing = 0.sp,
        ),
        displaySmall = TextStyle(
            fontFamily = displayFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp,
        ),
        headlineLarge = baseline.headlineLarge.copy(
            fontFamily = displayFamily,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp,
        ),
        headlineMedium = baseline.headlineMedium.copy(
            fontFamily = displayFamily,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp,
        ),
        headlineSmall = baseline.headlineSmall.copy(
            fontFamily = displayFamily,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.sp,
        ),
        titleLarge = baseline.titleLarge.copy(
            fontFamily = displayFamily,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.sp,
        ),
        titleMedium = baseline.titleMedium.copy(
            fontFamily = displayFamily,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.sp,
        ),
        titleSmall = baseline.titleSmall.copy(
            fontFamily = displayFamily,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.sp,
        ),
        bodyLarge = baseline.bodyLarge.copy(
            fontFamily = bodyFamily,
            fontSize = 17.sp,
            lineHeight = 25.sp,
            letterSpacing = 0.sp,
        ),
        bodyMedium = baseline.bodyMedium.copy(
            fontFamily = bodyFamily,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.sp,
        ),
        bodySmall = baseline.bodySmall.copy(
            fontFamily = bodyFamily,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.sp,
        ),
        labelLarge = baseline.labelLarge.copy(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.sp,
        ),
        labelMedium = baseline.labelMedium.copy(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            letterSpacing = 0.sp,
        ),
        labelSmall = baseline.labelSmall.copy(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.sp,
        ),
    )
}
