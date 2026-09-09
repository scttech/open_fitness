package com.scttech.android.kotlin.openfitness.ui.session

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.scttech.android.kotlin.openfitness.data.repository.ProfileRepository
import com.scttech.android.kotlin.openfitness.data.repository.SessionRepository
import com.scttech.android.kotlin.openfitness.data.repository.WorkoutRepository
import com.scttech.android.kotlin.openfitness.domain.model.PerformedSet
import com.scttech.android.kotlin.openfitness.domain.model.SessionResult
import com.scttech.android.kotlin.openfitness.domain.model.Workout
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutSession
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutStyle
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutStyleConfig
import com.scttech.android.kotlin.openfitness.ui.navigation.ActiveSessionRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
class ActiveSessionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository,
    private val sessionRepository: SessionRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val route: ActiveSessionRoute = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow<ActiveSessionUiState>(ActiveSessionUiState.Loading)
    val uiState: StateFlow<ActiveSessionUiState> = _uiState.asStateFlow()

    private lateinit var workout: Workout
    private val startedAt = Clock.System.now()
    private var tickerJob: Job? = null

    // Tabata-only running counters.
    private var tabataRoundsCompleted = 0
    private var tabataCyclesCompleted = 0

    // Density-only running counters.
    private var densityElapsed = 0

    private var toneGenerator: ToneGenerator? = null

    init {
        viewModelScope.launch {
            val loaded = workoutRepository.observeWorkout(route.workoutId).first() ?: return@launch
            workout = loaded
            _uiState.value = initialStateFor(loaded)
        }
    }

    private fun initialStateFor(workout: Workout): ActiveSessionUiState = when (val config = workout.styleConfig) {
        is WorkoutStyleConfig.Tabata -> ActiveSessionUiState.TabataSession(
            workoutName = workout.name,
            config = config,
            exercises = workout.exercises.sortedBy { it.order }.map { it.name }.ifEmpty { listOf(workout.name) },
            phase = TabataPhase.WORK,
            currentCycle = 1,
            currentRound = 1,
            currentExerciseIndex = 0,
            secondsRemaining = config.workSeconds,
            currentExerciseName = workout.exercises.sortedBy { it.order }.firstOrNull()?.name ?: workout.name,
            isRunning = false,
            isFinished = false,
        )
        is WorkoutStyleConfig.Density -> ActiveSessionUiState.DensitySession(
            workoutName = workout.name,
            config = config,
            exercises = workout.exercises.sortedBy { it.order }.map { it.name },
            elapsedSeconds = 0,
            roundsCompleted = 0,
            isRunning = false,
            isFinished = false,
        )
        is WorkoutStyleConfig.GreaseTheGroove -> ActiveSessionUiState.SetLoggingSession(
            workoutName = workout.name,
            style = WorkoutStyle.GREASE_THE_GROOVE,
            exerciseName = config.exerciseName,
            targetDescription = "${config.repsPerSet} reps, target ${config.targetSetsPerDay} sets today",
            nextSetTargetReps = config.repsPerSet,
            nextSetTargetWeightKg = null,
            loggedSets = emptyList(),
            repsInput = config.repsPerSet.toString(),
            weightInput = "",
            isFinished = false,
        )
        is WorkoutStyleConfig.Pyramid -> {
            val scheme = config.repScheme()
            ActiveSessionUiState.SetLoggingSession(
                workoutName = workout.name,
                style = WorkoutStyle.PYRAMID,
                exerciseName = workout.exercises.firstOrNull()?.name ?: workout.name,
                targetDescription = "Scheme: ${scheme.joinToString("-")} reps",
                nextSetTargetReps = scheme.firstOrNull(),
                nextSetTargetWeightKg = null,
                loggedSets = emptyList(),
                repsInput = scheme.firstOrNull()?.toString().orEmpty(),
                weightInput = "",
                isFinished = false,
            )
        }
        is WorkoutStyleConfig.StepLoading -> ActiveSessionUiState.SetLoggingSession(
            workoutName = workout.name,
            style = WorkoutStyle.STEP_LOADING,
            exerciseName = workout.exercises.firstOrNull()?.name ?: workout.name,
            targetDescription = "${config.setCount} sets × ${config.repsPerSet} reps",
            nextSetTargetReps = config.repsPerSet,
            nextSetTargetWeightKg = config.weightForSet(0),
            loggedSets = emptyList(),
            repsInput = config.repsPerSet.toString(),
            weightInput = config.weightForSet(0).toString(),
            isFinished = false,
        )
    }

    // ---- Tabata timer ----

    fun toggleTabataRunning() {
        val state = _uiState.value as? ActiveSessionUiState.TabataSession ?: return
        if (state.isRunning) {
            tickerJob?.cancel()
            _uiState.update { (it as ActiveSessionUiState.TabataSession).copy(isRunning = false) }
        } else {
            _uiState.update { (it as ActiveSessionUiState.TabataSession).copy(isRunning = true) }
            tickerJob = viewModelScope.launch {
                while (true) {
                    delay(1_000)
                    tickTabata()
                }
            }
        }
    }

    private fun tickTabata() {
        val state = _uiState.value as? ActiveSessionUiState.TabataSession ?: return
        val isCountdownPhase = state.phase == TabataPhase.WORK || state.phase == TabataPhase.ROUND_REST
        val playSounds = state.config.countdownSoundEnabled && isCountdownPhase
        if (state.secondsRemaining > 1) {
            val secondsRemaining = state.secondsRemaining - 1
            _uiState.update { (it as ActiveSessionUiState.TabataSession).copy(secondsRemaining = secondsRemaining) }
            if (playSounds && secondsRemaining <= COUNTDOWN_TICK_SECONDS) {
                playCountdownTick()
            }
            return
        }
        // Current phase's time is up - advance to the next phase.
        if (playSounds) {
            playIntervalFinishedSound()
        }
        val config = state.config
        when (state.phase) {
            TabataPhase.WORK -> {
                if (state.currentExerciseIndex < state.exercises.lastIndex) {
                    // More exercises left in this round - rest, then move to the next one.
                    _uiState.update { state.copy(phase = TabataPhase.REST, secondsRemaining = config.restSeconds) }
                } else {
                    // That was the last exercise - the round (one full pass through all exercises) is complete.
                    tabataRoundsCompleted++
                    val roundCompletesCycle = state.currentRound >= config.roundsPerCycle
                    if (roundCompletesCycle) tabataCyclesCompleted++
                    if (roundCompletesCycle && state.currentCycle >= config.cycles) {
                        finishTabata(state)
                    } else {
                        _uiState.update {
                            state.copy(phase = TabataPhase.ROUND_REST, secondsRemaining = config.restBetweenCyclesSeconds)
                        }
                    }
                }
            }
            TabataPhase.REST -> {
                val nextIndex = state.currentExerciseIndex + 1
                _uiState.update {
                    state.copy(
                        phase = TabataPhase.WORK,
                        currentExerciseIndex = nextIndex,
                        secondsRemaining = config.workSeconds,
                        currentExerciseName = state.exercises[nextIndex],
                    )
                }
            }
            TabataPhase.ROUND_REST -> {
                val roundCompletesCycle = state.currentRound >= config.roundsPerCycle
                _uiState.update {
                    state.copy(
                        phase = TabataPhase.WORK,
                        currentCycle = if (roundCompletesCycle) state.currentCycle + 1 else state.currentCycle,
                        currentRound = if (roundCompletesCycle) 1 else state.currentRound + 1,
                        currentExerciseIndex = 0,
                        secondsRemaining = config.workSeconds,
                        currentExerciseName = state.exercises[0],
                    )
                }
            }
            TabataPhase.DONE -> Unit
        }
    }

    private fun playCountdownTick() {
        val generator = toneGenerator ?: ToneGenerator(AudioManager.STREAM_MUSIC, 100).also { toneGenerator = it }
        generator.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
    }

    private fun playIntervalFinishedSound() {
        val generator = toneGenerator ?: ToneGenerator(AudioManager.STREAM_MUSIC, 100).also { toneGenerator = it }
        generator.startTone(ToneGenerator.TONE_PROP_BEEP2, 400)
    }

    private fun finishTabata(state: ActiveSessionUiState.TabataSession) {
        tickerJob?.cancel()
        _uiState.update { state.copy(phase = TabataPhase.DONE, isRunning = false, secondsRemaining = 0) }
        saveSession(
            SessionResult.TabataResult(
                roundsCompleted = tabataRoundsCompleted,
                cyclesCompleted = tabataCyclesCompleted,
            ),
        )
        markFinished()
    }

    // ---- Density timer ----

    fun toggleDensityRunning() {
        val state = _uiState.value as? ActiveSessionUiState.DensitySession ?: return
        if (state.isRunning) {
            tickerJob?.cancel()
            _uiState.update { (it as ActiveSessionUiState.DensitySession).copy(isRunning = false) }
        } else {
            _uiState.update { (it as ActiveSessionUiState.DensitySession).copy(isRunning = true) }
            tickerJob = viewModelScope.launch {
                while (true) {
                    delay(1_000)
                    densityElapsed++
                    _uiState.update { (it as ActiveSessionUiState.DensitySession).copy(elapsedSeconds = densityElapsed) }
                }
            }
        }
    }

    fun incrementDensityRounds() {
        _uiState.update {
            val state = it as? ActiveSessionUiState.DensitySession ?: return@update it
            state.copy(roundsCompleted = state.roundsCompleted + 1)
        }
    }

    fun finishDensity() {
        val state = _uiState.value as? ActiveSessionUiState.DensitySession ?: return
        tickerJob?.cancel()
        _uiState.update { state.copy(isRunning = false, isFinished = true) }
        saveSession(SessionResult.DensityResult(roundsCompleted = state.roundsCompleted, elapsedSeconds = densityElapsed))
        markFinished()
    }

    // ---- Set-logging styles (GTG, Pyramid, Step-Loading) ----

    fun updateRepsInput(value: String) {
        _uiState.update { (it as? ActiveSessionUiState.SetLoggingSession)?.copy(repsInput = value) ?: it }
    }

    fun updateWeightInput(value: String) {
        _uiState.update { (it as? ActiveSessionUiState.SetLoggingSession)?.copy(weightInput = value) ?: it }
    }

    fun logSet() {
        val state = _uiState.value as? ActiveSessionUiState.SetLoggingSession ?: return
        val nextIndex = state.loggedSets.size
        val newSet = PerformedSet(
            exerciseName = state.exerciseName,
            setIndex = nextIndex,
            reps = state.repsInput.toIntOrNull(),
            weightKg = state.weightInput.toDoubleOrNull(),
        )
        val loggedSets = state.loggedSets + newSet
        val nextTarget = nextSetTarget(loggedSets.size)
        _uiState.update {
            state.copy(
                loggedSets = loggedSets,
                nextSetTargetReps = nextTarget.first,
                nextSetTargetWeightKg = nextTarget.second,
                repsInput = nextTarget.first?.toString() ?: state.repsInput,
                weightInput = nextTarget.second?.toString() ?: state.weightInput,
            )
        }
    }

    private fun nextSetTarget(completedSets: Int): Pair<Int?, Double?> = when (val config = workout.styleConfig) {
        is WorkoutStyleConfig.GreaseTheGroove -> config.repsPerSet to null
        is WorkoutStyleConfig.Pyramid -> config.repScheme().getOrNull(completedSets) to null
        is WorkoutStyleConfig.StepLoading -> config.repsPerSet to config.weightForSet(completedSets)
        else -> null to null
    }

    fun finishSetLoggingSession() {
        val state = _uiState.value as? ActiveSessionUiState.SetLoggingSession ?: return
        _uiState.update { state.copy(isFinished = true) }
        val result: SessionResult = when (state.style) {
            WorkoutStyle.GREASE_THE_GROOVE -> SessionResult.GreaseTheGrooveResult(state.loggedSets)
            WorkoutStyle.PYRAMID -> SessionResult.PyramidResult(state.loggedSets)
            WorkoutStyle.STEP_LOADING -> SessionResult.StepLoadingResult(state.loggedSets)
            else -> SessionResult.GreaseTheGrooveResult(state.loggedSets)
        }
        saveSession(result)
        markFinished()
    }

    // ---- Shared ----

    private var finished = false

    private fun markFinished() {
        finished = true
    }

    val isSessionFinished: Boolean get() = finished

    private fun saveSession(result: SessionResult) {
        viewModelScope.launch {
            val profileId = profileRepository.currentProfileId.filterNotNull().first()
            sessionRepository.startSession(
                WorkoutSession(
                    profileId = profileId,
                    workoutId = workout.id,
                    workoutName = workout.name,
                    style = workout.style,
                    startedAt = startedAt,
                    completedAt = Clock.System.now(),
                    result = result,
                ),
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
        toneGenerator?.release()
    }

    private companion object {
        const val COUNTDOWN_TICK_SECONDS = 10
    }
}
