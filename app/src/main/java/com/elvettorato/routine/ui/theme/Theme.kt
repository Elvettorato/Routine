package com.elvettorato.routine.ui.theme

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LineageDarkColorScheme = darkColorScheme(
    primary = LineagePrimary,
    onPrimary = LineageOnPrimary,
    primaryContainer = LineagePrimaryContainer,
    onPrimaryContainer = LineageOnPrimaryContainer,
    secondary = LineageSecondary,
    onSecondary = LineageOnSecondary,
    secondaryContainer = LineageSecondaryContainer,
    onSecondaryContainer = LineageOnSecondaryContainer,
    tertiary = LineageTertiary,
    onTertiary = LineageOnTertiary,
    tertiaryContainer = LineageTertiaryContainer,
    onTertiaryContainer = LineageOnTertiaryContainer,
    error = LineageError,
    onError = LineageOnError,
    errorContainer = LineageErrorContainer,
    onErrorContainer = LineageOnErrorContainer,
    background = LineageBackground,
    onBackground = LineageOnBackground,
    surface = LineageSurface,
    onSurface = LineageOnSurface,
    surfaceVariant = LineageSurfaceVariant,
    onSurfaceVariant = LineageOnSurfaceVariant,
    outline = LineageOutline,
    outlineVariant = LineageOutlineVariant,
    inverseSurface = LineageInverseSurface,
    inverseOnSurface = LineageInverseOnSurface,
    inversePrimary = LineageInversePrimary,
    scrim = LineageScrim
)

private val LineageLightColorScheme = lightColorScheme(
    primary = LineageLightPrimary,
    onPrimary = LineageLightOnPrimary,
    primaryContainer = LineageLightPrimaryContainer,
    onPrimaryContainer = LineageLightOnPrimaryContainer,
    secondary = LineageLightSecondary,
    onSecondary = LineageLightOnSecondary,
    secondaryContainer = LineageLightSecondaryContainer,
    onSecondaryContainer = LineageLightOnSecondaryContainer,
    tertiary = LineageLightTertiary,
    onTertiary = LineageLightOnTertiary,
    tertiaryContainer = LineageLightTertiaryContainer,
    onTertiaryContainer = LineageLightOnTertiaryContainer,
    error = LineageLightError,
    onError = LineageLightOnError,
    errorContainer = LineageLightErrorContainer,
    onErrorContainer = LineageLightOnErrorContainer,
    background = LineageLightBackground,
    onBackground = LineageLightOnBackground,
    surface = LineageLightSurface,
    onSurface = LineageLightOnSurface,
    surfaceVariant = LineageLightSurfaceVariant,
    onSurfaceVariant = LineageLightOnSurfaceVariant,
    outline = LineageLightOutline,
    outlineVariant = LineageLightOutlineVariant,
    inverseSurface = LineageLightInverseSurface,
    inverseOnSurface = LineageLightInverseOnSurface,
    inversePrimary = LineagePrimary,
    scrim = LineageScrim
)

@Composable
fun RoutineTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    val colorScheme = if (Build.VERSION.SDK_INT >= 31) {
        if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (isDark) LineageDarkColorScheme else LineageLightColorScheme
    }

    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as ComponentActivity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LineageTypography,
        content = content
    )
}
