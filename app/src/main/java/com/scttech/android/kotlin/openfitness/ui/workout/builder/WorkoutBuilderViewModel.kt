package com.scttech.android.kotlin.openfitness.ui.workout.builder

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.scttech.android.kotlin.openfitness.data.repository.ProfileRepository
import com.scttech.android.kotlin.openfitness.data.repository.WorkoutRepository
import com.scttech.android.kotlin.openfitness.domain.model.Workout
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutStyle
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutStyleConfig
import com.scttech.android.kotlin.openfitness.ui.navigation.WorkoutBuilderRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import javax.inject.Inject

@HiltViewModel
class WorkoutBuilderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val route: WorkoutBuilderRoute = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow<WorkoutBuilderUiState>(WorkoutBuilderUiState.Loading)
    val uiState: StateFlow<WorkoutBuilderUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            if (route.workoutId != 0L) {
                val workout = workoutRepository.observeWorkout(route.workoutId).first()
                if (workout != null) {
                    _uiState.value = WorkoutBuilderUiState.Loaded(
                        workoutId = workout.id,
                        isNew = false,
                        style = workout.style,
                        name = workout.name,
                        notes = workout.notes,
                        circuitExerciseNames = workout.exercises.sortedBy { it.order }.map { it.name },
                        fields = StyleFormFields.from(workout.styleConfig, workout.exercises),
                    )
                    return@launch
                }
            }
            val style = route.newWorkoutStyle?.let { runCatching { WorkoutStyle.valueOf(it) }.getOrNull() } ?: WorkoutStyle.TABATA
            _uiState.value = WorkoutBuilderUiState.Loaded(
                workoutId = 0L,
                isNew = true,
                style = style,
                name = "",
                notes = "",
                circuitExerciseNames = listOf(""),
                fields = StyleFormFields.default(style),
            )
        }
    }

    fun updateName(name: String) = updateLoaded { copy(name = name) }
    fun updateNotes(notes: String) = updateLoaded { copy(notes = notes) }
    fun updateFields(fields: StyleFormFields) = updateLoaded { copy(fields = fields) }

    fun updateCircuitExerciseName(index: Int, name: String) = updateLoaded {
        copy(circuitExerciseNames = circuitExerciseNames.toMutableList().also { it[index] = name })
    }

    fun addCircuitExercise() = updateLoaded { copy(circuitExerciseNames = circuitExerciseNames + "") }

    fun removeCircuitExercise(index: Int) = updateLoaded {
        copy(circuitExerciseNames = circuitExerciseNames.toMutableList().also { it.removeAt(index) })
    }

    private inline fun updateLoaded(block: WorkoutBuilderUiState.Loaded.() -> WorkoutBuilderUiState.Loaded) {
        val current = _uiState.value
        if (current is WorkoutBuilderUiState.Loaded) {
            _uiState.update { current.block() }
        }
    }

    fun save() {
        val state = _uiState.value
        if (state !is WorkoutBuilderUiState.Loaded || !state.canSave) return
        viewModelScope.launch {
            val profileId = profileRepository.currentProfileId.filterNotNull().first()
            val exercises = state.fields.toExercises(state.circuitExerciseNames.filter { it.isNotBlank() })
            val workout = Workout(
                id = state.workoutId,
                profileId = profileId,
                name = state.name.trim(),
                style = state.style,
                styleConfig = state.fields.toStyleConfig(),
                exercises = exercises,
                notes = state.notes,
                isTemplate = false,
                createdAt = Clock.System.now(),
            )
            workoutRepository.saveWorkout(workout)
            _uiState.update { (it as WorkoutBuilderUiState.Loaded).copy(saved = true) }
        }
    }
}
