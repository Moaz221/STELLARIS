package com.example.myapplication.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = LungCarePrimaryDark,
    onPrimary = LungCareBackgroundDark,
    secondary = LungCareSecondaryDark,
    onSecondary = LungCareBackgroundDark,
    tertiary = LungCareInfo,
    onTertiary = Color.White,
    background = LungCareBackgroundDark,
    onBackground = LungCareTextPrimaryDark,
    surface = LungCareSurfaceDark,
    onSurface = LungCareTextPrimaryDark,
    surfaceVariant = LungCareElevatedDark,
    onSurfaceVariant = LungCareTextSecondaryDark,
    outline = LungCareBorderDark,
    outlineVariant = LungCareBorderDark.copy(alpha = 0.5f),
    error = LungCareUrgent
)

private val LightColorScheme = lightColorScheme(
    primary = LungCarePrimaryLight,
    onPrimary = Color.White,
    secondary = LungCareSecondaryLight,
    onSecondary = Color.White,
    tertiary = LungCareInfo,
    onTertiary = Color.White,
    background = LungCareBackgroundLight,
    onBackground = LungCareTextPrimaryLight,
    surface = LungCareSurfaceLight,
    onSurface = LungCareTextPrimaryLight,
    surfaceVariant = LungCareSoftMintLight,
    onSurfaceVariant = LungCareTextSecondaryLight,
    outline = LungCareBorderLight,
    outlineVariant = LungCareBorderLight.copy(alpha = 0.5f),
    error = LungCareUrgent
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
