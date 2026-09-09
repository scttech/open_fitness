package com.scttech.android.kotlin.openfitness.ui.session

import com.scttech.android.kotlin.openfitness.domain.model.PerformedSet
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutStyle
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutStyleConfig

enum class TabataPhase { WORK, REST, ROUND_REST, DONE }

sealed interface ActiveSessionUiState {

    data object Loading : ActiveSessionUiState

    data class TabataSession(
        val workoutName: String,
        val config: WorkoutStyleConfig.Tabata,
        val exercises: List<String>,
        val phase: TabataPhase,
        val currentCycle: Int,
        val currentRound: Int,
        val currentExerciseIndex: Int,
        val secondsRemaining: Int,
        val currentExerciseName: String,
        val isRunning: Boolean,
        val isFinished: Boolean,
    ) : ActiveSessionUiState

    /** Used for the three set-by-set logging styles: Grease the Groove, Pyramid, Step-Loading. */
    data class SetLoggingSession(
        val workoutName: String,
        val style: WorkoutStyle,
        val exerciseName: String,
        val targetDescription: String,
        val nextSetTargetReps: Int?,
        val nextSetTargetWeightKg: Double?,
        val loggedSets: List<PerformedSet>,
        val repsInput: String,
        val weightInput: String,
        val isFinished: Boolean,
    ) : ActiveSessionUiState

    data class DensitySession(
        val workoutName: String,
        val config: WorkoutStyleConfig.Density,
        val exercises: List<String>,
        val elapsedSeconds: Int,
        val roundsCompleted: Int,
        val isRunning: Boolean,
        val isFinished: Boolean,
    ) : ActiveSessionUiState
}
