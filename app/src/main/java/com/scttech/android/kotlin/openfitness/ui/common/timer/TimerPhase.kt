package com.scttech.android.kotlin.openfitness.ui.common.timer

/** Which color role a phase should render in - resolved to an actual color by the caller. */
enum class TimerPhaseKind { WORK, REST }

/** One countdown segment of a workout/program timer, e.g. a Tabata work interval or a rest. */
data class TimerPhase(
    val kind: TimerPhaseKind,
    val label: String,
    val seconds: Int,
    /** Secondary context shown under the label, e.g. "Round 2/4 · Cycle 1/2". */
    val subtitle: String? = null,
    /** Links a WORK phase's [label] into the exercise library, so it can be tapped for info. */
    val exerciseId: Long? = null,
)

/** Snapshot of a [PhaseTimerController] - which phase is active, and how much of it is left. */
data class PhaseTimerState(
    val phases: List<TimerPhase> = emptyList(),
    val phaseIndex: Int = 0,
    val secondsRemaining: Int = 0,
    val isRunning: Boolean = false,
    val isFinished: Boolean = false,
    /** True from the first [PhaseTimerController.start] call onward - distinguishes "never run yet" (show "Start") from "paused" (show "Resume"). */
    val hasStarted: Boolean = false,
) {
    val currentPhase: TimerPhase? get() = phases.getOrNull(phaseIndex)

    /** The phase that will play after [currentPhase] - skips zero-length ones, like [PhaseTimerController.advance] does. Null past the last phase. */
    val nextPhase: TimerPhase?
        get() {
            var index = phaseIndex + 1
            while (index < phases.size && phases[index].seconds <= 0) index++
            return phases.getOrNull(index)
        }

    val progress: Float
        get() {
            val total = currentPhase?.seconds ?: return 0f
            return if (total > 0) secondsRemaining / total.toFloat() else 0f
        }
}
