package com.example.myapplication.presentation.care

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.R
import com.example.myapplication.core.model.CareJourneyState
import com.example.myapplication.core.model.JourneyType
import com.example.myapplication.data.care.CareRepository
import com.example.myapplication.data.care.LocalCareRepository
import com.example.myapplication.core.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CareJourneyUiState {
    data object Loading : CareJourneyUiState
    data class Error(val messageRes: Int) : CareJourneyUiState
    data class Content(val data: CareJourneyState) : CareJourneyUiState
}

class CareJourneyViewModel @JvmOverloads constructor(
    private val repository: CareRepository = LocalCareRepository(),
    val journey: JourneyType = JourneyType.DIAGNOSED
) : ViewModel() {

    private val _uiState = MutableStateFlow<CareJourneyUiState>(CareJourneyUiState.Loading)
    val uiState: StateFlow<CareJourneyUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        _uiState.value = CareJourneyUiState.Loading
        viewModelScope.launch {
            try {
                _uiState.value = CareJourneyUiState.Content(repository.getCareJourney())
            } catch (e: Exception) {
                _uiState.value = CareJourneyUiState.Error(R.string.error_load_care)
            }
        }
    }

    fun completeMilestone(id: String) {
        viewModelScope.launch {
            val current = _uiState.value
            if (current is CareJourneyUiState.Content) {
                _uiState.value = CareJourneyUiState.Content(repository.completeMilestone(id))
            }
        }
    }

    private fun update(block: suspend () -> CareJourneyState) {
        viewModelScope.launch {
            try { _uiState.value = CareJourneyUiState.Content(block()) } catch (e: Exception) { _uiState.value = CareJourneyUiState.Error(R.string.error_load_care) }
        }
    }

    private fun now(): String = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())

    fun addAppointment(title: String, facility: String, dateTime: String, notes: String, replaceId: String? = null) = update {
        if (replaceId != null) repository.delete("appointment", replaceId)
        repository.addAppointment(Appointment(java.util.UUID.randomUUID().toString(), title, facility, null, dateTime, AppointmentType.OTHER, AppointmentStatus.UPCOMING, notes.ifBlank { null }, SourceType.PATIENT_ENTERED, VerificationStatus.USER_CONFIRMED, now(), now()))
    }

    fun addMedication(name: String, instructions: String, times: List<String>, replaceId: String? = null) = update {
        if (replaceId != null) repository.delete("medication", replaceId)
        repository.addMedication(Medication(java.util.UUID.randomUUID().toString(), name, instructions, times, true, SourceType.PATIENT_ENTERED, VerificationStatus.USER_CONFIRMED, now(), now()))
    }

    fun addSideEffect(name: String, severity: SideEffectSeverity, notes: String, replaceId: String? = null, date: String? = null) = update {
        if (replaceId != null) repository.delete("side_effect", replaceId)
        repository.addSideEffect(SideEffectEntry(java.util.UUID.randomUUID().toString(), name, severity, date ?: now(), notes.ifBlank { null }, SourceType.PATIENT_ENTERED, VerificationStatus.USER_CONFIRMED, now(), now()))
    }

    fun setDiagnosis(title: String, details: String, specialist: String) = update {
        repository.setDiagnosis(DiagnosisInfo(java.util.UUID.randomUUID().toString(), title, details, now(), specialist, SourceType.PATIENT_ENTERED, VerificationStatus.USER_CONFIRMED, now(), now()))
    }

    fun delete(kind: String, id: String) = update { repository.delete(kind, id) }
}
