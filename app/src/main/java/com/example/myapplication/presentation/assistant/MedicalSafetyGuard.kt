package com.example.myapplication.presentation.assistant

/**
 * المكان الوحيد لفحص أعراض الطوارئ (اتشال UrgentInputDetector من الـ UI).
 *
 * النص بيتطبّع قبل المقارنة: lowercase، شيل التشكيل والتطويل، توحيد الهمزات (أ إ آ → ا)،
 * (ى → ي)، (ة → ه)، وتوحيد علامات الاقتباس. فـ "ألم" و"الم" و"كحّة" و"كحة" كلهم بيتطابقوا.
 *
 * ملاحظات:
 * - الفحص بالـ substring، فجملة زي "أنا مبكحش دم" ممكن تطلع إنذار (الأمان أهم من الدقة هنا).
 * - القائمة محتاجة مراجعة طبيب متخصص قبل النشر.
 */
object MedicalSafetyGuard {

    // لازم يتعرّفوا قبل normalizedPhrases لأن normalize() بيستخدمهم وقت الـ init
    private val marksToStrip = Regex("[\\u064B-\\u065F\\u0670\\u0640]")
    private val whitespace = Regex("\\s+")

    private val emergencyPhrases = listOf(
        // دم مع السعال
        "coughing up blood", "coughing blood", "cough up blood", "cough blood",
        "blood in sputum", "blood in phlegm", "hemoptysis",
        "سعال دم", "سعال بدم", "كحة دم", "كحة بدم", "بكح دم", "بسعل دم", "اكح دم",
        "دم مع السعال", "دم مع الكحة", "بصق دم", "ابصق دم", "دم في البلغم", "دم في البصاق",
        // ألم الصدر الشديد
        "severe chest pain", "crushing chest pain",
        "ألم شديد في الصدر", "ألم حاد في الصدر", "وجع شديد في الصدر",
        "وجع شديد في صدري", "وجع حاد في الصدر",
        // صعوبة التنفس الشديدة
        "can't breathe", "cannot breathe", "can not breathe", "cant breathe",
        "severe breathlessness", "struggling to breathe",
        "صعوبة شديدة في التنفس", "لا أستطيع التنفس",
        "مش قادر اتنفس", "مش قادرة اتنفس", "مش عارف اتنفس", "مش عارفة اتنفس",
        "مش قادر اخد نفسي", "مش قادرة اخد نفسي"
    )

    private val normalizedPhrases: List<String> =
        emergencyPhrases.map { normalize(it) }.distinct()

    fun containsEmergencySymptoms(input: String): Boolean {
        if (input.isBlank()) return false
        val normalized = normalize(input)
        return normalizedPhrases.any { normalized.contains(it) }
    }

    private fun normalize(text: String): String = text
        .lowercase()
        .replace('’', '\'')
        .replace('‘', '\'')
        .replace(marksToStrip, "")
        .replace('أ', 'ا')
        .replace('إ', 'ا')
        .replace('آ', 'ا')
        .replace('ٱ', 'ا')
        .replace('ى', 'ي')
        .replace('ة', 'ه')
        .replace(whitespace, " ")
        .trim()
}