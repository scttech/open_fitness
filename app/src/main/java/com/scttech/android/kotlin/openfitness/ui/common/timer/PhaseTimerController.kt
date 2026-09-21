package com.scttech.android.kotlin.openfitness.ui.common.timer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Drives a sequence of countdown [TimerPhase]s one second at a time - pausable and skippable.
 * Shared by every workout/program screen that needs a phase-based work or rest timer, so pause,
 * skip, and sound/color hooks behave identically everywhere instead of being reimplemented per
 * session type.
 *
 * Owned by a ViewModel (constructed with its `viewModelScope`); call [stop] or let the scope be
 * cancelled to tear down the ticking job.
 */
class PhaseTimerController(
    private val scope: CoroutineScope,
    /** Fires every second while running, with the phase's own updated remaining time. */
    private val onTick: (secondsRemaining: Int, phase: TimerPhase) -> Unit = { _, _ -> },
    /** Fires when a phase's countdown reaches zero naturally (not on [skip]) - e.g. to play a sound. */
    private val onPhaseComplete: (phase: TimerPhase) -> Unit = {},
    /** Fires once the last phase finishes, naturally or via [skip]. */
    private val onAllPhasesComplete: () -> Unit = {},
) {
    private val _state = MutableStateFlow(PhaseTimerState())
    val state: StateFlow<PhaseTimerState> = _state.asStateFlow()

    private var job: Job? = null

    /** Begins running [phases] from the first one. Replaces anything already in progress. */
    fun start(phases: List<TimerPhase>) {
        job?.cancel()
        if (phases.isEmpty()) {
            _state.value = PhaseTimerState(isFinished = true, hasStarted = true)
            onAllPhasesComplete()
            return
        }
        _state.value = PhaseTimerState(
            phases = phases,
            phaseIndex = 0,
            secondsRemaining = phases[0].seconds,
            isRunning = true,
            hasStarted = true,
        )
        runTicker()
    }

    fun pause() {
        job?.cancel()
        _state.update { if (it.isFinished) it else it.copy(isRunning = false) }
    }

    fun resume() {
        val current = _state.value
        if (current.isFinished || current.phases.isEmpty()) return
        _state.update { it.copy(isRunning = true) }
        runTicker()
    }

    /** Jumps straight to the next phase (or finishes, if this was the last one) without a sound. */
    fun skip() {
        job?.cancel()
        val current = _state.value
        if (current.phases.isEmpty() || current.isFinished) return
        if (advance(current, natural = false)) runTicker()
    }

    fun stop() {
        job?.cancel()
        _state.value = PhaseTimerState()
    }

    private fun runTicker() {
        job = scope.launch {
            while (true) {
                delay(1_000)
                val current = _state.value
                if (!current.isRunning) return@launch
                val remaining = current.secondsRemaining - 1
                if (remaining <= 0) {
                    if (!advance(current, natural = true)) return@launch
                } else {
                    _state.update { it.copy(secondsRemaining = remaining) }
                    current.currentPhase?.let { onTick(remaining, it) }
                }
            }
        }
    }

    /** Moves from [current] to the next phase (or marks finished). Returns false once finished. */
    private fun advance(current: PhaseTimerState, natural: Boolean): Boolean {
        val finishedPhase = current.currentPhase
        if (finishedPhase != null && natural) onPhaseComplete(finishedPhase)
        // Zero-length phases (e.g. rest set to 0) are never actually shown - skip straight past them.
        var nextIndex = current.phaseIndex + 1
        while (nextIndex < current.phases.size && current.phases[nextIndex].seconds <= 0) {
            nextIndex++
        }
        if (nextIndex >= current.phases.size) {
            _state.update { it.copy(isRunning = false, isFinished = true) }
            onAllPhasesComplete()
            return false
        }
        val nextPhase = current.phases[nextIndex]
        _state.value = current.copy(phaseIndex = nextIndex, secondsRemaining = nextPhase.seconds, isRunning = true)
        return true
    }
}
