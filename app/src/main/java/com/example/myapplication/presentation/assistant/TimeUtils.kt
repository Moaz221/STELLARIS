package com.example.myapplication.presentation.assistant

import android.text.format.DateUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.os.ConfigurationCompat
import java.text.DateFormat
import java.util.Date
import java.util.Locale

/** لغة التطبيق الحالية (نفس الـ Configuration اللي الـ NavGraph بيوفّره). */
@Composable
private fun currentLocale(): Locale {
    val configuration = LocalConfiguration.current
    return ConfigurationCompat.getLocales(configuration)[0] ?: Locale.getDefault()
}

/** وقت الرسالة، مثلاً 10:30 AM / ١٠:٣٠ ص حسب لغة التطبيق. */
@Composable
internal fun rememberTimeText(millis: Long): String {
    val locale = currentLocale()
    return remember(millis, locale) {
        DateFormat.getTimeInstance(DateFormat.SHORT, locale).format(Date(millis))
    }
}

/** في الـ History: لو النهارده بنعرض الوقت، غير كدا بنعرض التاريخ. */
@Composable
internal fun rememberHistoryDateText(millis: Long): String {
    val locale = currentLocale()
    return remember(millis, locale) {
        if (DateUtils.isToday(millis)) {
            DateFormat.getTimeInstance(DateFormat.SHORT, locale).format(Date(millis))
        } else {
            DateFormat.getDateInstance(DateFormat.MEDIUM, locale).format(Date(millis))
        }
    }
}
