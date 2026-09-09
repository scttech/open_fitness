package com.scttech.android.kotlin.openfitness.data.repository

import com.scttech.android.kotlin.openfitness.data.local.dao.WorkoutDao
import com.scttech.android.kotlin.openfitness.data.local.entity.asDomainModel
import com.scttech.android.kotlin.openfitness.data.local.entity.asEntity
import com.scttech.android.kotlin.openfitness.domain.model.Workout
import com.scttech.android.kotlin.openfitness.domain.template.StarterTemplates
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepositoryImpl @Inject constructor(
    private val workoutDao: WorkoutDao,
) : WorkoutRepository {

    override fun observeWorkoutsForProfile(profileId: Long): Flow<List<Workout>> =
        workoutDao.observeWorkoutsForProfile(profileId).map { entities -> entities.map { it.asDomainModel() } }

    override fun observeTemplates(): Flow<List<Workout>> =
        workoutDao.observeTemplates().map { entities -> entities.map { it.asDomainModel() } }

    override fun observeWorkout(id: Long): Flow<Workout?> =
        workoutDao.observeWorkout(id).map { it?.asDomainModel() }

    override suspend fun saveWorkout(workout: Workout): Long {
        val toSave = if (workout.id == 0L) workout.copy(createdAt = Clock.System.now()) else workout
        return if (toSave.id == 0L) {
            workoutDao.insert(toSave.asEntity())
        } else {
            workoutDao.update(toSave.asEntity())
            toSave.id
        }
    }

    override suspend fun deleteWorkout(workout: Workout) {
        workoutDao.delete(workout.asEntity())
    }

    override suspend fun copyTemplateToProfile(template: Workout, profileId: Long): Long {
        val copy = template.copy(
            id = 0L,
            profileId = profileId,
            isTemplate = false,
            createdAt = Clock.System.now(),
        )
        return workoutDao.insert(copy.asEntity())
    }

    override suspend fun seedTemplatesIfNeeded() {
        if (workoutDao.templateCount() == 0) {
            workoutDao.insertAll(StarterTemplates.all().map { it.asEntity() })
        }
    }
}
