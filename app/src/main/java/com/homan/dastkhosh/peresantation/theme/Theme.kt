package com.homan.dastkhosh.peresantation.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat


private val LightColors = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    inversePrimary = DarkPrimary,

    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,

    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,

    background = LightBackground,
    onBackground = LightOnBackground,

    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceTint = LightPrimary,

    inverseSurface = Color(0xFF25332C),
    inverseOnSurface = Color(0xFFEDF5F0),

    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,

    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    scrim = Color.Black,

    surfaceBright = Color(0xFFFAFCFB),
    surfaceDim = Color(0xFFD8E1DC),

    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF2F6F4),
    surfaceContainer = Color(0xFFECF2EE),
    surfaceContainerHigh = Color(0xFFE5EDE8),
    surfaceContainerHighest = Color(0xFFDEE7E1)
)


private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    inversePrimary = LightPrimary,

    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,

    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,

    background = DarkBackground,
    onBackground = DarkOnBackground,

    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceTint = DarkPrimary,

    inverseSurface = Color(0xFFE2EBE7),
    inverseOnSurface = Color(0xFF23322A),

    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,

    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    scrim = Color.Black,

    surfaceBright = Color(0xFF33423B),
    surfaceDim = Color(0xFF0D151A),

    surfaceContainerLowest = Color(0xFF091014),
    surfaceContainerLow = Color(0xFF141F25),
    surfaceContainer = Color(0xFF19262C),
    surfaceContainerHigh = Color(0xFF223137),
    surfaceContainerHighest = Color(0xFF2B3C41)
)


val BrandGradient: Brush = Brush.linearGradient(
    colors = listOf(
        NavyDark,
        Navy,
        EmeraldDark
    )
)


private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}


@Composable
fun DastkhoshTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    darkStatusBarBackground: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findActivity()?.window

            if (window != null) {
                val controller = WindowCompat.getInsetsController(
                    window,
                    view
                )

                controller.isAppearanceLightStatusBars =
                    !darkStatusBarBackground

                controller.isAppearanceLightNavigationBars =
                    !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}