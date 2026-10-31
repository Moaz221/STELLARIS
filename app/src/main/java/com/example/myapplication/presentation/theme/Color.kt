package com.example.myapplication.presentation.theme

import androidx.compose.ui.graphics.Color

// Light Theme Palette
val LungCarePrimaryLight = Color(0xFF176B67)
val LungCareSecondaryLight = Color(0xFF2F8F89)
val LungCareSoftMintLight = Color(0xFFEAF6F3)
val LungCareBackgroundLight = Color(0xFFF7F9FB)
val LungCareSurfaceLight = Color(0xFFFFFFFF)
val LungCareTextPrimaryLight = Color(0xFF1D2939)
val LungCareTextSecondaryLight = Color(0xFF667085)
val LungCareBorderLight = Color(0xFFE4E7EC)

// Status Colors
val LungCareInfo = Color(0xFF3478C5)
val LungCareWarning = Color(0xFFD99022)
val LungCareUrgent = Color(0xFFC83E4D)
val LungCareSuccess = Color(0xFF27845B)

// Dark Theme Palette
val LungCareBackgroundDark = Color(0xFF081011)
val LungCareSurfaceDark = Color(0xFF111A1B)
val LungCareElevatedDark = Color(0xFF172526)
val LungCareTextPrimaryDark = Color(0xFFF3F7F6)
val LungCareTextSecondaryDark = Color(0xFF9BADAA)
val LungCareBorderDark = Color(0xFF263334)
val LungCarePrimaryDark = Color(0xFF56C5B3)
val LungCareSecondaryDark = Color(0xFF73D2C2)
val LungCareSoftMintDark = Color(0xFF142B29)

// Legacy alias mappings for backward compatibility
val DeepSpace = LungCareBackgroundDark
val Navy = LungCareSurfaceDark
val SurfaceDark = LungCareElevatedDark
val StellarTeal = LungCarePrimaryDark
val StellarCyan = LungCareSecondaryDark
val Nebula = LungCareInfo
val Hairline = LungCareBorderDark
val TextPrimary = LungCareTextPrimaryDark
val TextSecondary = LungCareTextSecondaryDark
val TextTertiary = LungCareTextSecondaryDark.copy(alpha = 0.6f)
