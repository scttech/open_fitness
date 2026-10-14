package com.scttech.android.kotlin.openfitness.data.repository

import com.scttech.android.kotlin.openfitness.data.local.dao.ScheduleDao
import com.scttech.android.kotlin.openfitness.data.local.entity.ProgramDaySkipEntity
import com.scttech.android.kotlin.openfitness.data.local.entity.asDomainModel
import com.scttech.android.kotlin.openfitness.data.local.entity.asEntity
import com.scttech.android.kotlin.openfitness.domain.model.ProgramDaySkip
import com.scttech.android.kotlin.openfitness.domain.model.ScheduledEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepositoryImpl @Inject constructor(
    private val scheduleDao: ScheduleDao,
) : ScheduleRepository {

    override fun observeEvents(profileId: Long): Flow<List<ScheduledEvent>> =
        scheduleDao.observeEventsForProfile(profileId).map { list -> list.map { it.asDomainModel() } }

    override fun observeProgramDaySkips(profileId: Long): Flow<List<ProgramDaySkip>> =
        scheduleDao.observeProgramDaySkips(profileId).map { list -> list.map { it.asDomainModel() } }

    override suspend fun getEvent(id: Long): ScheduledEvent? = scheduleDao.getEvent(id)?.asDomainModel()

    override suspend fun getEvents(profileId: Long): List<ScheduledEvent> =
        scheduleDao.getEventsForProfile(profileId).map { it.asDomainModel() }

    override suspend fun saveEvent(event: ScheduledEvent): Long =
        if (event.id == 0L) {
            scheduleDao.insert(event.asEntity())
        } else {
            scheduleDao.update(event.asEntity())
            event.id
        }

    override suspend fun deleteEvent(eventId: Long) {
        scheduleDao.deleteEvent(eventId)
    }

    override suspend fun skipOccurrence(eventId: Long, date: LocalDate) {
        val event = getEvent(eventId) ?: return
        if (event.isRecurring) {
            saveEvent(event.copy(skippedDates = event.skippedDates + date))
        } else {
            deleteEvent(eventId)
        }
    }

    override suspend fun setOccurrenceCompleted(eventId: Long, date: LocalDate, completed: Boolean) {
        val event = getEvent(eventId) ?: return
        val updated = if (completed) event.completedDates + date else event.completedDates - date
        saveEvent(event.copy(completedDates = updated))
    }

    override suspend fun skipProgramDay(profileId: Long, programId: Long, date: LocalDate) {
        scheduleDao.insertProgramDaySkip(
            ProgramDaySkipEntity(profileId = profileId, programId = programId, dateEpochDay = date.toEpochDays().toLong()),
        )
    }
}
