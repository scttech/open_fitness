package com.scttech.android.kotlin.openfitness.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scttech.android.kotlin.openfitness.data.repository.ProfileRepository
import com.scttech.android.kotlin.openfitness.data.repository.ProgramRepository
import com.scttech.android.kotlin.openfitness.data.repository.ProgramSessionRepository
import com.scttech.android.kotlin.openfitness.data.repository.ScheduleRepository
import com.scttech.android.kotlin.openfitness.data.repository.SessionRepository
import com.scttech.android.kotlin.openfitness.domain.model.CalendarItem
import com.scttech.android.kotlin.openfitness.domain.model.CalendarItemType
import com.scttech.android.kotlin.openfitness.domain.model.Program
import com.scttech.android.kotlin.openfitness.domain.model.ProgramDaySkip
import com.scttech.android.kotlin.openfitness.domain.model.ProgramSession
import com.scttech.android.kotlin.openfitness.domain.model.ProgramTest
import com.scttech.android.kotlin.openfitness.domain.model.ScheduledEvent
import com.scttech.android.kotlin.openfitness.domain.model.ScheduledEventKind
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutSession
import com.scttech.android.kotlin.openfitness.domain.schedule.ScheduleInputs
import com.scttech.android.kotlin.openfitness.domain.schedule.ScheduleProjector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val profileRepository: ProfileRepository,
    programRepository: ProgramRepository,
    programSessionRepository: ProgramSessionRepository,
    sessionRepository: SessionRepository,
) : ViewModel() {

    private val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    private val mode = MutableStateFlow(CalendarMode.WEEK)
    private val selectedDate = MutableStateFlow(today)

    private val inputs = profileRepository.currentProfileId
        .filterNotNull()
        .flatMapLatest { profileId ->
            combine(
                scheduleRepository.observeEvents(profileId),
                programRepository.observeProgramsForProfile(profileId),
                programRepository.observeTestsForProfile(profileId),
                programSessionRepository.observeSessionsForProfile(profileId),
                sessionRepository.observeSessionsForProfile(profileId),
                scheduleRepository.observeProgramDaySkips(profileId),
            ) { values ->
                @Suppress("UNCHECKED_CAST")
                ScheduleInputs(
                    events = values[0] as List<ScheduledEvent>,
                    programs = values[1] as List<Program>,
                    programTests = values[2] as List<ProgramTest>,
                    programSessions = values[3] as List<ProgramSession>,
                    workoutSessions = values[4] as List<WorkoutSession>,
                    programDaySkips = values[5] as List<ProgramDaySkip>,
                )
            }
        }

    val uiState: StateFlow<CalendarUiState> = combine(inputs, mode, selectedDate) { inputs, mode, selected ->
        val range = visibleRangeFor(mode, selected)
        val lookbackStart = minOf(range.start, today.plus(-STREAK_LOOKBACK_DAYS, DateTimeUnit.DAY))
        val all = ScheduleProjector.project(inputs, lookbackStart, range.endInclusive, today)
        CalendarUiState(
            mode = mode,
            selectedDate = selected,
            today = today,
            items = all.filter { it.date in range },
            streak = ScheduleProjector.currentStreak(all.filter { it.date <= today }, today),
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CalendarUiState(CalendarMode.WEEK, today, today, emptyList(), 0, isLoading = true),
    )

    fun setMode(newMode: CalendarMode) {
        mode.value = newMode
    }

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun goToToday() {
        selectedDate.value = today
    }

    /** Steps one day/week/month in [direction] (-1 or +1), matching the current mode. */
    fun step(direction: Int) {
        selectedDate.update { date ->
            when (mode.value) {
                CalendarMode.DAY -> date.plus(direction, DateTimeUnit.DAY)
                CalendarMode.WEEK -> date.plus(direction, DateTimeUnit.WEEK)
                CalendarMode.MONTH -> date.plus(direction, DateTimeUnit.MONTH)
            }
        }
    }

    fun setDone(item: CalendarItem, done: Boolean) {
        val eventId = item.eventId ?: return
        viewModelScope.launch { scheduleRepository.setOccurrenceCompleted(eventId, item.date, done) }
    }

    /** Removes just this occurrence from the calendar. */
    fun removeOccurrence(item: CalendarItem) {
        viewModelScope.launch {
            val eventId = item.eventId
            if (eventId != null) {
                scheduleRepository.skipOccurrence(eventId, item.date)
            } else if (item.programId != null) {
                scheduleRepository.skipProgramDay(currentProfileId(), item.programId, item.date)
            }
        }
    }

    /** Deletes a recurring event's entire series. */
    fun deleteSeries(item: CalendarItem) {
        val eventId = item.eventId ?: return
        viewModelScope.launch { scheduleRepository.deleteEvent(eventId) }
    }

    /** Moves this one occurrence (a missed or upcoming item) to [newDate]. */
    fun moveOccurrence(item: CalendarItem, newDate: LocalDate) {
        if (newDate == item.date) return
        viewModelScope.launch {
            val eventId = item.eventId
            val original = eventId?.let { scheduleRepository.getEvent(it) }
            if (original != null && !original.isRecurring) {
                scheduleRepository.saveEvent(original.copy(date = newDate, completedDates = emptySet()))
                return@launch
            }
            val profileId = currentProfileId()
            val moved = original?.copy(
                id = 0L,
                date = newDate,
                repeatDays = emptySet(),
                repeatUntil = null,
                completedDates = emptySet(),
                skippedDates = emptySet(),
            ) ?: ScheduledEvent(
                profileId = profileId,
                kind = if (item.type == CalendarItemType.PROGRAM_TEST) ScheduledEventKind.PROGRAM_TEST else ScheduledEventKind.PROGRAM_SESSION,
                title = item.title,
                programId = item.programId,
                date = newDate,
            )
            scheduleRepository.saveEvent(moved)
            if (eventId != null) {
                scheduleRepository.skipOccurrence(eventId, item.date)
            } else if (item.programId != null) {
                scheduleRepository.skipProgramDay(profileId, item.programId, item.date)
            }
        }
    }

    private suspend fun currentProfileId(): Long = profileRepository.currentProfileId.filterNotNull().first()

    private companion object {
        const val STREAK_LOOKBACK_DAYS = 120
    }
}
