package com.homan.dastkhosh.peresantation.theme

import androidx.compose.material3.Typography as MaterialTypography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val AppFontFamily = FontFamily.Default

private val DefaultTypography = MaterialTypography()

val Typography = DefaultTypography.copy(
    displayLarge = DefaultTypography.displayLarge.copy(
        fontFamily = AppFontFamily,
        letterSpacing = 0.sp
    ),
    displayMedium = DefaultTypography.displayMedium.copy(
        fontFamily = AppFontFamily,
        letterSpacing = 0.sp
    ),
    displaySmall = DefaultTypography.displaySmall.copy(
        fontFamily = AppFontFamily,
        letterSpacing = 0.sp
    ),

    headlineLarge = DefaultTypography.headlineLarge.copy(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp
    ),
    headlineMedium = DefaultTypography.headlineMedium.copy(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp
    ),
    headlineSmall = DefaultTypography.headlineSmall.copy(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp
    ),

    titleLarge = DefaultTypography.titleLarge.copy(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp
    ),
    titleMedium = DefaultTypography.titleMedium.copy(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp
    ),
    titleSmall = DefaultTypography.titleSmall.copy(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp
    ),

    bodyLarge = DefaultTypography.bodyLarge.copy(
        fontFamily = AppFontFamily,
        fontSize = 16.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = DefaultTypography.bodyMedium.copy(
        fontFamily = AppFontFamily,
        fontSize = 14.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    bodySmall = DefaultTypography.bodySmall.copy(
        fontFamily = AppFontFamily,
        fontSize = 12.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),

    labelLarge = DefaultTypography.labelLarge.copy(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        letterSpacing = 0.sp
    ),
    labelMedium = DefaultTypography.labelMedium.copy(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp
    ),
    labelSmall = DefaultTypography.labelSmall.copy(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp
    )
)