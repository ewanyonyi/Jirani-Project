package com.jirani.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = JiraniGreen80,
    onPrimary = JiraniDarkBackground,
    primaryContainer = JiraniGreenDark,
    onPrimaryContainer = JiraniGreenContainer,
    secondary = JiraniSage80,
    onSecondary = JiraniDarkBackground,
    secondaryContainer = JiraniDarkSurfaceMuted,
    onSecondaryContainer = JiraniSageContainer,
    tertiary = JiraniTerracotta80,
    onTertiary = JiraniDarkBackground,
    tertiaryContainer = Color(0xFF5E2F1D),
    onTertiaryContainer = JiraniTerracottaContainer,
    background = JiraniDarkBackground,
    onBackground = Color(0xFFE8EFE8),
    surface = JiraniDarkSurface,
    onSurface = Color(0xFFE8EFE8),
    surfaceVariant = JiraniDarkSurfaceMuted,
    onSurfaceVariant = Color(0xFFC8D0C8),
    outline = Color(0xFF748076),
    outlineVariant = Color(0xFF3E4941),
    error = JiraniTerracotta80,
)

private val LightColorScheme = lightColorScheme(
    primary = JiraniGreen,
    onPrimary = Color.White,
    primaryContainer = JiraniGreenContainer,
    onPrimaryContainer = JiraniOnGreenContainer,
    secondary = JiraniSage,
    onSecondary = Color.White,
    secondaryContainer = JiraniSageContainer,
    onSecondaryContainer = Color(0xFF1F2D1E),
    tertiary = JiraniTerracotta,
    onTertiary = Color.White,
    tertiaryContainer = JiraniTerracottaContainer,
    onTertiaryContainer = Color(0xFF3B1F12),
    background = JiraniBackground,
    onBackground = JiraniCharcoal,
    surface = JiraniSurface,
    onSurface = JiraniCharcoal,
    surfaceVariant = JiraniSurfaceWarm,
    onSurfaceVariant = JiraniGray,
    outline = JiraniOutline,
    outlineVariant = Color(0xFFE2D6C6),
    error = Color(0xFFB3261E),
)

@Composable
fun JiraniTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = provideAppTypography(),
        shapes = Shapes,
        content = content
    )
}
