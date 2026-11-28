package com.example.myapplication.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

data class LungCarePalette(
    val background: Color,
    val surface: Color,
    val elevated: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val border: Color,
    val primary: Color,
    val onPrimary: Color,
    val softMint: Color,
    val success: Color,
    val warning: Color,
    val urgent: Color,
    val info: Color
)

// One palette for the whole app: the same blue values as HomePalette and the Material theme.
val LightLungCarePalette = LungCarePalette(
    background = Color(0xFFF3F8FC), surface = Color(0xFFFFFFFF), elevated = Color(0xFFFFFFFF),
    textPrimary = Color(0xFF0B1F33), textSecondary = Color(0xFF506A7F), border = Color(0xFFCFE2F0),
    primary = Color(0xFF0B7CC0), onPrimary = Color(0xFFFFFFFF), softMint = Color(0xFFEAF4FB),
    success = Color(0xFF16A34A), warning = Color(0xFFEA580C), urgent = Color(0xFFE53935), info = Color(0xFF2B9BD9)
)

val DarkLungCarePalette = LungCarePalette(
    background = Color(0xFF030A12), surface = Color(0xFF0C2438), elevated = Color(0xFF102A42),
    textPrimary = Color(0xFFEAF4FB), textSecondary = Color(0xFF8FA9BF), border = Color(0xFF123450),
    primary = Color(0xFF5CC8FF), onPrimary = Color(0xFF03202F), softMint = Color(0xFF0D1F3A),
    success = Color(0xFF4ADE80), warning = Color(0xFFFBBF24), urgent = Color(0xFFFF5252), info = Color(0xFF3A9FD9)
)

/** Follows the in-app dark/light switch (not the phone setting), so every screen matches. */
@Composable
fun lungCarePalette(): LungCarePalette =
    if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) DarkLungCarePalette else LightLungCarePalette
