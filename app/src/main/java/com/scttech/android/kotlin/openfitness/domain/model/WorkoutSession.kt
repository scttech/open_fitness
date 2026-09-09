package com.scttech.android.kotlin.openfitness.domain.model

import kotlinx.datetime.Instant

/** A single tracked instance of performing a workout - in progress or completed. */
data class WorkoutSession(
    val id: Long = 0L,
    val profileId: Long,
    val workoutId: Long,
    val workoutName: String,
    val style: WorkoutStyle,
    val startedAt: Instant,
    val completedAt: Instant? = null,
    val result: SessionResult? = null,
    val notes: String = "",
) {
    val isCompleted: Boolean get() = completedAt != null
}
