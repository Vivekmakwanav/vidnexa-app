package com.example.ui.theme

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
    primary = IndigoLight,
    secondary = VioletLight,
    tertiary = PinkLight,
    background = CleanBackgroundDark,
    surface = CleanSurfaceDark,
    surfaceVariant = CleanSurfaceVariantDark,
    onPrimary = CharcoalDark,
    onSecondary = CharcoalDark,
    onTertiary = CharcoalDark,
    onBackground = TextDarkMain,
    onSurface = TextDarkMain,
    onSurfaceVariant = TextDarkSecondary,
    error = CoralRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Indigo600,
    secondary = Violet600,
    tertiary = Pink500,
    background = CleanBackgroundLight,
    surface = CleanSurfaceLight,
    surfaceVariant = CleanSurfaceVariantLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = CharcoalDark,
    onSurface = CharcoalDark,
    onSurfaceVariant = CharcoalLight,
    error = CoralRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Allow users to force dark theme or honor system setting
    dynamicColor: Boolean = false, // Set to false to preserve our premium hand-crafted brand branding colors!
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
