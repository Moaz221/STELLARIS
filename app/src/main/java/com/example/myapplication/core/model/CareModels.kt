package com.example.myapplication.core.model

enum class SourceType { PATIENT_ENTERED, CLINICIAN_PROVIDED, IMPORTED, AI_GENERATED, DEMO }
enum class VerificationStatus { UNVERIFIED, USER_CONFIRMED, CLINICIAN_CONFIRMED }

interface HealthRecord {
    val id: String
    val sourceType: SourceType
    val verificationStatus: VerificationStatus
    val createdAt: String
    val updatedAt: String
}

enum class JourneyType { EARLY_DETECTION, EVALUATION, DIAGNOSED }

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

enum class AppointmentType { CONSULTATION, IMAGING, INFUSION, LAB, OTHER }
enum class AppointmentStatus { UPCOMING, COMPLETED, MISSED, CANCELLED }

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

enum class SideEffectSeverity { MILD, MODERATE, SEVERE }

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
