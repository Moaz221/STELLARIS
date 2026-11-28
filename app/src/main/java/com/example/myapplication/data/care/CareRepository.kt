package com.example.myapplication.data.care

import com.example.myapplication.core.model.*
import com.example.myapplication.data.local.AppStore

interface CareRepository {
    suspend fun getCareJourney(): CareJourneyState
    suspend fun completeMilestone(id: String): CareJourneyState
    suspend fun addAppointment(item: Appointment): CareJourneyState
    suspend fun addMedication(item: Medication): CareJourneyState
    suspend fun addSideEffect(item: SideEffectEntry): CareJourneyState
    suspend fun delete(kind: String, id: String): CareJourneyState
}

/** Stores only what the user entered, on the phone, per signed-in user. No demo data. */
class LocalCareRepository : CareRepository {

    private suspend fun load() = CareJourneyState(
        diagnosis = null,
        appointments = AppStore.load("care_appointments", Appointment.serializer()),
        medications = AppStore.load("care_medications", Medication.serializer()),
        sideEffects = AppStore.load("care_side_effects", SideEffectEntry.serializer()),
        milestones = AppStore.load("care_milestones", FollowUpMilestone.serializer())
    )

    override suspend fun getCareJourney() = load()

    override suspend fun completeMilestone(id: String): CareJourneyState {
        val list = AppStore.load("care_milestones", FollowUpMilestone.serializer()).map { if (it.id == id) it.copy(isCompleted = true) else it }
        AppStore.save("care_milestones", FollowUpMilestone.serializer(), list)
        return load()
    }

    override suspend fun addAppointment(item: Appointment): CareJourneyState {
        AppStore.save("care_appointments", Appointment.serializer(), listOf(item) + AppStore.load("care_appointments", Appointment.serializer()))
        return load()
    }

    override suspend fun addMedication(item: Medication): CareJourneyState {
        AppStore.save("care_medications", Medication.serializer(), listOf(item) + AppStore.load("care_medications", Medication.serializer()))
        return load()
    }

    override suspend fun addSideEffect(item: SideEffectEntry): CareJourneyState {
        AppStore.save("care_side_effects", SideEffectEntry.serializer(), listOf(item) + AppStore.load("care_side_effects", SideEffectEntry.serializer()))
        return load()
    }

    override suspend fun delete(kind: String, id: String): CareJourneyState {
        when (kind) {
            "appointment" -> AppStore.save("care_appointments", Appointment.serializer(), AppStore.load("care_appointments", Appointment.serializer()).filter { it.id != id })
            "medication" -> AppStore.save("care_medications", Medication.serializer(), AppStore.load("care_medications", Medication.serializer()).filter { it.id != id })
            "side_effect" -> AppStore.save("care_side_effects", SideEffectEntry.serializer(), AppStore.load("care_side_effects", SideEffectEntry.serializer()).filter { it.id != id })
        }
        return load()
    }
}
