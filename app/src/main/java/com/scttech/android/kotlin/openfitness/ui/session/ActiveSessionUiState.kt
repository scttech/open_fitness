package com.scttech.android.kotlin.openfitness.ui.session

import com.scttech.android.kotlin.openfitness.domain.model.PerformedSet
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutExercise
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutStyle
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutStyleConfig
import com.scttech.android.kotlin.openfitness.ui.common.timer.PhaseTimerState

sealed interface ActiveSessionUiState {

    data object Loading : ActiveSessionUiState

    data class TabataSession(
        val workoutName: String,
        val timerState: PhaseTimerState,
        val isFinished: Boolean,
        /** Set once [isFinished], so the screen can send the user off on a high note. */
        val completionMessage: String? = null,
    ) : ActiveSessionUiState

    /** Used for the three set-by-set logging styles: Grease the Groove, Pyramid, Step-Loading. */
    data class SetLoggingSession(
        val workoutName: String,
        val style: WorkoutStyle,
        val exerciseName: String,
        val exerciseId: Long?,
        val targetDescription: String,
        val nextSetTargetReps: Int?,
        val nextSetTargetWeightKg: Double?,
        val loggedSets: List<PerformedSet>,
        val repsInput: String,
        val weightInput: String,
        val isFinished: Boolean,
        /** Set once [isFinished], so the screen can send the user off on a high note. */
        val completionMessage: String? = null,
        /** Null for Grease the Groove, which has no fixed session-length - only a daily target. */
        val totalSets: Int? = null,
        /** Non-null while resting between sets; the rest duration behind it lives in [WorkoutStyleConfig]. */
        val restTimerState: PhaseTimerState? = null,
    ) : ActiveSessionUiState

    data class DensitySession(
        val workoutName: String,
        val config: WorkoutStyleConfig.Density,
        val exercises: List<WorkoutExercise>,
        val elapsedSeconds: Int,
        val roundsCompleted: Int,
        val isRunning: Boolean,
        val isFinished: Boolean,
        /** Set once [isFinished], so the screen can send the user off on a high note. */
        val completionMessage: String? = null,
    ) : ActiveSessionUiState

    data class EmomSession(
        val workoutName: String,
        val config: WorkoutStyleConfig.Emom,
        val timerState: PhaseTimerState,
        val isFinished: Boolean,
        /** Set once [isFinished], so the screen can send the user off on a high note. */
        val completionMessage: String? = null,
    ) : ActiveSessionUiState
}
