package com.scttech.android.kotlin.openfitness.data.repository

import com.scttech.android.kotlin.openfitness.data.local.dao.ExerciseDao
import com.scttech.android.kotlin.openfitness.data.local.entity.asDomainModel
import com.scttech.android.kotlin.openfitness.data.local.entity.asEntity
import com.scttech.android.kotlin.openfitness.domain.model.Exercise
import com.scttech.android.kotlin.openfitness.domain.template.StarterExercises
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseRepositoryImpl @Inject constructor(
    private val exerciseDao: ExerciseDao,
) : ExerciseRepository {

    override fun observeExercises(): Flow<List<Exercise>> =
        exerciseDao.observeExercises().map { entities -> entities.map { it.asDomainModel() } }

    override fun observeExercise(id: Long): Flow<Exercise?> =
        exerciseDao.observeExercise(id).map { it?.asDomainModel() }

    override fun searchExercises(query: String): Flow<List<Exercise>> =
        exerciseDao.searchExercises(query).map { entities -> entities.map { it.asDomainModel() } }

    override suspend fun saveExercise(exercise: Exercise): Long {
        val toSave = if (exercise.id == 0L) exercise.copy(createdAt = Clock.System.now()) else exercise
        return if (toSave.id == 0L) {
            exerciseDao.insert(toSave.asEntity())
        } else {
            exerciseDao.update(toSave.asEntity())
            toSave.id
        }
    }

    override suspend fun deleteExercise(exercise: Exercise) {
        exerciseDao.delete(exercise.asEntity())
    }

    override suspend fun seedExercisesIfNeeded() {
        if (exerciseDao.count() == 0) {
            exerciseDao.insertAll(StarterExercises.all().map { it.asEntity() })
        }
    }
}
