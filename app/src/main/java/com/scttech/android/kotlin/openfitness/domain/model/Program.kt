package com.scttech.android.kotlin.openfitness.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/** What a [Program]'s periodic test measures. */
enum class ProgramGoalType(val unitLabel: String) {
    REPS("reps"),
    DURATION_SECONDS("seconds"),
    DISTANCE_METERS("meters"),
}

/**
 * User-adjustable parameters for how a test result turns into a prescription. See
 * [com.scttech.android.kotlin.openfitness.domain.program.ProgramProgression].
 */
@Serializable
data class ProgramConfig(
    val percentageLadder: List<Double> = listOf(0.5, 0.6, 0.7, 0.8, 0.9),
    val trainingMaxFactor: Double = 0.9,
    val sessionsPerWeek: Int = 3,
    val retestIntervalDays: Int = 14,
)

@Serializable
data class ProgramSet(val setIndex: Int, val targetValue: Int)

/** The training block currently prescribed, computed from the most recent [ProgramTest]. */
@Serializable
data class ProgramPrescription(
    val basedOnTestResult: Double,
    val trainingMax: Double,
    val sets: List<ProgramSet>,
)

/**
 * Goal-driven training toward a target (e.g. "100 push-ups"). Unlike a [Workout], a Program has
 * no fixed rep scheme up front - the user periodically performs a max-effort [ProgramTest], and
 * the app recalculates [currentPrescription] from that result.
 */
data class Program(
    val id: Long = 0L,
    val profileId: Long,
    val name: String,
    val exerciseId: Long?,
    val exerciseName: String,
    val goalType: ProgramGoalType,
    val goalTarget: Double,
    val config: ProgramConfig = ProgramConfig(),
    val currentPrescription: ProgramPrescription? = null,
    val lastTestResult: Double? = null,
    val lastTestedAt: Instant? = null,
    val nextTestDueAt: Instant? = null,
    val isArchived: Boolean = false,
    val createdAt: Instant,
)
