package com.scttech.android.kotlin.openfitness.domain.model

import kotlinx.serialization.Serializable

/** A single as-performed set, logged during a session for the non-timer-driven styles. */
@Serializable
data class PerformedSet(
    val exerciseName: String,
    val setIndex: Int,
    val reps: Int? = null,
    val weightKg: Double? = null,
    val durationSeconds: Int? = null,
    val completed: Boolean = true,
)

/**
 * The as-performed outcome of a session, mirroring the shape of the workout's
 * [WorkoutStyleConfig]. Stored as a JSON column on the session entity for the same reasons as
 * [WorkoutStyleConfig].
 */
@Serializable
sealed interface SessionResult {

    @Serializable
    data class TabataResult(
        val roundsCompleted: Int,
        val cyclesCompleted: Int,
    ) : SessionResult

    @Serializable
    data class GreaseTheGrooveResult(
        val setsLogged: List<PerformedSet>,
    ) : SessionResult

    @Serializable
    data class PyramidResult(
        val setsLogged: List<PerformedSet>,
    ) : SessionResult

    @Serializable
    data class DensityResult(
        val roundsCompleted: Int,
        val elapsedSeconds: Int,
    ) : SessionResult

    @Serializable
    data class StepLoadingResult(
        val setsLogged: List<PerformedSet>,
    ) : SessionResult

    @Serializable
    data class EmomResult(
        val roundsCompleted: Int,
    ) : SessionResult
}
