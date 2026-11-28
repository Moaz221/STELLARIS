package com.example.myapplication.presentation.myhealth

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
        loadPrototypeData()
    }

    fun setJourney(journey: JourneyType) {
        _uiState.value = _uiState.value.copy(journeyType = journey)
    }

    private fun loadPrototypeData() {
        // No demo data: the screen starts empty and shows only what the user saves.
        val name = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.displayName.orEmpty()
        _uiState.value = MyHealthUiState(journeyType = JourneyType.EARLY_DETECTION, userName = name)
    }
}
