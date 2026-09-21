package com.scttech.android.kotlin.openfitness.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/** What a [Program]'s periodic test measures. */
enum class ProgramGoalType(val unitLabel: String) {
    REPS("reps"),
    DURATION_SECONDS("seconds"),
    DISTANCE_METERS("meters"),
}

/** How a [Program]'s periodic test result turns into its next block of training sets. */
enum class RepStrategy(val displayName: String, val shortDescription: String) {
    PERCENTAGE_LADDER(
        displayName = "Percentage ladder",
        shortDescription = "One set per rung, climbing from 50% to 90% of your training max.",
    ),
    STRAIGHT_SETS(
        displayName = "Straight sets",
        shortDescription = "The same target across a fixed number of sets, e.g. 5 sets at 70%.",
    ),
    AMRAP_FINISHER(
        displayName = "AMRAP finisher",
        shortDescription = "A few fixed-target sets, then a final set pushed to failure.",
    ),
    CUSTOM_MANUAL(
        displayName = "Custom sets",
        shortDescription = "You set each set's target by hand, ignoring the test result.",
    ),
}

/**
 * The structural parameters that make each [RepStrategy] distinct. Stored as part of
 * [ProgramConfig]'s JSON column rather than separate table columns, since it's never queried
 * relationally - only ever read/written as a whole alongside its parent program.
 */
@Serializable
sealed interface RepStrategyConfig {

    val strategy: RepStrategy

    @Serializable
    data class PercentageLadder(val percentages: List<Double> = listOf(0.5, 0.6, 0.7, 0.8, 0.9)) : RepStrategyConfig {
        override val strategy get() = RepStrategy.PERCENTAGE_LADDER
    }

    @Serializable
    data class StraightSets(val setCount: Int = 5, val percentage: Double = 0.7) : RepStrategyConfig {
        override val strategy get() = RepStrategy.STRAIGHT_SETS
    }

    @Serializable
    data class AmrapFinisher(val fixedSetCount: Int = 3, val fixedPercentage: Double = 0.8) : RepStrategyConfig {
        override val strategy get() = RepStrategy.AMRAP_FINISHER
    }

    @Serializable
    data class CustomManual(val setTargets: List<Int> = emptyList()) : RepStrategyConfig {
        override val strategy get() = RepStrategy.CUSTOM_MANUAL
    }

    companion object {
        fun default(strategy: RepStrategy): RepStrategyConfig = when (strategy) {
            RepStrategy.PERCENTAGE_LADDER -> PercentageLadder()
            RepStrategy.STRAIGHT_SETS -> StraightSets()
            RepStrategy.AMRAP_FINISHER -> AmrapFinisher()
            RepStrategy.CUSTOM_MANUAL -> CustomManual()
        }
    }
}

/**
 * User-adjustable parameters for how a test result turns into a prescription. See
 * [com.scttech.android.kotlin.openfitness.domain.program.ProgramProgression].
 */
@Serializable
data class ProgramConfig(
    val repStrategyConfig: RepStrategyConfig = RepStrategyConfig.PercentageLadder(),
    val trainingMaxFactor: Double = 0.9,
    val sessionsPerWeek: Int = 3,
    val retestIntervalDays: Int = 7,
    /** Rest between sets during a session, counted down by a skippable timer. 0 disables it. */
    val restSeconds: Int = 90,
)

@Serializable
data class ProgramSet(val setIndex: Int, val targetValue: Int, val isAmrap: Boolean = false)

/** The training block currently prescribed, computed from the most recent [ProgramTest]. */
@Serializable
data class ProgramPrescription(
    val basedOnTestResult: Int,
    val trainingMax: Int,
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
    val goalTarget: Int,
    val config: ProgramConfig = ProgramConfig(),
    val currentPrescription: ProgramPrescription? = null,
    val lastTestResult: Int? = null,
    val lastTestedAt: Instant? = null,
    val nextTestDueAt: Instant? = null,
    val isArchived: Boolean = false,
    val createdAt: Instant,
)
