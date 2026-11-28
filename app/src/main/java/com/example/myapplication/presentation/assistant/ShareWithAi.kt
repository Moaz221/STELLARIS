package com.example.myapplication.presentation.assistant

import com.example.myapplication.data.local.AppStore
import com.example.myapplication.presentation.questions.QuestionItem
import com.example.myapplication.presentation.records.MedicalRecord
import com.example.myapplication.presentation.symptoms.TrackedSymptom

/**
 * Nothing is sent automatically. The user taps "Share with AI" on a screen; the text built here
 * is shown in the chat as the user's own message, so they see exactly what is sent.
 */
object ShareWithAi {
    @Volatile
    var pending: String? = null

    fun take(): String? = pending.also { pending = null }

    private const val HEAD = "I'm sharing my own saved data from the app. I know you are an AI assistant and not a doctor. Please help me understand it and what to ask my doctor.\n\n"

    suspend fun symptoms(): String? {
        val list = AppStore.load("symptoms", TrackedSymptom.serializer()).filter { !it.isResolved }
        if (list.isEmpty()) return null
        val sb = StringBuilder(HEAD).append("My active symptoms:\n")
        list.forEach {
            sb.append("- ${it.name}, severity ${it.severity}/5, ${it.frequency}")
            if (it.triggers.isNotBlank()) sb.append(", triggers: ${it.triggers}")
            if (it.notes.isNotBlank()) sb.append(", notes: ${it.notes}")
            sb.append(" (logged ${it.dateLogged})\n")
        }
        return sb.toString()
    }

    suspend fun records(): String? {
        val list = AppStore.load("records", MedicalRecord.serializer())
        if (list.isEmpty()) return null
        val sb = StringBuilder(HEAD).append("My medical records:\n")
        list.forEach {
            sb.append("- ${it.title} (${it.type.label}, ${it.date}, status ${it.status})")
            if (it.summary.isNotBlank()) sb.append(": ${it.summary}")
            if (it.values.isNotEmpty()) sb.append(" Values: " + it.values.joinToString { v -> "${v.name} ${v.result} (ref ${v.referenceRange})" })
            sb.append("\n")
        }
        return sb.toString()
    }

    suspend fun questions(): String? {
        val list = AppStore.load("questions", QuestionItem.serializer()).filter { it.status == "ACTIVE" }
        if (list.isEmpty()) return null
        return HEAD + "Questions I want to ask my doctor:\n" + list.joinToString("\n") { "- ${it.text}" }
    }

    suspend fun risk(): String? {
        val list = AppStore.load("risk", com.example.myapplication.presentation.risk.RiskResult.serializer())
        val r = list.firstOrNull() ?: return null
        return HEAD + "My lung screening risk check (" + r.date + "):\n" + r.summary
    }

    /** Plain-text summary for sharing with the user's medical team through the system share sheet. */
    suspend fun careSummary(): String? {
        val sb = StringBuilder("My health summary (from my own entries in STELLARIS; not a diagnosis)\n")
        var any = false
        AppStore.load("care_diagnosis", com.example.myapplication.core.model.DiagnosisInfo.serializer()).firstOrNull()?.let {
            sb.append("\nDiagnosis told by my doctor: ${it.title}"); if (it.details.isNotBlank()) sb.append(" - ${it.details}"); sb.append("\n"); any = true
        }
        val symptoms = AppStore.load("symptoms", TrackedSymptom.serializer()).filter { !it.isResolved }
        if (symptoms.isNotEmpty()) { any = true; sb.append("\nSymptoms:\n"); symptoms.forEach { sb.append("- ${it.name}, severity ${it.severity}/5, ${it.frequency}\n") } }
        val meds = AppStore.load("care_medications", com.example.myapplication.core.model.Medication.serializer())
        if (meds.isNotEmpty()) { any = true; sb.append("\nMedications:\n"); meds.forEach { sb.append("- ${it.name} ${it.instructions} ${it.reminderTimes.joinToString(", ")}\n") } }
        val fx = AppStore.load("care_side_effects", com.example.myapplication.core.model.SideEffectEntry.serializer())
        if (fx.isNotEmpty()) { any = true; sb.append("\nSide effects:\n"); fx.forEach { sb.append("- ${it.name} (${it.severity})\n") } }
        val appts = AppStore.load("care_appointments", com.example.myapplication.core.model.Appointment.serializer())
        if (appts.isNotEmpty()) { any = true; sb.append("\nAppointments:\n"); appts.forEach { sb.append("- ${it.title} ${it.dateTime}\n") } }
        return if (any) sb.toString() else null
    }
}
