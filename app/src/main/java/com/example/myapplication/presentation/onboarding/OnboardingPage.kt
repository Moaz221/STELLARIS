package com.example.myapplication.presentation.onboarding

import androidx.annotation.RawRes
import androidx.compose.ui.graphics.Color
import com.example.myapplication.R
import com.example.myapplication.presentation.theme.*

data class OnboardingPage(
    @param:RawRes val lottieRes: Int,
    val kicker: String,
    val title: String,
    val description: String,
    val accentColor: Color
)

val onboardingPages = listOf(
    OnboardingPage(
        lottieRes = R.raw.robot_wave,
        kicker = "YOUR AI COMPANION",
        title = "Meet STELLARIS",
        description = "An intelligent assistant for chest X-ray analysis — fast, precise, always with you.",
        accentColor = StellarTeal
    ),
    OnboardingPage(
        lottieRes = R.raw.ai_scan,
        kicker = "X-RAY INTELLIGENCE",
        title = "See What AI Sees",
        description = "Upload a scan and get instant analysis with heatmaps highlighting the focus areas.",
        accentColor = StellarCyan
    ),
    OnboardingPage(
        lottieRes = R.raw.robot_chat,
        kicker = "MEDICAL CHATBOT",
        title = "Just Ask",
        description = "Chat with STELLARIS to understand your results in simple, clear words.",
        accentColor = Nebula
    )
)
