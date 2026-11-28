package com.example.myapplication.presentation.theme

import androidx.compose.ui.graphics.Color

// Light Theme Palette (Matching HomeScreen Light Palette)
val LungCarePrimaryLight = Color(0xFF0B7CC0)
val LungCareSecondaryLight = Color(0xFF2B9BD9)
val LungCareSoftMintLight = Color(0xFFEAF4FB)
val LungCareBackgroundLight = Color(0xFFF3F8FC)
val LungCareSurfaceLight = Color(0xFFFFFFFF)
val LungCareTextPrimaryLight = Color(0xFF0B1F33)
val LungCareTextSecondaryLight = Color(0xFF3A556B)
val LungCareBorderLight = Color(0xFF9DBFD8)

// Status Colors
val LungCareInfo = Color(0xFF0B7CC0)
val LungCareWarning = Color(0xFFEA580C)
val LungCareUrgent = Color(0xFFE53935)
val LungCareSuccess = Color(0xFF16A34A)

// Dark Theme Palette (Matching HomeScreen Dark Palette)
val LungCareBackgroundDark = Color(0xFF030A12)
val LungCareSurfaceDark = Color(0xFF0C2438)
val LungCareElevatedDark = Color(0xFF102A42)
val LungCareTextPrimaryDark = Color(0xFFEAF4FB)
val LungCareTextSecondaryDark = Color(0xFFB8CEDF)
val LungCareBorderDark = Color(0xFF3A6E94)
val LungCarePrimaryDark = Color(0xFF5CC8FF)
val LungCareSecondaryDark = Color(0xFF3A9FD9)
val LungCareSoftMintDark = Color(0xFF0D1F3A)

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
