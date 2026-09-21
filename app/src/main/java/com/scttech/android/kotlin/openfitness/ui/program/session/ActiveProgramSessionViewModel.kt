package com.scttech.android.kotlin.openfitness.ui.program.session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.scttech.android.kotlin.openfitness.data.repository.ProfileRepository
import com.scttech.android.kotlin.openfitness.data.repository.ProgramRepository
import com.scttech.android.kotlin.openfitness.data.repository.ProgramSessionRepository
import com.scttech.android.kotlin.openfitness.domain.model.PerformedSet
import com.scttech.android.kotlin.openfitness.domain.model.Program
import com.scttech.android.kotlin.openfitness.domain.model.ProgramSession
import com.scttech.android.kotlin.openfitness.ui.common.MotivationalMessages
import com.scttech.android.kotlin.openfitness.ui.common.timer.PhaseTimerController
import com.scttech.android.kotlin.openfitness.ui.common.timer.TimerColorPrefs
import com.scttech.android.kotlin.openfitness.ui.common.timer.TimerPhase
import com.scttech.android.kotlin.openfitness.ui.common.timer.TimerPhaseKind
import com.scttech.android.kotlin.openfitness.ui.common.timer.TimerSoundPlayer
import com.scttech.android.kotlin.openfitness.ui.common.timer.timerColorPrefs
import com.scttech.android.kotlin.openfitness.ui.navigation.ActiveProgramSessionRoute
import dagger.hilt.android.lifecycle.HiltViewModel
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
class ActiveProgramSessionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val programRepository: ProgramRepository,
    private val programSessionRepository: ProgramSessionRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val route: ActiveProgramSessionRoute = savedStateHandle.toRoute()
    private val startedAt = Clock.System.now()
    private lateinit var program: Program

    private val _uiState = MutableStateFlow<ActiveProgramSessionUiState>(ActiveProgramSessionUiState.Loading)
    val uiState: StateFlow<ActiveProgramSessionUiState> = _uiState.asStateFlow()

    val timerColors: StateFlow<TimerColorPrefs> = profileRepository.observeCurrentProfile()
        .map { it.timerColorPrefs() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TimerColorPrefs())

    private var soundEnabled = true
    private val soundPlayer = TimerSoundPlayer()
    private val restTimer = PhaseTimerController(
        scope = viewModelScope,
        onTick = { remaining, _ -> if (soundEnabled && remaining in 1..COUNTDOWN_TICK_SECONDS) soundPlayer.playTick() },
        onPhaseComplete = { if (soundEnabled) soundPlayer.playPhaseComplete() },
    )

    init {
        viewModelScope.launch {
            profileRepository.observeCurrentProfile().collect { soundEnabled = it?.timerSoundEnabled ?: true }
        }
        viewModelScope.launch {
            val loaded = programRepository.observeProgram(route.programId).first() ?: return@launch
            program = loaded
            _uiState.value = ActiveProgramSessionUiState.InProgress(
                program = loaded,
                loggedSets = emptyList(),
                valueInput = loaded.currentPrescription?.sets?.firstOrNull()?.targetValue?.toString().orEmpty(),
            )
        }
        viewModelScope.launch {
            restTimer.state.collect { timerState ->
                _uiState.update { current ->
                    (current as? ActiveProgramSessionUiState.InProgress)?.copy(
                        restTimerState = timerState.takeIf { it.phases.isNotEmpty() && !it.isFinished },
                    ) ?: current
                }
            }
        }
    }

    fun updateValueInput(value: String) {
        _uiState.update { (it as? ActiveProgramSessionUiState.InProgress)?.copy(valueInput = value) ?: it }
    }

    fun logSet() {
        val state = _uiState.value as? ActiveProgramSessionUiState.InProgress ?: return
        val newSet = PerformedSet(
            exerciseName = state.program.exerciseName,
            setIndex = state.loggedSets.size,
            reps = state.valueInput.toIntOrNull(),
        )
        val loggedSets = state.loggedSets + newSet
        val totalSets = state.totalSets
        if (totalSets != null && loggedSets.size >= totalSets) {
            completeSession(state.copy(loggedSets = loggedSets))
        } else {
            val nextTarget = state.program.currentPrescription?.sets?.getOrNull(loggedSets.size)?.targetValue
            _uiState.update {
                state.copy(
                    loggedSets = loggedSets,
                    valueInput = nextTarget?.toString() ?: state.valueInput,
                )
            }
            val restSeconds = state.program.config.restSeconds
            if (restSeconds > 0) {
                restTimer.start(listOf(TimerPhase(TimerPhaseKind.REST, "Rest", restSeconds)))
            }
        }
    }

    fun skipRest() = restTimer.skip()

    fun toggleRestRunning() {
        if (restTimer.state.value.isRunning) restTimer.pause() else restTimer.resume()
    }

    fun finishSession() {
        val state = _uiState.value as? ActiveProgramSessionUiState.InProgress ?: return
        restTimer.stop()
        completeSession(state)
    }

    private fun completeSession(state: ActiveProgramSessionUiState.InProgress) {
        _uiState.update {
            state.copy(isFinished = true, completionMessage = MotivationalMessages.random(), restTimerState = null)
        }
        viewModelScope.launch {
            val profileId = profileRepository.currentProfileId.filterNotNull().first()
            programSessionRepository.startSession(
                ProgramSession(
                    profileId = profileId,
                    programId = state.program.id,
                    programName = state.program.name,
                    startedAt = startedAt,
                    completedAt = Clock.System.now(),
                    loggedSets = state.loggedSets,
                ),
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        restTimer.stop()
        soundPlayer.release()
    }

    private companion object {
        const val COUNTDOWN_TICK_SECONDS = 10
    }
}
