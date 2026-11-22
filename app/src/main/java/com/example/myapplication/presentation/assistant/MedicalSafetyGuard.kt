package com.example.myapplication.presentation.assistant

object MedicalSafetyGuard {
    private val emergencyKeywords = listOf(
        "دم مع السعال", "بصق دم", "دم في البلغم", "كحة دم", "coughing blood", "blood in sputum", "hemoptysis",
        "ألم حاد في الصدر", "وجع شديد في صدري", "severe chest pain", "crushing chest pain",
        "صعوبة شديدة في التنفس", "مش قادر اتنفس خالص", "severe breathlessness", "cannot breathe"
    )

    fun containsEmergencySymptoms(input: String): Boolean {
        val normalized = input.lowercase()
        return emergencyKeywords.any { normalized.contains(it) }
    }
}
