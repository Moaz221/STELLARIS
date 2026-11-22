package com.example.myapplication.data.care

import com.example.myapplication.core.model.*
import kotlinx.coroutines.delay

interface CareRepository {
    suspend fun getCareJourney(): CareJourneyState
    suspend fun completeMilestone(id: String): CareJourneyState
}

class FakeCareRepository : CareRepository {
    private var state = CareJourneyState(
        diagnosis = null,
        appointments = listOf(
            Appointment(
                id = "a1", title = "Follow-up consultation", facility = "Pulmonology Clinic — Room 204",
                doctor = "Your care team", dateTime = "Oct 28, 2025 • 10:30 AM",
                type = AppointmentType.CONSULTATION, status = AppointmentStatus.UPCOMING, notes = null,
                sourceType = SourceType.DEMO, verificationStatus = VerificationStatus.UNVERIFIED,
                createdAt = "2025-10-01", updatedAt = "2025-10-01"
            ),
            Appointment(
                id = "a2", title = "Chest CT scan", facility = "Imaging Center — Floor 2",
                doctor = null, dateTime = "Nov 12, 2025 • 8:00 AM",
                type = AppointmentType.IMAGING, status = AppointmentStatus.UPCOMING, notes = "Fasting not required unless your care team says so",
                sourceType = SourceType.DEMO, verificationStatus = VerificationStatus.UNVERIFIED,
                createdAt = "2025-10-01", updatedAt = "2025-10-01"
            ),
            Appointment(
                id = "a3", title = "Lab work — blood panel", facility = "Main Hospital Lab",
                doctor = null, dateTime = "Sep 30, 2025 • 7:30 AM",
                type = AppointmentType.LAB, status = AppointmentStatus.COMPLETED, notes = null,
                sourceType = SourceType.DEMO, verificationStatus = VerificationStatus.USER_CONFIRMED,
                createdAt = "2025-09-20", updatedAt = "2025-09-30"
            )
        ),
        medications = listOf(
            Medication(
                id = "m1", name = "Daily medication — morning",
                instructions = "As prescribed by your doctor", reminderTimes = listOf("8:00 AM"), isActive = true,
                sourceType = SourceType.DEMO, verificationStatus = VerificationStatus.UNVERIFIED,
                createdAt = "2025-10-01", updatedAt = "2025-10-01"
            ),
            Medication(
                id = "m2", name = "Evening medication",
                instructions = "As prescribed by your doctor", reminderTimes = listOf("9:00 PM"), isActive = true,
                sourceType = SourceType.DEMO, verificationStatus = VerificationStatus.UNVERIFIED,
                createdAt = "2025-10-01", updatedAt = "2025-10-01"
            )
        ),
        sideEffects = listOf(
            SideEffectEntry(id = "s1", name = "Mild nausea", severity = SideEffectSeverity.MILD,
                date = "Oct 20, 2025", notes = "After morning dose", sourceType = SourceType.DEMO,
                verificationStatus = VerificationStatus.UNVERIFIED, createdAt = "2025-10-20", updatedAt = "2025-10-20"),
            SideEffectEntry(id = "s2", name = "Fatigue", severity = SideEffectSeverity.MODERATE,
                date = "Oct 22, 2025", notes = null, sourceType = SourceType.DEMO,
                verificationStatus = VerificationStatus.UNVERIFIED, createdAt = "2025-10-22", updatedAt = "2025-10-22")
        ),
        milestones = listOf(
            FollowUpMilestone(id = "f1", title = "Complete follow-up scan", dueDate = "Nov 12, 2025", isCompleted = false,
                sourceType = SourceType.DEMO, verificationStatus = VerificationStatus.UNVERIFIED,
                createdAt = "2025-10-01", updatedAt = "2025-10-01"),
            FollowUpMilestone(id = "f2", title = "Bring medication list to next visit", dueDate = "Oct 28, 2025", isCompleted = true,
                sourceType = SourceType.DEMO, verificationStatus = VerificationStatus.USER_CONFIRMED,
                createdAt = "2025-10-01", updatedAt = "2025-10-26")
        )
    )

    override suspend fun getCareJourney(): CareJourneyState {
        delay(500)
        return state
    }

    override suspend fun completeMilestone(id: String): CareJourneyState {
        state = state.copy(milestones = state.milestones.map { if (it.id == id) it.copy(isCompleted = true) else it })
        return state
    }
}
