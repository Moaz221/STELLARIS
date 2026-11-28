package com.example.myapplication.presentation.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.AppStore
import kotlinx.coroutines.launch
import com.example.myapplication.presentation.home.JourneyType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

// ---------------------------------------------------------------------------
// Data Models
// ---------------------------------------------------------------------------
@kotlinx.serialization.Serializable
enum class RecordType(val label: String, val labelAr: String) {
    LAB("Lab Results", "تحاليل"),
    IMAGING("Imaging", "أشعة"),
    PATHOLOGY("Pathology", "أنسجة"),
    VISIT("Visit Notes", "زيارة طبيب"),
    PROCEDURE("Procedure", "إجراء طبي"),
    PRESCRIPTION("Prescription", "روشتة")
}

@kotlinx.serialization.Serializable
enum class RecordStatus {
    NORMAL, ABNORMAL, PENDING, UNKNOWN
}

@kotlinx.serialization.Serializable
enum class RecordSource {
    PATIENT_ENTERED, CLINICIAN_PROVIDED, IMPORTED
}

@kotlinx.serialization.Serializable
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

@kotlinx.serialization.Serializable
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

    private fun updateRecordsList(records: List<MedicalRecord>, persist: Boolean = true) {
        _uiState.value = _uiState.value.copy(
            records = records,
            totalCount = records.size,
            pendingCount = records.count { it.status == RecordStatus.PENDING },
            abnormalCount = records.count { it.status == RecordStatus.ABNORMAL }
        )
        if (persist) {
            viewModelScope.launch { AppStore.save("records", MedicalRecord.serializer(), records) }
        }
    }

    private fun loadPrototypeData() {
        // No demo data. Load only what the user saved.
        viewModelScope.launch {
            updateRecordsList(AppStore.load("records", MedicalRecord.serializer()), persist = false)
        }
    }
}

@androidx.compose.runtime.Composable
fun RecordType.display(): String = com.example.myapplication.core.ui.loc(label, labelAr)
