package com.example.myapplication.presentation.symptoms

import androidx.lifecycle.ViewModel
import com.example.myapplication.presentation.home.JourneyType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ---------------------------------------------------------------------------
// Data Models for Symptom Tracker
// ---------------------------------------------------------------------------

enum class SymptomCategory {
    RESPIRATORY,     // Cough, Breathlessness, Wheezing
    PAIN_CHEST,      // Chest pain, Tightness
    CONSTITUTIONAL,  // Fatigue, Fever, Weight Loss
    TREATMENT_RELATED, // Nausea, Loss of appetite
    CRITICAL_EMERGENCY // Hemoptysis (Coughing blood), Severe Dyspnea
}

enum class SymptomFilter {
    ALL,
    ACTIVE,
    URGENT,
    RESOLVED
}

data class TrackedSymptom(
    val id: String,
    val name: String,
    val category: SymptomCategory,
    val severity: Int,            // 1 (Mild) to 5 (Critical/Emergency)
    val dateLogged: String,       // Formatted string
    val timestamp: Long = System.currentTimeMillis(),
    val isUrgent: Boolean = false,
    val isResolved: Boolean = false,
    val notes: String = "",
    val frequency: String = "Occasional", // "Occasional", "Frequent", "Constant"
    val triggers: String = ""             // Optional trigger description
)

data class SymptomPreset(
    val nameEn: String,
    val nameAr: String,
    val category: SymptomCategory,
    val isUrgentByDefault: Boolean,
    val defaultSeverity: Int,
    val iconName: String
)

data class SymptomTrackerUiState(
    val journeyType: JourneyType = JourneyType.EARLY_DETECTION,
    val symptoms: List<TrackedSymptom> = emptyList(),
    val selectedFilter: SymptomFilter = SymptomFilter.ALL,
    val hasUrgentAlert: Boolean = false,
    val urgentAlertMessage: String = "",
    val showAddDialog: Boolean = false,
    val symptomToEdit: TrackedSymptom? = null,
    val showExportDialog: Boolean = false,
    val doctorExportText: String = ""
)

// ---------------------------------------------------------------------------
// SymptomTrackerViewModel
// ---------------------------------------------------------------------------
class SymptomTrackerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SymptomTrackerUiState())
    val uiState: StateFlow<SymptomTrackerUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    val presets = listOf(
        SymptomPreset("Persistent Cough", "سعال مستمر", SymptomCategory.RESPIRATORY, false, 2, "cough"),
        SymptomPreset("Coughing up Blood (Hemoptysis)", "سعال مصحوب بدم", SymptomCategory.CRITICAL_EMERGENCY, true, 5, "blood"),
        SymptomPreset("Shortness of Breath", "ضيق في التنفس", SymptomCategory.RESPIRATORY, true, 3, "breath"),
        SymptomPreset("Chest Pain or Tightness", "ألم أو ضغط بالصدر", SymptomCategory.PAIN_CHEST, true, 4, "chest"),
        SymptomPreset("High Fever (> 38.5°C)", "ارتفاع شديد في الحرارة", SymptomCategory.CONSTITUTIONAL, true, 4, "fever"),
        SymptomPreset("Fatigue & Weakness", "إرهاق وضعف عام", SymptomCategory.CONSTITUTIONAL, false, 2, "fatigue"),
        SymptomPreset("Unexplained Weight Loss", "فقدان وزن غير مبرر", SymptomCategory.CONSTITUTIONAL, false, 2, "weight"),
        SymptomPreset("Wheezing or Hoarseness", "صفير بالصدر أو بحة بالصوت", SymptomCategory.RESPIRATORY, false, 2, "voice"),
        SymptomPreset("Nausea / Treatment Side Effect", "غثيان أو آثار جانبية للعلاج", SymptomCategory.TREATMENT_RELATED, false, 3, "nausea")
    )

    fun setJourney(journey: JourneyType) {
        _uiState.value = _uiState.value.copy(journeyType = journey)
        reevaluateUrgency()
    }

    fun setFilter(filter: SymptomFilter) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
    }

    fun openAddDialog(symptomToEdit: TrackedSymptom? = null) {
        _uiState.value = _uiState.value.copy(
            showAddDialog = true,
            symptomToEdit = symptomToEdit
        )
    }

    fun closeAddDialog() {
        _uiState.value = _uiState.value.copy(
            showAddDialog = false,
            symptomToEdit = null
        )
    }

    fun saveSymptom(
        name: String,
        category: SymptomCategory,
        severity: Int,
        isUrgent: Boolean,
        notes: String,
        frequency: String,
        triggers: String
    ) {
        val currentDateStr = SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.getDefault()).format(Date())
        val existing = _uiState.value.symptomToEdit

        val updatedList = if (existing != null) {
            _uiState.value.symptoms.map { item ->
                if (item.id == existing.id) {
                    item.copy(
                        name = name,
                        category = category,
                        severity = severity,
                        isUrgent = isUrgent || severity >= 4 || category == SymptomCategory.CRITICAL_EMERGENCY,
                        notes = notes,
                        frequency = frequency,
                        triggers = triggers
                    )
                } else item
            }
        } else {
            val newEntry = TrackedSymptom(
                id = "symptom_${System.currentTimeMillis()}",
                name = name,
                category = category,
                severity = severity,
                dateLogged = currentDateStr,
                isUrgent = isUrgent || severity >= 4 || category == SymptomCategory.CRITICAL_EMERGENCY,
                isResolved = false,
                notes = notes,
                frequency = frequency,
                triggers = triggers
            )
            listOf(newEntry) + _uiState.value.symptoms
        }

        _uiState.value = _uiState.value.copy(
            symptoms = updatedList,
            showAddDialog = false,
            symptomToEdit = null
        )

        reevaluateUrgency()
    }

    fun toggleResolved(id: String) {
        _uiState.value = _uiState.value.copy(
            symptoms = _uiState.value.symptoms.map {
                if (it.id == id) it.copy(isResolved = !it.isResolved) else it
            }
        )
        reevaluateUrgency()
    }

    fun deleteSymptom(id: String) {
        _uiState.value = _uiState.value.copy(
            symptoms = _uiState.value.symptoms.filterNot { it.id == id }
        )
        reevaluateUrgency()
    }

    fun generateDoctorSummary() {
        val activeSymptoms = _uiState.value.symptoms.filter { !it.isResolved }
        val urgentSymptoms = activeSymptoms.filter { it.isUrgent || it.severity >= 4 }

        val sb = StringBuilder()
        sb.append("📋 LUNG CARE PATIENT SYMPTOM SUMMARY\n")
        sb.append("Generated on: ").append(SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())).append("\n")
        sb.append("Journey Stage: ").append(_uiState.value.journeyType.name).append("\n\n")

        if (urgentSymptoms.isNotEmpty()) {
            sb.append("🚨 URGENT / HIGH SEVERITY SYMPTOMS:\n")
            urgentSymptoms.forEach { s ->
                sb.append("• ").append(s.name)
                    .append(" (Severity: ").append(s.severity).append("/5)")
                    .append(" - Logged: ").append(s.dateLogged).append("\n")
                if (s.notes.isNotBlank()) sb.append("   Notes: ").append(s.notes).append("\n")
            }
            sb.append("\n")
        }

        sb.append("🩺 ALL ACTIVE SYMPTOMS (Total: ").append(activeSymptoms.size).append("):\n")
        activeSymptoms.forEach { s ->
            sb.append("• ").append(s.name)
                .append(" [").append(s.frequency).append("]")
                .append(" - Severity ").append(s.severity).append("/5\n")
            if (s.triggers.isNotBlank()) sb.append("   Triggers: ").append(s.triggers).append("\n")
            if (s.notes.isNotBlank()) sb.append("   Notes: ").append(s.notes).append("\n")
        }

        if (activeSymptoms.isEmpty()) {
            sb.append("No active symptoms reported at this time.\n")
        }

        _uiState.value = _uiState.value.copy(
            showExportDialog = true,
            doctorExportText = sb.toString()
        )
    }

    fun closeExportDialog() {
        _uiState.value = _uiState.value.copy(showExportDialog = false)
    }

    private fun reevaluateUrgency() {
        val active = _uiState.value.symptoms.filter { !it.isResolved }
        val critical = active.find { it.isUrgent || it.severity >= 4 || it.category == SymptomCategory.CRITICAL_EMERGENCY }

        if (critical != null) {
            val msg = if (critical.category == SymptomCategory.CRITICAL_EMERGENCY || critical.severity == 5) {
                "🚨 CRITICAL RED FLAG: \"${critical.name}\" requires immediate emergency medical evaluation. Please contact your doctor or local emergency line immediately."
            } else {
                "⚠️ HIGH SEVERITY ALERT: You logged \"${critical.name}\" with severe intensity. Please inform your healthcare provider."
            }
            _uiState.value = _uiState.value.copy(
                hasUrgentAlert = true,
                urgentAlertMessage = msg
            )
        } else {
            _uiState.value = _uiState.value.copy(
                hasUrgentAlert = false,
                urgentAlertMessage = ""
            )
        }
    }

    private fun loadInitialData() {
        val initialSymptoms = listOf(
            TrackedSymptom(
                id = "s1",
                name = "Coughing up Blood (Hemoptysis)",
                category = SymptomCategory.CRITICAL_EMERGENCY,
                severity = 5,
                dateLogged = "Today, 09:15 AM",
                isUrgent = true,
                isResolved = false,
                notes = "Noticed streaks of blood in sputum during morning cough.",
                frequency = "Occasional",
                triggers = "Morning coughing fit"
            ),
            TrackedSymptom(
                id = "s2",
                name = "Persistent Cough",
                category = SymptomCategory.RESPIRATORY,
                severity = 3,
                dateLogged = "Yesterday, 04:30 PM",
                isUrgent = false,
                isResolved = false,
                notes = "Dry cough, feels worse in cold air.",
                frequency = "Frequent",
                triggers = "Cold air, dust"
            ),
            TrackedSymptom(
                id = "s3",
                name = "Shortness of Breath",
                category = SymptomCategory.RESPIRATORY,
                severity = 3,
                dateLogged = "Dec 10, 2024",
                isUrgent = false,
                isResolved = false,
                notes = "Shortness of breath after climbing two flights of stairs.",
                frequency = "Occasional",
                triggers = "Physical exertion"
            ),
            TrackedSymptom(
                id = "s4",
                name = "Mild Chest Tightness",
                category = SymptomCategory.PAIN_CHEST,
                severity = 1,
                dateLogged = "Dec 05, 2024",
                isUrgent = false,
                isResolved = true,
                notes = "Resolved after rest.",
                frequency = "Occasional"
            )
        )

        _uiState.value = SymptomTrackerUiState(
            journeyType = JourneyType.EARLY_DETECTION,
            symptoms = initialSymptoms
        )
        reevaluateUrgency()
    }
}
