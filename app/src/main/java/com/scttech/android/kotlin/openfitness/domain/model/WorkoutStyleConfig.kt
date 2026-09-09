package com.scttech.android.kotlin.openfitness.domain.model

import kotlinx.serialization.Serializable

/**
 * The structural parameters that make each training style distinct. Stored as a JSON column on
 * the workout entity (via a Room [androidx.room.TypeConverter]) rather than as separate join
 * tables, since this data is never queried relationally - only ever read/written as a whole
 * alongside its parent workout.
 */
@Serializable
sealed interface WorkoutStyleConfig {

    val style: WorkoutStyle

    @Serializable
    data class Tabata(
        val workSeconds: Int = 20,
        val restSeconds: Int = 10,
        val roundsPerCycle: Int = 8,
        val cycles: Int = 1,
        val restBetweenCyclesSeconds: Int = 60,
        /** Plays a beep when 10 seconds remain in a work interval or between-round rest. */
        val countdownSoundEnabled: Boolean = false,
    ) : WorkoutStyleConfig {
        override val style get() = WorkoutStyle.TABATA
    }

    @Serializable
    data class GreaseTheGroove(
        val exerciseName: String = "",
        val repsPerSet: Int = 5,
        val targetSetsPerDay: Int = 10,
        val minRestMinutesBetweenSets: Int = 30,
    ) : WorkoutStyleConfig {
        override val style get() = WorkoutStyle.GREASE_THE_GROOVE
        val dailyTargetReps: Int get() = repsPerSet * targetSetsPerDay
    }

    @Serializable
    data class Pyramid(
        val direction: PyramidDirection = PyramidDirection.UP_DOWN,
        val startReps: Int = 2,
        val stepReps: Int = 2,
        val peakReps: Int = 10,
    ) : WorkoutStyleConfig {
        override val style get() = WorkoutStyle.PYRAMID

        /** The full rep scheme this config produces, e.g. 2,4,6,8,10,8,6,4,2 for UP_DOWN. */
        fun repScheme(): List<Int> {
            val ascending = generateSequence(startReps) { it + stepReps }
                .takeWhile { it <= peakReps }
                .toList()
                .ifEmpty { listOf(startReps) }
            return when (direction) {
                PyramidDirection.UP -> ascending
                PyramidDirection.DOWN -> ascending.reversed()
                PyramidDirection.UP_DOWN -> ascending + ascending.reversed().drop(1)
            }
        }
    }

    @Serializable
    enum class PyramidDirection { UP, DOWN, UP_DOWN }

    @Serializable
    data class Density(
        val durationMinutes: Int = 15,
        val targetRounds: Int? = null,
    ) : WorkoutStyleConfig {
        override val style get() = WorkoutStyle.DENSITY
    }

    @Serializable
    data class StepLoading(
        val startWeightKg: Double = 20.0,
        val stepWeightKg: Double = 5.0,
        val setCount: Int = 5,
        val repsPerSet: Int = 5,
        val deloadEverySessions: Int? = null,
    ) : WorkoutStyleConfig {
        override val style get() = WorkoutStyle.STEP_LOADING

        fun weightForSet(setIndex: Int): Double = startWeightKg + stepWeightKg * setIndex
    }

    companion object {
        fun default(style: WorkoutStyle): WorkoutStyleConfig = when (style) {
            WorkoutStyle.TABATA -> Tabata()
            WorkoutStyle.GREASE_THE_GROOVE -> GreaseTheGroove()
            WorkoutStyle.PYRAMID -> Pyramid()
            WorkoutStyle.DENSITY -> Density()
            WorkoutStyle.STEP_LOADING -> StepLoading()
        }
    }
}
