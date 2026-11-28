package com.example.myapplication.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

/** Picks the Arabic or English text by the app language. */
@Composable
fun loc(en: String, ar: String): String =
    if (LocalConfiguration.current.locales[0].language == "ar") ar else en

/** Non-composable version for view models and repositories. */
fun locNow(en: String, ar: String): String =
    if (java.util.Locale.getDefault().language == "ar") ar else en

fun frequencyLabel(f: String): String = when (f) {
    "Occasional" -> locNow("Occasional", "أحيانًا")
    "Frequent" -> locNow("Frequent", "متكرر")
    "Constant" -> locNow("Constant", "مستمر")
    else -> f
}
