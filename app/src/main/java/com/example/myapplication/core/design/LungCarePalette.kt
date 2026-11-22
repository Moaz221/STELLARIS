package com.example.myapplication.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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

val LightLungCarePalette = LungCarePalette(
    background = Color(0xFFF7F9FB), surface = Color(0xFFFFFFFF), elevated = Color(0xFFFFFFFF),
    textPrimary = Color(0xFF1D2939), textSecondary = Color(0xFF667085), border = Color(0xFFE4E7EC),
    primary = Color(0xFF176B67), onPrimary = Color(0xFFFFFFFF), softMint = Color(0xFFEAF6F3),
    success = Color(0xFF27845B), warning = Color(0xFFD99022), urgent = Color(0xFFC83E4D), info = Color(0xFF3478C5)
)

val DarkLungCarePalette = LungCarePalette(
    background = Color(0xFF081011), surface = Color(0xFF111A1B), elevated = Color(0xFF172526),
    textPrimary = Color(0xFFF3F7F6), textSecondary = Color(0xFF9BADAA), border = Color(0xFF263334),
    primary = Color(0xFF56C5B3), onPrimary = Color(0xFF052E2B), softMint = Color(0xFF142B29),
    success = Color(0xFF5BD6A2), warning = Color(0xFFE8B15C), urgent = Color(0xFFFF8A94), info = Color(0xFF7FB5E8)
)

@Composable
fun lungCarePalette(): LungCarePalette =
    if (isSystemInDarkTheme()) DarkLungCarePalette else LightLungCarePalette
