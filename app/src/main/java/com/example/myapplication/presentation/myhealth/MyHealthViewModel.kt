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
    val userName: String = "Moaz",

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
        _uiState.value = MyHealthUiState(
            journeyType = JourneyType.EARLY_DETECTION,
            userName = "Moaz",

            // Early Detection
            riskLevel = RiskLevel.MODERATE,
            riskFactors = listOf(
                RiskFactor("smoking", "Smoking history", true),
                RiskFactor("family", "Family history of lung disease", true),
                RiskFactor("exposure", "Occupational exposure", false),
                RiskFactor("age", "Age over 50", false),
                RiskFactor("copd", "COPD or chronic lung disease", false)
            ),
            riskFactorCount = 2,
            screeningDue = true,
            screeningMessage = "Based on your risk factors, consider discussing LDCT screening with your doctor.",

            // Shared
            recentSymptoms = listOf(
                SymptomEntry("s1", "Persistent cough", 3, "Dec 10, 2024", false),
                SymptomEntry("s2", "Mild shortness of breath", 2, "Dec 8, 2024", false),
                SymptomEntry("s3", "Occasional chest tightness", 1, "Dec 1, 2024", true)
            ),
            activeSymptomCount = 2,

            // Under Evaluation
            recentRecords = listOf(
                MedicalRecord("r1", "Chest X-Ray", "Imaging", "Dec 5, 2024", "Pending"),
                MedicalRecord("r2", "Complete Blood Count", "Lab", "Nov 28, 2024", "Normal"),
                MedicalRecord("r3", "Pulmonary Function Test", "Lab", "Nov 20, 2024", "Abnormal")
            ),
            pendingResultsCount = 1,
            doctorQuestions = listOf(
                DoctorQuestion("q1", "Should I get an LDCT scan?", false, "Dec 9, 2024"),
                DoctorQuestion("q2", "What do my PFT results mean?", false, "Dec 7, 2024"),
                DoctorQuestion("q3", "Is my cough related to allergies?", true, "Nov 25, 2024")
            ),
            unansweredCount = 2,

            // Diagnosed
            treatmentDay = 45,
            treatmentName = "Chemotherapy Cycle 2",
            medications = listOf(
                Medication("m1", "Cisplatin", "75 mg/m²", "Every 3 weeks", "Dec 20, 2024"),
                Medication("m2", "Ondansetron", "8 mg", "Twice daily", "Today, 8:00 PM"),
                Medication("m3", "Dexamethasone", "4 mg", "Once daily", "Tomorrow, 8:00 AM")
            ),
            sideEffects = listOf(
                SideEffect("se1", "Nausea", 3, "Dec 11, 2024", "Cisplatin"),
                SideEffect("se2", "Fatigue", 4, "Dec 10, 2024", null),
                SideEffect("se3", "Loss of appetite", 2, "Dec 9, 2024", "Cisplatin")
            ),
            activeSideEffectCount = 3,
            appointments = listOf(
                Appointment("a1", "Dr. Sarah Ahmed", "Oncology", "Dec 18, 2024", "10:00 AM", "Cairo Oncology Center"),
                Appointment("a2", "Dr. Mohamed Ali", "Pulmonology", "Dec 22, 2024", "2:30 PM", "Dar Al Fouad Hospital")
            ),
            timeline = listOf(
                TimelineEvent("t1", "Diagnosis Confirmed", "Stage II NSCLC confirmed via biopsy", "Nov 1, 2024", TimelineEventType.DIAGNOSIS),
                TimelineEvent("t2", "Treatment Started", "Chemotherapy Cycle 1 initiated", "Nov 10, 2024", TimelineEventType.TREATMENT_START),
                TimelineEvent("t3", "CT Scan", "Follow-up imaging scheduled", "Dec 15, 2024", TimelineEventType.SCAN),
                TimelineEvent("t4", "Cycle 2 Started", "Chemotherapy Cycle 2 initiated", "Dec 1, 2024", TimelineEventType.TREATMENT_START)
            ),

            isLoading = false
        )
    }
}
