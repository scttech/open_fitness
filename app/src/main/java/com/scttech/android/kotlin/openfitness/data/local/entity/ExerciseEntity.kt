package com.scttech.android.kotlin.openfitness.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.scttech.android.kotlin.openfitness.domain.model.Exercise
import com.scttech.android.kotlin.openfitness.domain.model.ExerciseCategory
import com.scttech.android.kotlin.openfitness.domain.model.ExerciseEquipment
import kotlinx.datetime.Instant

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val category: ExerciseCategory,
    val equipment: ExerciseEquipment,
    val formNotes: String,
    val isCustom: Boolean,
    val createdAtEpochMillis: Long,
)

fun ExerciseEntity.asDomainModel() = Exercise(
    id = id,
    name = name,
    category = category,
    equipment = equipment,
    formNotes = formNotes,
    isCustom = isCustom,
    createdAt = Instant.fromEpochMilliseconds(createdAtEpochMillis),
)

fun Exercise.asEntity() = ExerciseEntity(
    id = id,
    name = name,
    category = category,
    equipment = equipment,
    formNotes = formNotes,
    isCustom = isCustom,
    createdAtEpochMillis = createdAt.toEpochMilliseconds(),
)
