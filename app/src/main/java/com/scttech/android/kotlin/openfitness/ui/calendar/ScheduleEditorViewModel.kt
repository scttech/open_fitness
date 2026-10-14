package com.scttech.android.kotlin.openfitness.ui.calendar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.scttech.android.kotlin.openfitness.data.repository.ProfileRepository
import com.scttech.android.kotlin.openfitness.data.repository.ProgramRepository
import com.scttech.android.kotlin.openfitness.data.repository.ScheduleRepository
import com.scttech.android.kotlin.openfitness.data.repository.WorkoutRepository
import com.scttech.android.kotlin.openfitness.domain.model.Program
import com.scttech.android.kotlin.openfitness.domain.model.ScheduledEvent
import com.scttech.android.kotlin.openfitness.domain.model.ScheduledEventKind
import com.scttech.android.kotlin.openfitness.domain.model.Workout
import com.scttech.android.kotlin.openfitness.ui.navigation.ScheduleEditorRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import javax.inject.Inject

data class ScheduleEditorUiState(
    val isLoading: Boolean = true,
    val eventId: Long = 0L,
    val kind: ScheduledEventKind = ScheduledEventKind.WORKOUT,
    val title: String = "",
    val workoutId: Long? = null,
    val programId: Long? = null,
    val date: LocalDate = LocalDate(2000, 1, 1),
    val startMinutes: Int? = null,
    val reminderMinutesBefore: Int? = null,
    val repeatDays: Set<DayOfWeek> = emptySet(),
    val repeatUntil: LocalDate? = null,
    val notes: String = "",
    val workouts: List<Workout> = emptyList(),
    val programs: List<Program> = emptyList(),
    /** Opened from a workout's own screen: the workout is fixed, so no kind or workout choice is offered. */
    val isWorkoutLocked: Boolean = false,
) {
    val isEditing: Boolean get() = eventId != 0L

    val canSave: Boolean
        get() = when (kind) {
            ScheduledEventKind.WORKOUT -> workoutId != null
            ScheduledEventKind.PROGRAM_SESSION, ScheduledEventKind.PROGRAM_TEST -> programId != null
            ScheduledEventKind.CUSTOM -> title.isNotBlank()
        }
}

@HiltViewModel
class ScheduleEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val scheduleRepository: ScheduleRepository,
    private val profileRepository: ProfileRepository,
    workoutRepository: WorkoutRepository,
    programRepository: ProgramRepository,
) : ViewModel() {

    private val route: ScheduleEditorRoute = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(ScheduleEditorUiState())
    val uiState: StateFlow<ScheduleEditorUiState> = _uiState.asStateFlow()

    private var original: ScheduledEvent? = null

    init {
        viewModelScope.launch {
            val profileId = profileRepository.currentProfileId.filterNotNull().first()
            val workouts = workoutRepository.observeWorkoutsForProfile(profileId).first()
            val programs = programRepository.observeProgramsForProfile(profileId).first().filterNot { it.isArchived }
            val existing = if (route.eventId != 0L) scheduleRepository.getEvent(route.eventId) else null
            original = existing
            _uiState.value = if (existing != null) {
                ScheduleEditorUiState(
                    isLoading = false,
                    eventId = existing.id,
                    kind = existing.kind,
                    title = existing.title,
                    workoutId = existing.workoutId,
                    programId = existing.programId,
                    date = existing.date,
                    startMinutes = existing.startMinutes,
                    reminderMinutesBefore = existing.reminderMinutesBefore,
                    repeatDays = existing.repeatDays,
                    repeatUntil = existing.repeatUntil,
                    notes = existing.notes,
                    workouts = workouts,
                    programs = programs,
                )
            } else {
                val preselected = workouts.firstOrNull { it.id == route.workoutId }
                ScheduleEditorUiState(
                    isLoading = false,
                    kind = ScheduledEventKind.WORKOUT,
                    workoutId = preselected?.id,
                    title = preselected?.name.orEmpty(),
                    isWorkoutLocked = preselected != null,
                    date = LocalDate.fromEpochDays(route.epochDay),
                    workouts = workouts,
                    programs = programs,
                )
            }
        }
    }

    fun setKind(kind: ScheduledEventKind) = _uiState.update { it.copy(kind = kind) }

    fun setTitle(title: String) = _uiState.update { it.copy(title = title) }

    fun selectWorkout(workout: Workout) = _uiState.update { it.copy(workoutId = workout.id, title = workout.name) }

    fun selectProgram(program: Program) = _uiState.update { it.copy(programId = program.id, title = program.name) }

    fun setDate(date: LocalDate) = _uiState.update { it.copy(date = date) }

    fun setStartMinutes(minutes: Int?) = _uiState.update {
        it.copy(startMinutes = minutes, reminderMinutesBefore = if (minutes == null) null else it.reminderMinutesBefore)
    }

    fun setReminder(minutesBefore: Int?) = _uiState.update { it.copy(reminderMinutesBefore = minutesBefore) }

    fun toggleRepeatDay(day: DayOfWeek) = _uiState.update {
        it.copy(repeatDays = if (day in it.repeatDays) it.repeatDays - day else it.repeatDays + day)
    }

    fun setRepeatUntil(date: LocalDate?) = _uiState.update { it.copy(repeatUntil = date) }

    fun setNotes(notes: String) = _uiState.update { it.copy(notes = notes) }

    fun save(onSaved: () -> Unit) {
        val state = _uiState.value
        if (!state.canSave) return
        viewModelScope.launch {
            val profileId = profileRepository.currentProfileId.filterNotNull().first()
            val base = original ?: ScheduledEvent(profileId = profileId, kind = state.kind, title = "", date = state.date)
            val title = state.title.ifBlank {
                state.workouts.firstOrNull { it.id == state.workoutId }?.name
                    ?: state.programs.firstOrNull { it.id == state.programId }?.name
                    ?: "Scheduled event"
            }
            scheduleRepository.saveEvent(
                base.copy(
                    kind = state.kind,
                    title = title,
                    workoutId = state.workoutId.takeIf { state.kind == ScheduledEventKind.WORKOUT },
                    programId = state.programId.takeIf {
                        state.kind == ScheduledEventKind.PROGRAM_SESSION || state.kind == ScheduledEventKind.PROGRAM_TEST
                    },
                    date = state.date,
                    startMinutes = state.startMinutes,
                    notes = state.notes.trim(),
                    reminderMinutesBefore = state.reminderMinutesBefore.takeIf { state.startMinutes != null },
                    repeatDays = state.repeatDays,
                    repeatUntil = state.repeatUntil.takeIf { state.repeatDays.isNotEmpty() },
                ),
            )
            onSaved()
        }
    }

    fun delete(onDeleted: () -> Unit) {
        val id = original?.id ?: return
        viewModelScope.launch {
            scheduleRepository.deleteEvent(id)
            onDeleted()
        }
    }
}
