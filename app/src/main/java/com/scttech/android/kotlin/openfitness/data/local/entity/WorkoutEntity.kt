package com.scttech.android.kotlin.openfitness.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.scttech.android.kotlin.openfitness.domain.model.Workout
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutExercise
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutStyle
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutStyleConfig
import kotlinx.datetime.Instant

@Entity(
    tableName = "workouts",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("profileId")],
)
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val profileId: Long?,
    val name: String,
    val style: WorkoutStyle,
    val styleConfig: WorkoutStyleConfig,
    val exercises: List<WorkoutExercise>,
    val notes: String,
    val isTemplate: Boolean,
    val createdAtEpochMillis: Long,
)

fun WorkoutEntity.asDomainModel() = Workout(
    id = id,
    profileId = profileId,
    name = name,
    style = style,
    styleConfig = styleConfig,
    exercises = exercises,
    notes = notes,
    isTemplate = isTemplate,
    createdAt = Instant.fromEpochMilliseconds(createdAtEpochMillis),
)

fun Workout.asEntity() = WorkoutEntity(
    id = id,
    profileId = profileId,
    name = name,
    style = style,
    styleConfig = styleConfig,
    exercises = exercises,
    notes = notes,
    isTemplate = isTemplate,
    createdAtEpochMillis = createdAt.toEpochMilliseconds(),
)
