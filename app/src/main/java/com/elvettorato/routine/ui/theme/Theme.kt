package com.elvettorato.routine.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LineageColorScheme = darkColorScheme(
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

@Composable
fun RoutineTheme(content: @Composable () -> Unit) {
    val colorScheme = LineageColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LineageTypography,
        content = content
    )
}
