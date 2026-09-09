package com.scttech.android.kotlin.openfitness.domain.model

import kotlinx.datetime.Instant

/**
 * A workout plan. When [profileId] is null this is a shared starter template (browsable by any
 * profile, copyable into a profile's own workout list); otherwise it belongs to exactly one
 * profile.
 */
data class Workout(
    val id: Long = 0L,
    val profileId: Long?,
    val name: String,
    val style: WorkoutStyle,
    val styleConfig: WorkoutStyleConfig,
    val exercises: List<WorkoutExercise>,
    val notes: String = "",
    val isTemplate: Boolean = false,
    val createdAt: Instant,
)
