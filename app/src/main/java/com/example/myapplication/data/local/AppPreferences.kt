package com.example.myapplication.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "lungcare_prefs")

class AppPreferences(private val context: Context) {

    private val onboardingDoneKey = booleanPreferencesKey("onboarding_done")
    private val darkThemeKey = booleanPreferencesKey("dark_theme")
    private val languageKey = stringPreferencesKey("language_code")
    private val journeyKey = stringPreferencesKey("journey_type")

    val isOnboardingDone: Flow<Boolean> =
        context.dataStore.data.map { it[onboardingDoneKey] ?: false }

    val isDarkTheme: Flow<Boolean> =
        context.dataStore.data.map { it[darkThemeKey] ?: true }

    val languageCode: Flow<String> =
        context.dataStore.data.map { it[languageKey] ?: "ar" }

    val journeyType: Flow<String> =
        context.dataStore.data.map { it[journeyKey] ?: "EARLY_DETECTION" }

    suspend fun setOnboardingDone() {
        context.dataStore.edit { it[onboardingDoneKey] = true }
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.dataStore.edit { it[darkThemeKey] = isDark }
    }

    suspend fun setLanguageCode(code: String) {
        context.dataStore.edit { it[languageKey] = code }
    }

    suspend fun setJourneyType(type: String) {
        context.dataStore.edit { it[journeyKey] = type }
    }
}
