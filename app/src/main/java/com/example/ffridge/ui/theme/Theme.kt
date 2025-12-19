package com.example.ffridge.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.ffridge.data.model.AppTheme

private val FrostColorScheme = lightColorScheme(
    primary = FrostPrimary,
    onPrimary = FrostOnPrimary,
    primaryContainer = FrostPrimaryVariant,
    onPrimaryContainer = FrostOnPrimary,
    secondary = FrostSecondary,
    onSecondary = FrostOnSecondary,
    secondaryContainer = FrostSecondaryVariant,
    onSecondaryContainer = FrostOnSecondary,
    tertiary = FrostTertiary,
    onTertiary = Color.White,
    tertiaryContainer = FrostTertiary.copy(alpha = 0.2f),
    onTertiaryContainer = FrostTertiary,
    error = FrostError,
    onError = FrostOnError,
    errorContainer = FrostError.copy(alpha = 0.1f),
    onErrorContainer = FrostError,
    background = FrostBackground,
    onBackground = FrostOnBackground,
    surface = FrostSurface,
    onSurface = FrostOnSurface,
    surfaceVariant = FrostSurfaceVariant,
    onSurfaceVariant = FrostOnSurfaceVariant,
    outline = FrostOnSurfaceVariant.copy(alpha = 0.5f),
    outlineVariant = FrostOnSurfaceVariant.copy(alpha = 0.2f)
)

private val MidnightColorScheme = darkColorScheme(
    primary = MidnightPrimary,
    onPrimary = MidnightOnPrimary,
    primaryContainer = MidnightPrimaryVariant,
    onPrimaryContainer = Color.White,
    secondary = MidnightSecondary,
    onSecondary = MidnightOnSecondary,
    secondaryContainer = MidnightSecondaryVariant,
    onSecondaryContainer = Color.White,
    tertiary = MidnightTertiary,
    onTertiary = Color.White,
    tertiaryContainer = MidnightTertiary.copy(alpha = 0.2f),
    onTertiaryContainer = MidnightTertiary,
    error = MidnightError,
    onError = MidnightOnError,
    errorContainer = MidnightError.copy(alpha = 0.2f),
    onErrorContainer = MidnightError,
    background = MidnightBackground,
    onBackground = MidnightOnBackground,
    surface = MidnightSurface,
    onSurface = MidnightOnSurface,
    surfaceVariant = MidnightSurfaceVariant,
    onSurfaceVariant = MidnightOnSurfaceVariant,
    outline = MidnightOnSurfaceVariant.copy(alpha = 0.5f),
    outlineVariant = MidnightOnSurfaceVariant.copy(alpha = 0.2f)
)


@Composable
fun FfridgeTheme(
    appTheme: AppTheme = AppTheme.FROST,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppTheme.FROST -> FrostColorScheme
        AppTheme.MIDNIGHT -> MidnightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()

            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = appTheme != AppTheme.MIDNIGHT
            insetsController.isAppearanceLightNavigationBars = appTheme != AppTheme.MIDNIGHT
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
