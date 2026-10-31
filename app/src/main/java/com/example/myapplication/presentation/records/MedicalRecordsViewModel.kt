package com.example.myapplication.presentation.records

import androidx.lifecycle.ViewModel
import com.example.myapplication.presentation.home.JourneyType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

// ---------------------------------------------------------------------------
// Data Models
// ---------------------------------------------------------------------------
enum class RecordType(val label: String, val labelAr: String) {
    LAB("Lab Results", "تحاليل"),
    IMAGING("Imaging", "أشعة"),
    PATHOLOGY("Pathology", "أنسجة"),
    VISIT("Visit Notes", "زيارة طبيب"),
    PROCEDURE("Procedure", "إجراء طبي"),
    PRESCRIPTION("Prescription", "روشتة")
}

enum class RecordStatus {
    NORMAL, ABNORMAL, PENDING, UNKNOWN
}

enum class RecordSource {
    PATIENT_ENTERED, CLINICIAN_PROVIDED, IMPORTED
}

data class MedicalRecord(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val type: RecordType,
    val status: RecordStatus,
    val source: RecordSource = RecordSource.PATIENT_ENTERED,
    val date: String,
    val doctor: String = "",
    val facility: String = "",
    val summary: String = "",
    val notes: String = "",
    val values: List<LabValue> = emptyList(),
    val isVerified: Boolean = false,
    val verificationStatus: String = "UNVERIFIED"
)

data class LabValue(
    val name: String,
    val result: String,
    val referenceRange: String,
    val isAbnormal: Boolean
)

data class RecordsUiState(
    val journeyType: JourneyType = JourneyType.EARLY_DETECTION,
    val records: List<MedicalRecord> = emptyList(),
    val totalCount: Int = 0,
    val pendingCount: Int = 0,
    val abnormalCount: Int = 0,
    val selectedTypeFilter: RecordType? = null,
    val showAddSheet: Boolean = false,
    val editingRecord: MedicalRecord? = null,
    val selectedRecord: MedicalRecord? = null,
    val showDetailSheet: Boolean = false
)

// ---------------------------------------------------------------------------
// ViewModel
// ---------------------------------------------------------------------------
class MedicalRecordsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RecordsUiState())
    val uiState: StateFlow<RecordsUiState> = _uiState.asStateFlow()

    init {
        loadPrototypeData()
    }

    fun setJourney(journey: JourneyType) {
        _uiState.value = _uiState.value.copy(journeyType = journey)
    }

    fun toggleAddSheet(show: Boolean) {
        _uiState.value = _uiState.value.copy(
            showAddSheet = show,
            editingRecord = if (!show) null else _uiState.value.editingRecord
        )
    }

    fun setEditingRecord(record: MedicalRecord?) {
        _uiState.value = _uiState.value.copy(
            editingRecord = record,
            showAddSheet = record != null
        )
    }

    fun setTypeFilter(type: RecordType?) {
        _uiState.value = _uiState.value.copy(selectedTypeFilter = type)
    }

    fun openRecordDetail(record: MedicalRecord) {
        _uiState.value = _uiState.value.copy(
            selectedRecord = record,
            showDetailSheet = true
        )
    }

    fun closeRecordDetail() {
        _uiState.value = _uiState.value.copy(
            selectedRecord = null,
            showDetailSheet = false
        )
    }

    fun addRecord(
        title: String,
        type: RecordType,
        status: RecordStatus,
        date: String,
        doctor: String,
        facility: String,
        summary: String,
        notes: String,
        values: List<LabValue>
    ) {
        val newRecord = MedicalRecord(
            title = title,
            type = type,
            status = status,
            date = date,
            doctor = doctor,
            facility = facility,
            summary = summary,
            notes = notes,
            values = values
        )

        val updated = _uiState.value.records.toMutableList()
        updated.add(0, newRecord)
        updateRecordsList(updated)
        _uiState.value = _uiState.value.copy(showAddSheet = false, editingRecord = null)
    }

    fun updateRecord(record: MedicalRecord) {
        val updated = _uiState.value.records.map {
            if (it.id == record.id) record else it
        }
        updateRecordsList(updated)
        _uiState.value = _uiState.value.copy(showAddSheet = false, editingRecord = null)
    }

    fun deleteRecord(id: String) {
        val updated = _uiState.value.records.filter { it.id != id }
        updateRecordsList(updated)
    }

    fun getFilteredRecords(): List<MedicalRecord> {
        val filter = _uiState.value.selectedTypeFilter
        return if (filter == null) _uiState.value.records
        else _uiState.value.records.filter { it.type == filter }
    }

    private fun updateRecordsList(records: List<MedicalRecord>) {
        _uiState.value = _uiState.value.copy(
            records = records,
            totalCount = records.size,
            pendingCount = records.count { it.status == RecordStatus.PENDING },
            abnormalCount = records.count { it.status == RecordStatus.ABNORMAL }
        )
    }

    private fun loadPrototypeData() {
        val records = listOf(
            MedicalRecord(
                title = "Complete Blood Count (CBC)",
                type = RecordType.LAB,
                status = RecordStatus.NORMAL,
                date = "Dec 5, 2024",
                doctor = "Dr. Sarah Ahmed",
                facility = "Cairo Medical Lab",
                summary = "All values within normal range",
                notes = "Fasting blood sample",
                values = listOf(
                    LabValue("WBC", "7.2 K/uL", "4.5–11.0", false),
                    LabValue("RBC", "4.8 M/uL", "4.5–5.5", false),
                    LabValue("Hemoglobin", "14.2 g/dL", "13.5–17.5", false),
                    LabValue("Platelets", "245 K/uL", "150–400", false)
                ),
                isVerified = true,
                verificationStatus = "CLINICIAN_CONFIRMED"
            ),
            MedicalRecord(
                title = "Chest X-Ray (PA & Lateral)",
                type = RecordType.IMAGING,
                status = RecordStatus.PENDING,
                date = "Dec 3, 2024",
                doctor = "Dr. Mohamed Ali",
                facility = "Dar Al Fouad Hospital",
                summary = "Awaiting radiologist report",
                notes = "Routine screening X-ray",
                isVerified = false,
                verificationStatus = "UNVERIFIED"
            ),
            MedicalRecord(
                title = "Pulmonary Function Test (PFT)",
                type = RecordType.LAB,
                status = RecordStatus.ABNORMAL,
                date = "Nov 28, 2024",
                doctor = "Dr. Sarah Ahmed",
                facility = "Cairo Pulmonology Center",
                summary = "Mild obstructive pattern detected",
                notes = "FEV1/FVC ratio slightly below normal",
                values = listOf(
                    LabValue("FEV1", "2.1 L", "2.8–3.5", true),
                    LabValue("FVC", "3.4 L", "3.5–4.8", true),
                    LabValue("FEV1/FVC", "62%", ">70%", true),
                    LabValue("DLCO", "78%", ">75%", false)
                ),
                isVerified = true,
                verificationStatus = "CLINICIAN_CONFIRMED"
            ),
            MedicalRecord(
                title = "Oncology Consultation",
                type = RecordType.VISIT,
                status = RecordStatus.UNKNOWN,
                date = "Nov 20, 2024",
                doctor = "Dr. Ahmed Hassan",
                facility = "Cairo Oncology Center",
                summary = "Initial consultation — discussed screening options",
                notes = "Recommended LDCT screening based on risk factors",
                isVerified = true,
                verificationStatus = "CLINICIAN_CONFIRMED"
            ),
            MedicalRecord(
                title = "CT Scan — Chest with Contrast",
                type = RecordType.IMAGING,
                status = RecordStatus.ABNORMAL,
                date = "Nov 15, 2024",
                doctor = "Dr. Mohamed Ali",
                facility = "Dar Al Fouad Hospital",
                summary = "Small nodule detected in right upper lobe (6mm)",
                notes = "Follow-up CT recommended in 3 months",
                isVerified = true,
                verificationStatus = "CLINICIAN_CONFIRMED"
            ),
            MedicalRecord(
                title = "Tumor Markers Panel",
                type = RecordType.LAB,
                status = RecordStatus.NORMAL,
                date = "Nov 10, 2024",
                doctor = "Dr. Sarah Ahmed",
                facility = "Cairo Medical Lab",
                summary = "All tumor markers within normal limits",
                values = listOf(
                    LabValue("CEA", "2.1 ng/mL", "<5.0", false),
                    LabValue("CYFRA 21-1", "1.8 ng/mL", "<3.3", false),
                    LabValue("NSE", "12.4 ng/mL", "<16.3", false)
                ),
                isVerified = true,
                verificationStatus = "CLINICIAN_CONFIRMED"
            )
        )
        updateRecordsList(records)
    }
}
