package com.example.myapplication.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

/** Picks the Arabic or English text by the app language. */
@Composable
fun loc(en: String, ar: String): String =
    if (LocalConfiguration.current.locales[0].language == "ar") ar else en
