package com.scttech.android.kotlin.openfitness.domain.model

import kotlinx.serialization.Serializable

/**
 * A single planned movement within a workout. Which of the optional target fields matter is
 * determined by the workout's [WorkoutStyleConfig] - e.g. a Tabata circuit exercise mostly cares
 * about [order] and [name] (the timer drives sets/duration), while a Step-Loading exercise cares
 * about [targetWeightKg] and [targetReps].
 */
@Serializable
data class WorkoutExercise(
    val order: Int,
    val name: String,
    val targetSets: Int? = null,
    val targetReps: Int? = null,
    val targetWeightKg: Double? = null,
    val targetDurationSeconds: Int? = null,
    val targetDistanceMeters: Double? = null,
    val notes: String? = null,
)
