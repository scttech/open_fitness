package com.scttech.android.kotlin.openfitness.data.local

import androidx.room.TypeConverter
import com.scttech.android.kotlin.openfitness.domain.model.ExerciseCategory
import com.scttech.android.kotlin.openfitness.domain.model.ExerciseEquipment
import com.scttech.android.kotlin.openfitness.domain.model.PerformedSet
import com.scttech.android.kotlin.openfitness.domain.model.ProgramConfig
import com.scttech.android.kotlin.openfitness.domain.model.ProgramGoalType
import com.scttech.android.kotlin.openfitness.domain.model.ProgramPrescription
import com.scttech.android.kotlin.openfitness.domain.model.SessionResult
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutExercise
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutStyle
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutStyleConfig
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

internal val entityJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

class Converters {

    @TypeConverter
    fun fromWorkoutStyle(style: WorkoutStyle): String = style.name

    @TypeConverter
    fun toWorkoutStyle(value: String): WorkoutStyle = WorkoutStyle.valueOf(value)

    @TypeConverter
    fun fromWorkoutStyleConfig(config: WorkoutStyleConfig): String =
        entityJson.encodeToString(WorkoutStyleConfig.serializer(), config)

    @TypeConverter
    fun toWorkoutStyleConfig(value: String): WorkoutStyleConfig =
        entityJson.decodeFromString(WorkoutStyleConfig.serializer(), value)

    @TypeConverter
    fun fromExerciseList(exercises: List<WorkoutExercise>): String =
        entityJson.encodeToString(ListSerializer(WorkoutExercise.serializer()), exercises)

    @TypeConverter
    fun toExerciseList(value: String): List<WorkoutExercise> =
        entityJson.decodeFromString(ListSerializer(WorkoutExercise.serializer()), value)

    @TypeConverter
    fun fromSessionResult(result: SessionResult?): String? =
        result?.let { entityJson.encodeToString(SessionResult.serializer(), it) }

    @TypeConverter
    fun toSessionResult(value: String?): SessionResult? =
        value?.let { entityJson.decodeFromString(SessionResult.serializer(), it) }

    @TypeConverter
    fun fromPerformedSetList(sets: List<PerformedSet>): String =
        entityJson.encodeToString(ListSerializer(PerformedSet.serializer()), sets)

    @TypeConverter
    fun toPerformedSetList(value: String): List<PerformedSet> =
        entityJson.decodeFromString(ListSerializer(PerformedSet.serializer()), value)

    @TypeConverter
    fun fromProgramGoalType(goalType: ProgramGoalType): String = goalType.name

    @TypeConverter
    fun toProgramGoalType(value: String): ProgramGoalType = ProgramGoalType.valueOf(value)

    @TypeConverter
    fun fromProgramConfig(config: ProgramConfig): String =
        entityJson.encodeToString(ProgramConfig.serializer(), config)

    @TypeConverter
    fun toProgramConfig(value: String): ProgramConfig =
        entityJson.decodeFromString(ProgramConfig.serializer(), value)

    @TypeConverter
    fun fromProgramPrescription(prescription: ProgramPrescription?): String? =
        prescription?.let { entityJson.encodeToString(ProgramPrescription.serializer(), it) }

    @TypeConverter
    fun toProgramPrescription(value: String?): ProgramPrescription? =
        value?.let { entityJson.decodeFromString(ProgramPrescription.serializer(), it) }

    @TypeConverter
    fun fromExerciseCategory(category: ExerciseCategory): String = category.name

    @TypeConverter
    fun toExerciseCategory(value: String): ExerciseCategory = ExerciseCategory.valueOf(value)

    @TypeConverter
    fun fromExerciseEquipment(equipment: ExerciseEquipment): String = equipment.name

    @TypeConverter
    fun toExerciseEquipment(value: String): ExerciseEquipment = ExerciseEquipment.valueOf(value)
}
