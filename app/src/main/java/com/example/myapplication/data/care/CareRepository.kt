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
        appointments = emptyList(),
        medications = emptyList(),
        sideEffects = emptyList(),
        milestones = emptyList()
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
