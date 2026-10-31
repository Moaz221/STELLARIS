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

    private val _symptoms = MutableStateFlow(
        listOf(
            SymptomEntry("1", "Persistent Cough", "Moderate", "Today, 10:30 AM"),
            SymptomEntry("2", "Mild Chest Tightness", "Mild", "Yesterday, 4:15 PM"),
            SymptomEntry("3", "Shortness of Breath", "Severe", "2 days ago", isUrgent = true)
        )
    )
    override val symptoms: StateFlow<List<SymptomEntry>> = _symptoms.asStateFlow()

    private val _medicalRecords = MutableStateFlow(
        listOf(
            MedicalRecord("1", "Chest X-Ray Digital Copy", "Imaging", "Oct 12, 2024", "Patient Uploaded"),
            MedicalRecord("2", "LDCT Screening Report", "Scan Report", "Sep 04, 2024", "Clinician Confirmed"),
            MedicalRecord("3", "Pulmonology Consult Notes", "Doctor Notes", "Aug 18, 2024", "Clinician Confirmed")
        )
    )
    override val medicalRecords: StateFlow<List<MedicalRecord>> = _medicalRecords.asStateFlow()

    private val _doctorQuestions = MutableStateFlow(
        listOf(
            DoctorQuestion("1", "What are the next steps if my LDCT scan shows a small nodule?", "Screening", false),
            DoctorQuestion("2", "How often do I need follow-up imaging during treatment?", "Treatment", false),
            DoctorQuestion("3", "Are there specific dietary guidelines I should follow?", "General", true)
        )
    )
    override val doctorQuestions: StateFlow<List<DoctorQuestion>> = _doctorQuestions.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfile())
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
