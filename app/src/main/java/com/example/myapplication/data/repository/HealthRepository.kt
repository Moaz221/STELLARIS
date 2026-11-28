package com.example.myapplication.data.repository

import com.example.myapplication.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface HealthRepository {
    val symptoms: StateFlow<List<SymptomEntry>>
    val medicalRecords: StateFlow<List<MedicalRecord>>
    val doctorQuestions: StateFlow<List<DoctorQuestion>>
    val userProfile: StateFlow<UserProfile>

    suspend fun addSymptom(symptom: SymptomEntry)
    suspend fun addMedicalRecord(record: MedicalRecord)
    suspend fun addDoctorQuestion(question: DoctorQuestion)
    suspend fun toggleQuestionAnswered(questionId: String)
    suspend fun updateJourneyType(type: String)
}

class LocalHealthRepository : HealthRepository {

    private val _symptoms = MutableStateFlow(emptyList<SymptomEntry>())
    override val symptoms: StateFlow<List<SymptomEntry>> = _symptoms.asStateFlow()

    private val _medicalRecords = MutableStateFlow(emptyList<MedicalRecord>())
    override val medicalRecords: StateFlow<List<MedicalRecord>> = _medicalRecords.asStateFlow()

    private val _doctorQuestions = MutableStateFlow(emptyList<DoctorQuestion>())
    override val doctorQuestions: StateFlow<List<DoctorQuestion>> = _doctorQuestions.asStateFlow()

    private val _userProfile = MutableStateFlow(com.example.myapplication.data.model.currentUserProfile())
    override val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    override suspend fun addSymptom(symptom: SymptomEntry) {
        _symptoms.value = listOf(symptom) + _symptoms.value
    }

    override suspend fun addMedicalRecord(record: MedicalRecord) {
        _medicalRecords.value = listOf(record) + _medicalRecords.value
    }

    override suspend fun addDoctorQuestion(question: DoctorQuestion) {
        _doctorQuestions.value = listOf(question) + _doctorQuestions.value
    }

    override suspend fun toggleQuestionAnswered(questionId: String) {
        _doctorQuestions.value = _doctorQuestions.value.map {
            if (it.id == questionId) it.copy(isAnswered = !it.isAnswered) else it
        }
    }

    override suspend fun updateJourneyType(type: String) {
        _userProfile.value = _userProfile.value.copy(journeyType = type)
    }
}
