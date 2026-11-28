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

private fun ar() = java.util.Locale.getDefault().language == "ar"
private fun t(en: String, a: String) = if (ar()) a else en

val onboardingPages: List<OnboardingPage> get() = listOf(
    OnboardingPage(
        lottieRes = R.raw.robot_wave,
        kicker = t("YOUR AI COMPANION", "رفيقك الذكي"),
        title = t("Meet STELLARIS", "تعرّف على STELLARIS"),
        description = t("An intelligent assistant for chest X-ray analysis — fast, precise, always with you.", "مساعد ذكي لتحليل أشعة الصدر، سريع ودقيق ومعاك دايمًا."),
        accentColor = StellarTeal
    ),
    OnboardingPage(
        lottieRes = R.raw.ai_scan,
        kicker = t("X-RAY INTELLIGENCE", "ذكاء الأشعة"),
        title = t("See What AI Sees", "شوف اللي بيشوفه الذكاء الاصطناعي"),
        description = t("Upload a scan and get instant analysis with heatmaps highlighting the focus areas.", "ارفع الأشعة واحصل على تحليل فوري بخرائط حرارية توضح المناطق المهمة."),
        accentColor = StellarCyan
    ),
    OnboardingPage(
        lottieRes = R.raw.robot_chat,
        kicker = t("MEDICAL CHATBOT", "المساعد الطبي"),
        title = t("Just Ask", "اسأل بس"),
        description = t("Chat with STELLARIS to understand your results in simple, clear words.", "اتكلم مع STELLARIS وافهم نتايجك بكلمات بسيطة وواضحة."),
        accentColor = Nebula
    )
)
