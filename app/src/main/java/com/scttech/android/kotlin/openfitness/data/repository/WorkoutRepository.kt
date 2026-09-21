package com.scttech.android.kotlin.openfitness.data.repository

import com.scttech.android.kotlin.openfitness.domain.model.Workout
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun observeWorkoutsForProfile(profileId: Long): Flow<List<Workout>>
    fun observeTemplates(): Flow<List<Workout>>
    fun observeWorkout(id: Long): Flow<Workout?>
    suspend fun saveWorkout(workout: Workout): Long
    suspend fun deleteWorkout(workout: Workout)

    /** True if [profileId] already has a workout named [name] (case-insensitive), other than [excludeWorkoutId]. */
    suspend fun nameExists(profileId: Long, name: String, excludeWorkoutId: Long): Boolean
    suspend fun copyTemplateToProfile(template: Workout, profileId: Long): Long
    suspend fun seedTemplatesIfNeeded()
}
