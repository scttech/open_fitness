package com.scttech.android.kotlin.openfitness.ui.session

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
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutExercise
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutSession
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutStyle
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutStyleConfig
import com.scttech.android.kotlin.openfitness.ui.common.MotivationalMessages
import com.scttech.android.kotlin.openfitness.ui.common.timer.PhaseTimerController
import com.scttech.android.kotlin.openfitness.ui.common.timer.PhaseTimerState
import com.scttech.android.kotlin.openfitness.ui.common.timer.TimerColorPrefs
import com.scttech.android.kotlin.openfitness.ui.common.timer.TimerPhase
import com.scttech.android.kotlin.openfitness.ui.common.timer.TimerPhaseKind
import com.scttech.android.kotlin.openfitness.ui.common.timer.TimerSoundPlayer
import com.scttech.android.kotlin.openfitness.ui.common.timer.timerColorPrefs
import com.scttech.android.kotlin.openfitness.ui.navigation.ActiveSessionRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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

    val timerColors: StateFlow<TimerColorPrefs> = profileRepository.observeCurrentProfile()
        .map { it.timerColorPrefs() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TimerColorPrefs())

    private lateinit var workout: Workout
    private val startedAt = Clock.System.now()

    // Density-only running counter/ticker - a free-running stopwatch, not a phase timer.
    private var densityElapsed = 0
    private var densityTickerJob: Job? = null

    private var tabataStarted = false
    private var emomStarted = false
    private val soundPlayer = TimerSoundPlayer()
    private var soundEnabled = true

    private val tabataTimer = PhaseTimerController(
        scope = viewModelScope,
        onTick = { remaining, _ -> onCountdownTick(remaining) },
        onPhaseComplete = { playPhaseCompleteSound() },
        onAllPhasesComplete = ::finishTabata,
    )
    private val emomTimer = PhaseTimerController(
        scope = viewModelScope,
        onTick = { remaining, _ -> onCountdownTick(remaining) },
        onPhaseComplete = { playPhaseCompleteSound() },
        onAllPhasesComplete = ::finishEmom,
    )
    private val restTimer = PhaseTimerController(
        scope = viewModelScope,
        onTick = { remaining, _ -> onCountdownTick(remaining) },
        onPhaseComplete = { playPhaseCompleteSound() },
    )

    init {
        viewModelScope.launch {
            profileRepository.observeCurrentProfile().collect { soundEnabled = it?.timerSoundEnabled ?: true }
        }
        viewModelScope.launch {
            val loaded = workoutRepository.observeWorkout(route.workoutId).first() ?: return@launch
            workout = loaded
            _uiState.value = initialStateFor(loaded)
        }
        viewModelScope.launch {
            tabataTimer.state.collect { timerState ->
                _uiState.update { current ->
                    (current as? ActiveSessionUiState.TabataSession)?.copy(timerState = timerState) ?: current
                }
            }
        }
        viewModelScope.launch {
            emomTimer.state.collect { timerState ->
                _uiState.update { current ->
                    (current as? ActiveSessionUiState.EmomSession)?.copy(timerState = timerState) ?: current
                }
            }
        }
        viewModelScope.launch {
            restTimer.state.collect { timerState ->
                _uiState.update { current ->
                    (current as? ActiveSessionUiState.SetLoggingSession)?.copy(
                        restTimerState = timerState.takeIf { it.phases.isNotEmpty() && !it.isFinished },
                    ) ?: current
                }
            }
        }
    }

    private fun initialStateFor(workout: Workout): ActiveSessionUiState = when (val config = workout.styleConfig) {
        is WorkoutStyleConfig.Tabata -> {
            val exercises = workout.exercises.sortedBy { it.order }.ifEmpty { listOf(WorkoutExercise(order = 0, name = workout.name)) }
            val phases = buildTabataPhases(config, exercises)
            ActiveSessionUiState.TabataSession(
                workoutName = workout.name,
                timerState = PhaseTimerState(phases = phases, secondsRemaining = phases.firstOrNull()?.seconds ?: 0),
                isFinished = false,
            )
        }
        is WorkoutStyleConfig.Density -> ActiveSessionUiState.DensitySession(
            workoutName = workout.name,
            config = config,
            exercises = workout.exercises.sortedBy { it.order },
            elapsedSeconds = 0,
            roundsCompleted = 0,
            isRunning = false,
            isFinished = false,
        )
        is WorkoutStyleConfig.GreaseTheGroove -> ActiveSessionUiState.SetLoggingSession(
            workoutName = workout.name,
            style = WorkoutStyle.GREASE_THE_GROOVE,
            exerciseName = config.exerciseName,
            exerciseId = workout.exercises.firstOrNull()?.exerciseId,
            targetDescription = "${config.repsPerSet} reps, target ${config.targetSetsPerDay} sets today",
            nextSetTargetReps = config.repsPerSet,
            nextSetTargetWeightKg = null,
            loggedSets = emptyList(),
            repsInput = config.repsPerSet.toString(),
            weightInput = "",
            isFinished = false,
            totalSets = null,
        )
        is WorkoutStyleConfig.Pyramid -> {
            val scheme = config.repScheme()
            ActiveSessionUiState.SetLoggingSession(
                workoutName = workout.name,
                style = WorkoutStyle.PYRAMID,
                exerciseName = workout.exercises.firstOrNull()?.name ?: workout.name,
                exerciseId = workout.exercises.firstOrNull()?.exerciseId,
                targetDescription = "Scheme: ${scheme.joinToString("-")} reps",
                nextSetTargetReps = scheme.firstOrNull(),
                nextSetTargetWeightKg = null,
                loggedSets = emptyList(),
                repsInput = scheme.firstOrNull()?.toString().orEmpty(),
                weightInput = "",
                isFinished = false,
                totalSets = scheme.size,
            )
        }
        is WorkoutStyleConfig.StepLoading -> ActiveSessionUiState.SetLoggingSession(
            workoutName = workout.name,
            style = WorkoutStyle.STEP_LOADING,
            exerciseName = workout.exercises.firstOrNull()?.name ?: workout.name,
            exerciseId = workout.exercises.firstOrNull()?.exerciseId,
            targetDescription = "${config.setCount} sets × ${config.repsPerSet} reps",
            nextSetTargetReps = config.repsPerSet,
            nextSetTargetWeightKg = config.weightForSet(0),
            loggedSets = emptyList(),
            repsInput = config.repsPerSet.toString(),
            weightInput = config.weightForSet(0).toString(),
            isFinished = false,
            totalSets = config.setCount,
        )
        is WorkoutStyleConfig.Emom -> {
            val exercises = workout.exercises.sortedBy { it.order }.ifEmpty { listOf(WorkoutExercise(order = 0, name = workout.name)) }
            val phases = buildEmomPhases(config, exercises)
            ActiveSessionUiState.EmomSession(
                workoutName = workout.name,
                config = config,
                timerState = PhaseTimerState(phases = phases, secondsRemaining = phases.firstOrNull()?.seconds ?: 0),
                isFinished = false,
            )
        }
    }

    // ---- Tabata timer ----

    private fun buildTabataPhases(config: WorkoutStyleConfig.Tabata, exercises: List<WorkoutExercise>): List<TimerPhase> {
        val phases = mutableListOf<TimerPhase>()
        for (cycle in 1..config.cycles) {
            for (round in 1..config.roundsPerCycle) {
                val roundLabel = "Round $round/${config.roundsPerCycle}" +
                    if (config.cycles > 1) " · Cycle $cycle/${config.cycles}" else ""
                exercises.forEachIndexed { index, exercise ->
                    phases += TimerPhase(TimerPhaseKind.WORK, exercise.name, config.workSeconds, roundLabel, exercise.exerciseId)
                    if (index < exercises.lastIndex) {
                        phases += TimerPhase(TimerPhaseKind.REST, "Rest", config.restSeconds, roundLabel)
                    }
                }
                val isLastRoundOfLastCycle = cycle == config.cycles && round == config.roundsPerCycle
                if (!isLastRoundOfLastCycle) {
                    phases += TimerPhase(TimerPhaseKind.REST, "Round rest", config.restBetweenCyclesSeconds, roundLabel)
                }
            }
        }
        return phases
    }

    fun toggleTabataRunning() {
        val state = _uiState.value as? ActiveSessionUiState.TabataSession ?: return
        when {
            !tabataStarted -> {
                tabataStarted = true
                tabataTimer.start(state.timerState.phases)
            }
            state.timerState.isRunning -> tabataTimer.pause()
            else -> tabataTimer.resume()
        }
    }

    fun skipTabataPhase() = tabataTimer.skip()

    private fun finishTabata() {
        if (!::workout.isInitialized) return
        _uiState.update {
            (it as? ActiveSessionUiState.TabataSession)
                ?.copy(isFinished = true, completionMessage = MotivationalMessages.random())
                ?: it
        }
        val config = workout.styleConfig as? WorkoutStyleConfig.Tabata ?: return
        saveSession(
            SessionResult.TabataResult(
                roundsCompleted = config.roundsPerCycle * config.cycles,
                cyclesCompleted = config.cycles,
            ),
        )
        markFinished()
    }

    // ---- EMOM timer ----

    private fun buildEmomPhases(config: WorkoutStyleConfig.Emom, exercises: List<WorkoutExercise>): List<TimerPhase> {
        val phases = mutableListOf<TimerPhase>()
        for (round in 1..config.rounds) {
            val roundLabel = "Round $round/${config.rounds} · Goal: ${config.repGoal} reps"
            exercises.forEach { exercise ->
                phases += TimerPhase(TimerPhaseKind.WORK, exercise.name, EMOM_MINUTE_SECONDS, roundLabel, exercise.exerciseId)
            }
            if (round < config.rounds) {
                phases += TimerPhase(TimerPhaseKind.REST, "Rest", config.restBetweenRoundsSeconds, roundLabel)
            }
        }
        return phases
    }

    fun toggleEmomRunning() {
        val state = _uiState.value as? ActiveSessionUiState.EmomSession ?: return
        when {
            !emomStarted -> {
                emomStarted = true
                emomTimer.start(state.timerState.phases)
            }
            state.timerState.isRunning -> emomTimer.pause()
            else -> emomTimer.resume()
        }
    }

    fun skipEmomPhase() = emomTimer.skip()

    private fun finishEmom() {
        if (!::workout.isInitialized) return
        _uiState.update {
            (it as? ActiveSessionUiState.EmomSession)
                ?.copy(isFinished = true, completionMessage = MotivationalMessages.random())
                ?: it
        }
        val config = workout.styleConfig as? WorkoutStyleConfig.Emom ?: return
        saveSession(SessionResult.EmomResult(roundsCompleted = config.rounds))
        markFinished()
    }

    // ---- Shared countdown sound hooks (Tabata + EMOM + rest timers) ----

    private fun onCountdownTick(secondsRemaining: Int) {
        if (soundEnabled && secondsRemaining in 1..COUNTDOWN_TICK_SECONDS) soundPlayer.playTick()
    }

    private fun playPhaseCompleteSound() {
        if (soundEnabled) soundPlayer.playPhaseComplete()
    }

    // ---- Density timer (a free-running stopwatch, not phase-based) ----

    fun toggleDensityRunning() {
        val state = _uiState.value as? ActiveSessionUiState.DensitySession ?: return
        if (state.isRunning) {
            densityTickerJob?.cancel()
            _uiState.update { (it as ActiveSessionUiState.DensitySession).copy(isRunning = false) }
        } else {
            _uiState.update { (it as ActiveSessionUiState.DensitySession).copy(isRunning = true) }
            densityTickerJob = viewModelScope.launch {
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
        densityTickerJob?.cancel()
        _uiState.update {
            state.copy(isRunning = false, isFinished = true, completionMessage = MotivationalMessages.random())
        }
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
        val totalSets = state.totalSets
        if (totalSets != null && loggedSets.size >= totalSets) {
            completeSetLogging(state.copy(loggedSets = loggedSets))
        } else {
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
            val restSeconds = restSecondsFor(workout.styleConfig)
            if (restSeconds > 0) {
                restTimer.start(listOf(TimerPhase(TimerPhaseKind.REST, "Rest", restSeconds)))
            }
        }
    }

    fun skipRest() = restTimer.skip()

    fun toggleRestRunning() {
        if (restTimer.state.value.isRunning) restTimer.pause() else restTimer.resume()
    }

    private fun restSecondsFor(config: WorkoutStyleConfig): Int = when (config) {
        is WorkoutStyleConfig.GreaseTheGroove -> config.minRestMinutesBetweenSets * 60
        is WorkoutStyleConfig.Pyramid -> config.restSeconds
        is WorkoutStyleConfig.StepLoading -> config.restSeconds
        else -> 0
    }

    private fun nextSetTarget(completedSets: Int): Pair<Int?, Double?> = when (val config = workout.styleConfig) {
        is WorkoutStyleConfig.GreaseTheGroove -> config.repsPerSet to null
        is WorkoutStyleConfig.Pyramid -> config.repScheme().getOrNull(completedSets) to null
        is WorkoutStyleConfig.StepLoading -> config.repsPerSet to config.weightForSet(completedSets)
        else -> null to null
    }

    fun finishSetLoggingSession() {
        val state = _uiState.value as? ActiveSessionUiState.SetLoggingSession ?: return
        completeSetLogging(state)
    }

    private fun completeSetLogging(state: ActiveSessionUiState.SetLoggingSession) {
        restTimer.stop()
        _uiState.update { state.copy(isFinished = true, completionMessage = MotivationalMessages.random(), restTimerState = null) }
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
        densityTickerJob?.cancel()
        tabataTimer.stop()
        emomTimer.stop()
        restTimer.stop()
        soundPlayer.release()
    }

    private companion object {
        const val COUNTDOWN_TICK_SECONDS = 10
        const val EMOM_MINUTE_SECONDS = 60
    }
}
