package com.scttech.android.kotlin.openfitness.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.scttech.android.kotlin.openfitness.data.local.entity.WorkoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Query("SELECT * FROM workouts WHERE profileId = :profileId ORDER BY createdAtEpochMillis DESC")
    fun observeWorkoutsForProfile(profileId: Long): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE isTemplate = 1 ORDER BY style, name")
    fun observeTemplates(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE id = :id")
    fun observeWorkout(id: Long): Flow<WorkoutEntity?>

    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getWorkout(id: Long): WorkoutEntity?

    @Query("SELECT COUNT(*) FROM workouts WHERE isTemplate = 1")
    suspend fun templateCount(): Int

    @Query(
        "SELECT EXISTS(SELECT 1 FROM workouts WHERE profileId = :profileId AND LOWER(name) = LOWER(:name) AND id != :excludeId)",
    )
    suspend fun existsWithName(profileId: Long, name: String, excludeId: Long): Boolean

    @Insert
    suspend fun insert(workout: WorkoutEntity): Long

    @Insert
    suspend fun insertAll(workouts: List<WorkoutEntity>)

    @Update
    suspend fun update(workout: WorkoutEntity)

    @Delete
    suspend fun delete(workout: WorkoutEntity)
}
