package com.scttech.android.kotlin.openfitness.data.local

import androidx.room.TypeConverter
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
}
