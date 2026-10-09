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
            val unit = profileRepository.weightUnit.first()
            if (route.workoutId != 0L) {
                val workout = workoutRepository.observeWorkout(route.workoutId).first()
                if (workout != null) {
                    _uiState.value = WorkoutBuilderUiState.Loaded(
                        workoutId = workout.id,
                        isNew = false,
                        style = workout.style,
                        name = workout.name,
                        notes = workout.notes,
                        circuitExercises = workout.exercises.sortedBy { it.order }
                            .map { CircuitExerciseField(name = it.name, exerciseId = it.exerciseId) },
                        followAlongSteps = workout.exercises.sortedBy { it.order }.map {
                            FollowAlongStepField(
                                isRest = it.isRest,
                                name = it.name,
                                exerciseId = it.exerciseId,
                                seconds = (it.targetDurationSeconds ?: 30).toString(),
                            )
                        },
                        fields = StyleFormFields.from(workout.styleConfig, workout.exercises, unit),
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
                circuitExercises = listOf(CircuitExerciseField()),
                followAlongSteps = listOf(FollowAlongStepField(isRest = false)),
                fields = StyleFormFields.default(style, unit),
            )
        }
    }

    fun updateName(name: String) = updateLoaded { copy(name = name, nameError = null) }
    fun updateNotes(notes: String) = updateLoaded { copy(notes = notes) }
    fun updateFields(fields: StyleFormFields) = updateLoaded { copy(fields = fields) }

    fun pickCircuitExercise(index: Int, name: String, exerciseId: Long?) = updateLoaded {
        copy(
            circuitExercises = circuitExercises.toMutableList()
                .also { it[index] = CircuitExerciseField(name = name, exerciseId = exerciseId) },
        )
    }

    fun addCircuitExercise() = updateLoaded { copy(circuitExercises = circuitExercises + CircuitExerciseField()) }

    fun removeCircuitExercise(index: Int) = updateLoaded {
        copy(circuitExercises = circuitExercises.toMutableList().also { it.removeAt(index) })
    }

    fun addFollowAlongExercise() = updateLoaded {
        copy(followAlongSteps = followAlongSteps + FollowAlongStepField(isRest = false))
    }

    fun addFollowAlongRest() = updateLoaded {
        val defaultRest = (fields as? StyleFormFields.FollowAlongFields)?.defaultRestSeconds ?: "15"
        copy(followAlongSteps = followAlongSteps + FollowAlongStepField(isRest = true, name = "Rest", seconds = defaultRest))
    }

    fun pickFollowAlongExercise(index: Int, name: String, exerciseId: Long?) = updateLoaded {
        copy(
            followAlongSteps = followAlongSteps.toMutableList()
                .also { it[index] = it[index].copy(name = name, exerciseId = exerciseId) },
        )
    }

    fun updateFollowAlongSeconds(index: Int, value: String) = updateLoaded {
        copy(followAlongSteps = followAlongSteps.toMutableList().also { it[index] = it[index].copy(seconds = value) })
    }

    fun removeFollowAlongStep(index: Int) = updateLoaded {
        copy(followAlongSteps = followAlongSteps.toMutableList().also { it.removeAt(index) })
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
            val unit = profileRepository.weightUnit.first()
            val trimmedName = state.name.trim()
            if (workoutRepository.nameExists(profileId, trimmedName, state.workoutId)) {
                _uiState.update {
                    (it as WorkoutBuilderUiState.Loaded).copy(nameError = "A workout named \"$trimmedName\" already exists.")
                }
                return@launch
            }
            val exercises = if (state.style == WorkoutStyle.FOLLOW_ALONG) {
                state.followAlongSteps.toWorkoutExercises()
            } else {
                state.fields.toExercises(state.circuitExercises.filter { it.name.isNotBlank() }, unit)
            }
            val workout = Workout(
                id = state.workoutId,
                profileId = profileId,
                name = trimmedName,
                style = state.style,
                styleConfig = state.fields.toStyleConfig(unit),
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
