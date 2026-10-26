package com.equipo22.pagoflex.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = OrangePrimary,
    onPrimary = CreamSurface,
    primaryContainer = OrangeLight,
    onPrimaryContainer = OrangeDark,
    secondary = OrangeDark,
    onSecondary = CreamSurface,
    background = CreamBackground,
    onBackground = Ink,
    surface = CreamSurface,
    onSurface = Ink,
    surfaceVariant = ColorTokens.lightVariant,
    onSurfaceVariant = Muted,
    error = Danger
)

private val DarkColors = darkColorScheme(
    primary = NightOrange,
    onPrimary = NightBackground,
    primaryContainer = OrangeDark,
    onPrimaryContainer = CreamBackground,
    secondary = OrangeLight,
    onSecondary = NightBackground,
    background = NightBackground,
    onBackground = CreamBackground,
    surface = NightSurface,
    onSurface = CreamBackground,
    surfaceVariant = ColorTokens.darkVariant,
    onSurfaceVariant = ColorTokens.darkMuted,
    error = ColorTokens.darkError
)

private object ColorTokens {
    val lightVariant = androidx.compose.ui.graphics.Color(0xFFF4E9DD)
    val darkVariant = androidx.compose.ui.graphics.Color(0xFF2A2A2A)
    val darkMuted = androidx.compose.ui.graphics.Color(0xFFBDBDBD)
    val darkError = androidx.compose.ui.graphics.Color(0xFFFF8A80)
}

@Composable
fun PagoFlexTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colors.background.toArgb()
            window.navigationBarColor = colors.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        content = content
    )
}
