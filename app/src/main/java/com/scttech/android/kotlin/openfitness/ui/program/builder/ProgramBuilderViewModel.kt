package com.scttech.android.kotlin.openfitness.ui.program.builder

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.scttech.android.kotlin.openfitness.data.repository.ProfileRepository
import com.scttech.android.kotlin.openfitness.data.repository.ProgramRepository
import com.scttech.android.kotlin.openfitness.domain.model.Exercise
import com.scttech.android.kotlin.openfitness.domain.model.Program
import com.scttech.android.kotlin.openfitness.domain.model.ProgramConfig
import com.scttech.android.kotlin.openfitness.domain.model.ProgramGoalType
import com.scttech.android.kotlin.openfitness.domain.model.RepStrategy
import com.scttech.android.kotlin.openfitness.domain.model.RepStrategyConfig
import com.scttech.android.kotlin.openfitness.ui.navigation.ProgramBuilderRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import javax.inject.Inject

@HiltViewModel
class ProgramBuilderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val programRepository: ProgramRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val route: ProgramBuilderRoute = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow<ProgramBuilderUiState>(ProgramBuilderUiState.Loading)
    val uiState: StateFlow<ProgramBuilderUiState> = _uiState.asStateFlow()

    /** The as-loaded program being edited, kept so save() can preserve its test/prescription history. */
    private var originalProgram: Program? = null

    init {
        viewModelScope.launch {
            if (route.programId != 0L) {
                val program = programRepository.observeProgram(route.programId).first()
                if (program != null) {
                    originalProgram = program
                    val repStrategyConfig = program.config.repStrategyConfig
                    _uiState.value = ProgramBuilderUiState.Loaded(
                        programId = program.id,
                        name = program.name,
                        exerciseId = program.exerciseId,
                        exerciseName = program.exerciseName,
                        goalType = program.goalType,
                        goalTarget = program.goalTarget.toString(),
                        repStrategy = repStrategyConfig.strategy,
                        customSetTargets = (repStrategyConfig as? RepStrategyConfig.CustomManual)
                            ?.setTargets?.map { it.toString() }.orEmpty(),
                        sessionsPerWeek = program.config.sessionsPerWeek.toString(),
                        retestIntervalDays = program.config.retestIntervalDays.toString(),
                        restSeconds = program.config.restSeconds.toString(),
                    )
                    return@launch
                }
            }
            _uiState.value = ProgramBuilderUiState.Loaded(
                programId = 0L,
                name = "",
                exerciseId = null,
                exerciseName = "",
                goalType = ProgramGoalType.REPS,
                goalTarget = "100",
                repStrategy = RepStrategy.PERCENTAGE_LADDER,
                sessionsPerWeek = ProgramConfig().sessionsPerWeek.toString(),
                retestIntervalDays = ProgramConfig().retestIntervalDays.toString(),
                restSeconds = ProgramConfig().restSeconds.toString(),
            )
        }
    }

    fun updateName(name: String) = updateLoaded { copy(name = name) }
    fun updateGoalType(goalType: ProgramGoalType) = updateLoaded { copy(goalType = goalType) }
    fun updateGoalTarget(value: String) = updateLoaded { copy(goalTarget = value) }
    fun updateSessionsPerWeek(value: String) = updateLoaded { copy(sessionsPerWeek = value) }
    fun updateRetestIntervalDays(value: String) = updateLoaded { copy(retestIntervalDays = value) }
    fun updateRestSeconds(value: String) = updateLoaded { copy(restSeconds = value) }

    fun updateRepStrategy(strategy: RepStrategy) = updateLoaded {
        copy(
            repStrategy = strategy,
            customSetTargets = if (strategy == RepStrategy.CUSTOM_MANUAL && customSetTargets.isEmpty()) {
                listOf("")
            } else {
                customSetTargets
            },
        )
    }

    fun updateCustomSetTarget(index: Int, value: String) = updateLoaded {
        copy(customSetTargets = customSetTargets.toMutableList().also { it[index] = value })
    }

    fun addCustomSet() = updateLoaded { copy(customSetTargets = customSetTargets + "") }

    fun removeCustomSet(index: Int) = updateLoaded {
        copy(customSetTargets = customSetTargets.toMutableList().also { it.removeAt(index) })
    }

    fun pickExercise(exercise: Exercise) = updateLoaded {
        val newName = if (name.isBlank()) "${exercise.name} Goal" else name
        copy(exerciseId = exercise.id, exerciseName = exercise.name, name = newName)
    }

    private inline fun updateLoaded(block: ProgramBuilderUiState.Loaded.() -> ProgramBuilderUiState.Loaded) {
        val current = _uiState.value
        if (current is ProgramBuilderUiState.Loaded) {
            _uiState.update { current.block() }
        }
    }

    fun save() {
        val state = _uiState.value
        if (state !is ProgramBuilderUiState.Loaded || !state.canSave) return
        viewModelScope.launch {
            val existing = originalProgram
            val repStrategyConfig = when (state.repStrategy) {
                RepStrategy.CUSTOM_MANUAL -> RepStrategyConfig.CustomManual(
                    setTargets = state.customSetTargets.mapNotNull { it.toIntOrNull() }.filter { it > 0 },
                )
                else -> {
                    // Preserve an unchanged strategy's own parameters rather than resetting them to defaults.
                    val unchanged = existing?.config?.repStrategyConfig
                        ?.takeIf { it.strategy == state.repStrategy }
                    unchanged ?: RepStrategyConfig.default(state.repStrategy)
                }
            }
            val config = (existing?.config ?: ProgramConfig()).copy(
                repStrategyConfig = repStrategyConfig,
                sessionsPerWeek = state.sessionsPerWeek.toIntOrNull() ?: 3,
                retestIntervalDays = state.retestIntervalDays.toIntOrNull() ?: 14,
                restSeconds = state.restSeconds.toIntOrNull()?.coerceAtLeast(0) ?: 90,
            )
            val program = if (existing != null) {
                existing.copy(
                    name = state.name.trim(),
                    exerciseId = state.exerciseId,
                    exerciseName = state.exerciseName,
                    goalType = state.goalType,
                    goalTarget = state.goalTarget.toIntOrNull() ?: 100,
                    config = config,
                )
            } else {
                Program(
                    profileId = profileRepository.currentProfileId.filterNotNull().first(),
                    name = state.name.trim(),
                    exerciseId = state.exerciseId,
                    exerciseName = state.exerciseName,
                    goalType = state.goalType,
                    goalTarget = state.goalTarget.toIntOrNull() ?: 100,
                    config = config,
                    createdAt = Clock.System.now(),
                )
            }
            programRepository.saveProgram(program)
            _uiState.update { (it as ProgramBuilderUiState.Loaded).copy(saved = true) }
        }
    }
}
