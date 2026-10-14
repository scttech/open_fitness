package com.scttech.android.kotlin.openfitness.data.repository

import com.scttech.android.kotlin.openfitness.domain.model.ProgramDaySkip
import com.scttech.android.kotlin.openfitness.domain.model.ScheduledEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface ScheduleRepository {
    fun observeEvents(profileId: Long): Flow<List<ScheduledEvent>>
    fun observeProgramDaySkips(profileId: Long): Flow<List<ProgramDaySkip>>
    suspend fun getEvent(id: Long): ScheduledEvent?
    suspend fun getEvents(profileId: Long): List<ScheduledEvent>
    suspend fun saveEvent(event: ScheduledEvent): Long
    /** Deletes the whole event, including every recurrence. */
    suspend fun deleteEvent(eventId: Long)
    /** Removes a single occurrence of a recurring event (or deletes a one-off). */
    suspend fun skipOccurrence(eventId: Long, date: LocalDate)
    suspend fun setOccurrenceCompleted(eventId: Long, date: LocalDate, completed: Boolean)
    /** Hides [programId]'s projected workout/test day on [date]. */
    suspend fun skipProgramDay(profileId: Long, programId: Long, date: LocalDate)
}
