package com.example.myapplication.presentation.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.MedicalRecord
import com.example.myapplication.data.model.SymptomEntry
import com.example.myapplication.data.repository.HealthRepository
import com.example.myapplication.data.repository.LocalHealthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HealthViewModel(
    private val healthRepository: HealthRepository = LocalHealthRepository()
) : ViewModel() {

    val symptoms: StateFlow<List<SymptomEntry>> = healthRepository.symptoms.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val medicalRecords: StateFlow<List<MedicalRecord>> = healthRepository.medicalRecords.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addSymptom(name: String, severity: String) {
        viewModelScope.launch {
            healthRepository.addSymptom(
                SymptomEntry(
                    id = System.currentTimeMillis().toString(),
                    name = name,
                    severity = severity,
                    date = "Just now"
                )
            )
        }
    }

    fun addMedicalRecord(title: String, type: String) {
        viewModelScope.launch {
            healthRepository.addMedicalRecord(
                MedicalRecord(
                    id = System.currentTimeMillis().toString(),
                    title = title,
                    type = type,
                    date = "Just now",
                    source = "Patient Entered"
                )
            )
        }
    }
}
