package com.jirani.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val lightScheme = lightColorScheme(
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
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = JiraniBackground,
    onBackground = JiraniCharcoal,
    surface = JiraniSurface,
    onSurface = JiraniCharcoal,
    surfaceVariant = JiraniSurfaceWarm,
    onSurfaceVariant = JiraniGray,
    outline = JiraniOutline,
    outlineVariant = Color(0xFFE2D6C6),
    scrim = Color.Black,
    inverseSurface = Color(0xFF2A302C),
    inverseOnSurface = Color(0xFFEFF1EC),
    inversePrimary = JiraniGreen80,
    surfaceDim = Color(0xFFE4DDD3),
    surfaceBright = JiraniSurface,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFFF8EF),
    surfaceContainer = JiraniBackground,
    surfaceContainerHigh = Color(0xFFF7F0E7),
    surfaceContainerHighest = JiraniSurfaceWarm,
)

private val darkScheme = darkColorScheme(
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
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = JiraniDarkBackground,
    onBackground = Color(0xFFE8EFE8),
    surface = JiraniDarkSurface,
    onSurface = Color(0xFFE8EFE8),
    surfaceVariant = JiraniDarkSurfaceMuted,
    onSurfaceVariant = Color(0xFFDDE5DD),
    outline = Color(0xFF748076),
    outlineVariant = Color(0xFF3E4941),
    scrim = Color.Black,
    inverseSurface = Color(0xFFE8EFE8),
    inverseOnSurface = Color(0xFF2D332F),
    inversePrimary = JiraniGreen,
    surfaceDim = JiraniDarkBackground,
    surfaceBright = Color(0xFF363E38),
    surfaceContainerLowest = Color(0xFF0B0F0D),
    surfaceContainerLow = Color(0xFF161D18),
    surfaceContainer = JiraniDarkSurface,
    surfaceContainerHigh = JiraniDarkSurfaceMuted,
    surfaceContainerHighest = Color(0xFF303B33),
)

@Immutable
data class ColorFamily(
    val color: Color,
    val onColor: Color,
    val colorContainer: Color,
    val onColorContainer: Color,
)

val unspecified_scheme = ColorFamily(
    Color.Unspecified,
    Color.Unspecified,
    Color.Unspecified,
    Color.Unspecified,
)

@Composable
fun JiraniTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+ but defaults to false to preserve the Jirani palette.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkScheme
        else -> lightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = provideAppTypography(context),
        shapes = AppShapes,
        content = content
    )
}

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    JiraniTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        content = content,
    )
}
