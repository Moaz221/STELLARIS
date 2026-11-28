package com.example.myapplication.presentation.myhealth

import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModel
import com.example.myapplication.presentation.home.JourneyType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// ---------------------------------------------------------------------------
// Data Models
// ---------------------------------------------------------------------------
enum class RiskLevel { LOW, MODERATE, ELEVATED }

data class RiskFactor(
    val id: String,
    val label: String,
    val isActive: Boolean
)

data class SymptomEntry(
    val id: String,
    val name: String,
    val severity: Int,       // 1-5
    val date: String,
    val isResolved: Boolean
)

data class MedicalRecord(
    val id: String,
    val title: String,
    val type: String,        // "Lab", "Imaging", "Pathology", "Visit"
    val date: String,
    val status: String       // "Normal", "Abnormal", "Pending"
)

data class Medication(
    val id: String,
    val name: String,
    val dosage: String,
    val frequency: String,
    val nextDose: String
)

data class SideEffect(
    val id: String,
    val name: String,
    val severity: Int,       // 1-5
    val date: String,
    val relatedMedication: String?
)

data class Appointment(
    val id: String,
    val doctor: String,
    val specialty: String,
    val date: String,
    val time: String,
    val location: String
)

data class DoctorQuestion(
    val id: String,
    val question: String,
    val isAnswered: Boolean,
    val date: String
)

data class TimelineEvent(
    val id: String,
    val title: String,
    val description: String,
    val date: String,
    val type: TimelineEventType
)

enum class TimelineEventType {
    DIAGNOSIS, TREATMENT_START, MEDICATION, SURGERY, SCAN, SYMPTOM, MILESTONE
}

// ---------------------------------------------------------------------------
// UI State
// ---------------------------------------------------------------------------
data class MyHealthUiState(
    val journeyType: JourneyType = JourneyType.EARLY_DETECTION,
    val userName: String = "",

    // Early Detection
    val riskLevel: RiskLevel = RiskLevel.LOW,
    val riskFactors: List<RiskFactor> = emptyList(),
    val riskFactorCount: Int = 0,
    val screeningDue: Boolean = false,
    val screeningMessage: String = "",

    // Shared
    val recentSymptoms: List<SymptomEntry> = emptyList(),
    val activeSymptomCount: Int = 0,

    // Under Evaluation
    val recentRecords: List<MedicalRecord> = emptyList(),
    val pendingResultsCount: Int = 0,
    val doctorQuestions: List<DoctorQuestion> = emptyList(),
    val unansweredCount: Int = 0,

    // Diagnosed
    val treatmentDay: Int = 0,
    val treatmentName: String = "",
    val medications: List<Medication> = emptyList(),
    val sideEffects: List<SideEffect> = emptyList(),
    val activeSideEffectCount: Int = 0,
    val appointments: List<Appointment> = emptyList(),
    val timeline: List<TimelineEvent> = emptyList(),

    val isLoading: Boolean = false
)

// ---------------------------------------------------------------------------
// ViewModel
// ---------------------------------------------------------------------------
class MyHealthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MyHealthUiState())
    val uiState: StateFlow<MyHealthUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun setJourney(journey: JourneyType) {
        _uiState.value = _uiState.value.copy(journeyType = journey)
    }

    /** Reads everything the user saved in the other screens. Call whenever this screen is shown. */
    fun refresh(savedJourney: String? = null) {
        viewModelScope.launch {
            try {
                val name = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.displayName.orEmpty()
                val symptoms = com.example.myapplication.data.local.AppStore.load("symptoms", com.example.myapplication.presentation.symptoms.TrackedSymptom.serializer())
                val records = com.example.myapplication.data.local.AppStore.load("records", com.example.myapplication.presentation.records.MedicalRecord.serializer())
                val questions = com.example.myapplication.data.local.AppStore.load("questions", com.example.myapplication.presentation.questions.QuestionItem.serializer())
                val risks = com.example.myapplication.data.local.AppStore.load("risk", com.example.myapplication.presentation.risk.RiskResult.serializer())
                val care = com.example.myapplication.data.care.LocalCareRepository().getCareJourney()

                val activeSymptoms = symptoms.filter { !it.isResolved }
                val risk = risks.lastOrNull()
                val lv = risk?.level?.uppercase().orEmpty()
                val level = when { lv.contains("HIGH") || lv.contains("ELEV") -> RiskLevel.ELEVATED; lv.contains("MOD") -> RiskLevel.MODERATE; else -> RiskLevel.LOW }
                // Risk "factors" = the lines of the saved check summary (what the user answered), newest check only.
                val factorLine = risk?.summary.orEmpty().lines().map { it.trim() }.firstOrNull { it.startsWith("Risk factors:", true) || it.startsWith("عوامل الخطر:") }
                val factors = factorLine?.substringAfter(":")?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }.orEmpty()
                    .mapIndexed { i, l -> RiskFactor("rf$i", l, true) }
                val activeQuestions = questions.filter { it.status == "ACTIVE" }
                val journey = runCatching { JourneyType.valueOf(savedJourney ?: "") }.getOrNull() ?: _uiState.value.journeyType
                _uiState.value = MyHealthUiState(
                    journeyType = journey,
                    userName = name,
                    riskLevel = level,
                    riskFactors = factors,
                    riskFactorCount = factors.size,
                    screeningDue = risk?.eligibleForScreening == true,
                    screeningMessage = risk?.summary.orEmpty().lines().lastOrNull { it.isNotBlank() }.orEmpty(),
                    recentSymptoms = symptoms.sortedByDescending { it.timestamp }.take(3).map { SymptomEntry(it.id, it.name, it.severity, it.dateLogged, it.isResolved) },
                    activeSymptomCount = activeSymptoms.size,
                    recentRecords = records.take(3).map { MedicalRecord(it.id, it.title, it.type.name, it.date, it.status.name) },
                    pendingResultsCount = records.count { it.status.name == "PENDING" },
                    doctorQuestions = activeQuestions.take(3).map { DoctorQuestion(it.id, it.text, false, "") },
                    unansweredCount = activeQuestions.size,
                    medications = care.medications.map { Medication(it.id, it.name, it.instructions, "", it.reminderTimes.joinToString(", ")) },
                    sideEffects = care.sideEffects.take(3).map { SideEffect(it.id, it.name, when (it.severity.name) { "MILD" -> 1; "MODERATE" -> 3; else -> 5 }, it.date, null) },
                    activeSideEffectCount = care.sideEffects.size,
                    appointments = care.appointments.filter { it.status.name == "UPCOMING" }.map { Appointment(it.id, it.doctor ?: it.title, it.title, it.dateTime, "", it.facility) },
                    treatmentName = care.diagnosis?.title.orEmpty(),
                    isLoading = false
                )
            } catch (e: Exception) {
                // keep the previous state
            }
        }
    }
}
