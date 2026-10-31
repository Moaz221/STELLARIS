package com.example.myapplication.data.model

enum class SourceType {
    PATIENT_ENTERED,
    CLINICIAN_PROVIDED,
    IMPORTED,
    AI_GENERATED
}

enum class VerificationStatus {
    UNVERIFIED,
    USER_CONFIRMED,
    CLINICIAN_CONFIRMED
}

data class SymptomEntry(
    val id: String,
    val name: String,
    val severity: String,
    val date: String,
    val isUrgent: Boolean = false,
    val sourceType: SourceType = SourceType.PATIENT_ENTERED,
    val verificationStatus: VerificationStatus = VerificationStatus.USER_CONFIRMED
)

data class MedicalRecord(
    val id: String,
    val title: String,
    val type: String,
    val date: String,
    val source: String,
    val sourceType: SourceType = SourceType.PATIENT_ENTERED,
    val verificationStatus: VerificationStatus = VerificationStatus.CLINICIAN_CONFIRMED
)

data class DoctorQuestion(
    val id: String,
    val text: String,
    val category: String,
    val isAnswered: Boolean = false
)

data class SideEffectEntry(
    val id: String,
    val name: String,
    val severity: String,
    val date: String,
    val notes: String = ""
)

data class Appointment(
    val id: String,
    val title: String,
    val doctorName: String,
    val location: String,
    val dateTime: String
)
