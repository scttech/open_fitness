package com.scttech.android.kotlin.openfitness.data.repository

import com.scttech.android.kotlin.openfitness.domain.model.Exercise
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun observeExercises(): Flow<List<Exercise>>
    fun observeExercise(id: Long): Flow<Exercise?>
    fun searchExercises(query: String): Flow<List<Exercise>>
    suspend fun saveExercise(exercise: Exercise): Long
    suspend fun deleteExercise(exercise: Exercise)
    suspend fun seedExercisesIfNeeded()
}
