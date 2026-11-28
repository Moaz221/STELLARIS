package com.example.myapplication.core.model

@kotlinx.serialization.Serializable
enum class SourceType { PATIENT_ENTERED, CLINICIAN_PROVIDED, IMPORTED, AI_GENERATED, DEMO }
@kotlinx.serialization.Serializable
enum class VerificationStatus { UNVERIFIED, USER_CONFIRMED, CLINICIAN_CONFIRMED }

interface HealthRecord {
    val id: String
    val sourceType: SourceType
    val verificationStatus: VerificationStatus
    val createdAt: String
    val updatedAt: String
}

@kotlinx.serialization.Serializable
enum class JourneyType { EARLY_DETECTION, EVALUATION, DIAGNOSED }

@kotlinx.serialization.Serializable
data class DiagnosisInfo(
    override val id: String,
    val title: String,
    val details: String,
    val confirmedDate: String?,
    val treatingSpecialist: String,
    override val sourceType: SourceType,
    override val verificationStatus: VerificationStatus,
    override val createdAt: String,
    override val updatedAt: String
) : HealthRecord

@kotlinx.serialization.Serializable
enum class AppointmentType { CONSULTATION, IMAGING, INFUSION, LAB, OTHER }
@kotlinx.serialization.Serializable
enum class AppointmentStatus { UPCOMING, COMPLETED, MISSED, CANCELLED }

@kotlinx.serialization.Serializable
data class Appointment(
    override val id: String,
    val title: String,
    val facility: String,
    val doctor: String?,
    val dateTime: String,
    val type: AppointmentType,
    val status: AppointmentStatus,
    val notes: String?,
    override val sourceType: SourceType,
    override val verificationStatus: VerificationStatus,
    override val createdAt: String,
    override val updatedAt: String
) : HealthRecord

@kotlinx.serialization.Serializable
data class Medication(
    override val id: String,
    val name: String,
    val instructions: String,
    val reminderTimes: List<String>,
    val isActive: Boolean,
    override val sourceType: SourceType,
    override val verificationStatus: VerificationStatus,
    override val createdAt: String,
    override val updatedAt: String
) : HealthRecord

@kotlinx.serialization.Serializable
enum class SideEffectSeverity { MILD, MODERATE, SEVERE }

@kotlinx.serialization.Serializable
data class SideEffectEntry(
    override val id: String,
    val name: String,
    val severity: SideEffectSeverity,
    val date: String,
    val notes: String?,
    override val sourceType: SourceType,
    override val verificationStatus: VerificationStatus,
    override val createdAt: String,
    override val updatedAt: String
) : HealthRecord

@kotlinx.serialization.Serializable
data class FollowUpMilestone(
    override val id: String,
    val title: String,
    val dueDate: String?,
    val isCompleted: Boolean,
    override val sourceType: SourceType,
    override val verificationStatus: VerificationStatus,
    override val createdAt: String,
    override val updatedAt: String
) : HealthRecord

data class CareJourneyState(
    val diagnosis: DiagnosisInfo?,
    val appointments: List<Appointment>,
    val medications: List<Medication>,
    val sideEffects: List<SideEffectEntry>,
    val milestones: List<FollowUpMilestone>
)
