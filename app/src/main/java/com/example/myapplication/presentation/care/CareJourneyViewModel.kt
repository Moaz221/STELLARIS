package com.example.myapplication.presentation.care

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.R
import com.example.myapplication.core.model.CareJourneyState
import com.example.myapplication.core.model.JourneyType
import com.example.myapplication.data.care.CareRepository
import com.example.myapplication.data.care.FakeCareRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CareJourneyUiState {
    data object Loading : CareJourneyUiState
    data class Error(val messageRes: Int) : CareJourneyUiState
    data class Content(val data: CareJourneyState) : CareJourneyUiState
}

class CareJourneyViewModel(
    private val repository: CareRepository = FakeCareRepository(),
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
}
