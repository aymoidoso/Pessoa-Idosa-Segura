package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PoliceBlueDark,
    secondary = GoldAccentDark,
    tertiary = SafeGreenDark,
    error = PanicRedDark,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = LightText,
    onSecondary = DarkText,
    onTertiary = LightText,
    onError = LightText,
    onBackground = LightText,
    onSurface = LightText
)

private val LightColorScheme = lightColorScheme(
    primary = PoliceBlue,
    secondary = GoldAccent,
    tertiary = SafeGreen,
    error = PanicRed,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = LightText,
    onSecondary = DarkText,
    onTertiary = LightText,
    onError = LightText,
    onBackground = DarkText,
    onSurface = DarkText
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Allow turning dynamicColor off to enforce our high-contrast branded color scheme
    dynamicColor: Boolean = false,
    fontScale: Float = 1.0f,
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
        typography = getElderFriendlyTypography(fontScale),
        content = content
    )
}
