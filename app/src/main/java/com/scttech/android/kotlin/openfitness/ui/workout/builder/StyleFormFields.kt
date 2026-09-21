package com.scttech.android.kotlin.openfitness.ui.workout.builder

import com.scttech.android.kotlin.openfitness.domain.model.WorkoutExercise
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutStyle
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutStyleConfig

/**
 * Text-editable mirror of [WorkoutStyleConfig], one variant per style. Kept as raw strings (not
 * Int/Double) so text fields can hold transient/invalid states (e.g. an emptied field) while the
 * user is typing, without fighting the parsed domain value. Converted to a real
 * [WorkoutStyleConfig] + exercise list only on save.
 */
sealed interface StyleFormFields {

    data class TabataFields(
        val workSeconds: String,
        val restSeconds: String,
        val roundsPerCycle: String,
        val cycles: String,
        val restBetweenCyclesSeconds: String,
        val countdownSoundEnabled: Boolean = false,
    ) : StyleFormFields

    data class GtgFields(
        val exerciseName: String,
        val repsPerSet: String,
        val targetSetsPerDay: String,
        val minRestMinutesBetweenSets: String,
        val exerciseId: Long? = null,
    ) : StyleFormFields

    data class PyramidFields(
        val exerciseName: String,
        val direction: WorkoutStyleConfig.PyramidDirection,
        val startReps: String,
        val stepReps: String,
        val peakReps: String,
        val restSeconds: String,
        val exerciseId: Long? = null,
    ) : StyleFormFields

    data class DensityFields(
        val durationMinutes: String,
        val targetRounds: String,
    ) : StyleFormFields

    data class StepLoadingFields(
        val exerciseName: String,
        val startWeightKg: String,
        val stepWeightKg: String,
        val setCount: String,
        val repsPerSet: String,
        val deloadEverySessions: String,
        val restSeconds: String,
        val exerciseId: Long? = null,
    ) : StyleFormFields

    data class EmomFields(
        val repGoal: String,
        val rounds: String,
        val restBetweenRoundsSeconds: String,
    ) : StyleFormFields

    companion object {
        fun default(style: WorkoutStyle): StyleFormFields = when (style) {
            WorkoutStyle.TABATA -> TabataFields("20", "10", "8", "1", "60")
            WorkoutStyle.GREASE_THE_GROOVE -> GtgFields("", "5", "10", "30")
            WorkoutStyle.PYRAMID -> PyramidFields("", WorkoutStyleConfig.PyramidDirection.UP_DOWN, "2", "2", "10", "60")
            WorkoutStyle.DENSITY -> DensityFields("15", "")
            WorkoutStyle.STEP_LOADING -> StepLoadingFields("", "20", "5", "5", "5", "", "60")
            WorkoutStyle.EMOM -> EmomFields("10", "3", "60")
        }

        fun from(config: WorkoutStyleConfig, exercises: List<WorkoutExercise>): StyleFormFields = when (config) {
            is WorkoutStyleConfig.Tabata -> TabataFields(
                workSeconds = config.workSeconds.toString(),
                restSeconds = config.restSeconds.toString(),
                roundsPerCycle = config.roundsPerCycle.toString(),
                cycles = config.cycles.toString(),
                restBetweenCyclesSeconds = config.restBetweenCyclesSeconds.toString(),
                countdownSoundEnabled = config.countdownSoundEnabled,
            )
            is WorkoutStyleConfig.GreaseTheGroove -> GtgFields(
                exerciseName = config.exerciseName,
                exerciseId = exercises.firstOrNull()?.exerciseId,
                repsPerSet = config.repsPerSet.toString(),
                targetSetsPerDay = config.targetSetsPerDay.toString(),
                minRestMinutesBetweenSets = config.minRestMinutesBetweenSets.toString(),
            )
            is WorkoutStyleConfig.Pyramid -> PyramidFields(
                exerciseName = exercises.firstOrNull()?.name.orEmpty(),
                exerciseId = exercises.firstOrNull()?.exerciseId,
                direction = config.direction,
                startReps = config.startReps.toString(),
                stepReps = config.stepReps.toString(),
                peakReps = config.peakReps.toString(),
                restSeconds = config.restSeconds.toString(),
            )
            is WorkoutStyleConfig.Density -> DensityFields(
                durationMinutes = config.durationMinutes.toString(),
                targetRounds = config.targetRounds?.toString().orEmpty(),
            )
            is WorkoutStyleConfig.StepLoading -> StepLoadingFields(
                exerciseName = exercises.firstOrNull()?.name.orEmpty(),
                exerciseId = exercises.firstOrNull()?.exerciseId,
                startWeightKg = config.startWeightKg.toString(),
                stepWeightKg = config.stepWeightKg.toString(),
                setCount = config.setCount.toString(),
                repsPerSet = config.repsPerSet.toString(),
                deloadEverySessions = config.deloadEverySessions?.toString().orEmpty(),
                restSeconds = config.restSeconds.toString(),
            )
            is WorkoutStyleConfig.Emom -> EmomFields(
                repGoal = config.repGoal.toString(),
                rounds = config.rounds.toString(),
                restBetweenRoundsSeconds = config.restBetweenRoundsSeconds.toString(),
            )
        }
    }
}

fun StyleFormFields.toStyleConfig(): WorkoutStyleConfig = when (this) {
    is StyleFormFields.TabataFields -> WorkoutStyleConfig.Tabata(
        workSeconds = workSeconds.toIntOrNull() ?: 20,
        restSeconds = restSeconds.toIntOrNull() ?: 10,
        roundsPerCycle = roundsPerCycle.toIntOrNull() ?: 8,
        cycles = cycles.toIntOrNull() ?: 1,
        restBetweenCyclesSeconds = restBetweenCyclesSeconds.toIntOrNull() ?: 60,
        countdownSoundEnabled = countdownSoundEnabled,
    )
    is StyleFormFields.GtgFields -> WorkoutStyleConfig.GreaseTheGroove(
        exerciseName = exerciseName,
        repsPerSet = repsPerSet.toIntOrNull() ?: 5,
        targetSetsPerDay = targetSetsPerDay.toIntOrNull() ?: 10,
        minRestMinutesBetweenSets = minRestMinutesBetweenSets.toIntOrNull() ?: 30,
    )
    is StyleFormFields.PyramidFields -> WorkoutStyleConfig.Pyramid(
        direction = direction,
        startReps = startReps.toIntOrNull() ?: 2,
        stepReps = stepReps.toIntOrNull() ?: 2,
        peakReps = peakReps.toIntOrNull() ?: 10,
        restSeconds = restSeconds.toIntOrNull() ?: 60,
    )
    is StyleFormFields.DensityFields -> WorkoutStyleConfig.Density(
        durationMinutes = durationMinutes.toIntOrNull() ?: 15,
        targetRounds = targetRounds.toIntOrNull(),
    )
    is StyleFormFields.StepLoadingFields -> WorkoutStyleConfig.StepLoading(
        startWeightKg = startWeightKg.toDoubleOrNull() ?: 20.0,
        stepWeightKg = stepWeightKg.toDoubleOrNull() ?: 5.0,
        setCount = setCount.toIntOrNull() ?: 5,
        repsPerSet = repsPerSet.toIntOrNull() ?: 5,
        deloadEverySessions = deloadEverySessions.toIntOrNull(),
        restSeconds = restSeconds.toIntOrNull() ?: 60,
    )
    is StyleFormFields.EmomFields -> WorkoutStyleConfig.Emom(
        repGoal = repGoal.toIntOrNull() ?: 10,
        rounds = rounds.toIntOrNull() ?: 3,
        restBetweenRoundsSeconds = restBetweenRoundsSeconds.toIntOrNull() ?: 60,
    )
}

/** Applies a library-picked exercise to the styles that target a single named exercise. */
fun StyleFormFields.withPickedExercise(name: String, exerciseId: Long?): StyleFormFields = when (this) {
    is StyleFormFields.GtgFields -> copy(exerciseName = name, exerciseId = exerciseId)
    is StyleFormFields.PyramidFields -> copy(exerciseName = name, exerciseId = exerciseId)
    is StyleFormFields.StepLoadingFields -> copy(exerciseName = name, exerciseId = exerciseId)
    else -> this
}

fun StyleFormFields.singleExerciseName(): String? = when (this) {
    is StyleFormFields.GtgFields -> exerciseName
    is StyleFormFields.PyramidFields -> exerciseName
    is StyleFormFields.StepLoadingFields -> exerciseName
    else -> null
}

fun StyleFormFields.toExercises(circuitExercises: List<CircuitExerciseField>): List<WorkoutExercise> = when (this) {
    is StyleFormFields.TabataFields -> circuitExercises.mapIndexed { i, ex ->
        WorkoutExercise(order = i, name = ex.name, exerciseId = ex.exerciseId)
    }
    is StyleFormFields.DensityFields -> circuitExercises.mapIndexed { i, ex ->
        WorkoutExercise(order = i, name = ex.name, exerciseId = ex.exerciseId)
    }
    is StyleFormFields.EmomFields -> circuitExercises.mapIndexed { i, ex ->
        WorkoutExercise(order = i, name = ex.name, exerciseId = ex.exerciseId)
    }
    is StyleFormFields.GtgFields -> listOf(
        WorkoutExercise(order = 0, name = exerciseName, exerciseId = exerciseId, targetReps = repsPerSet.toIntOrNull()),
    )
    is StyleFormFields.PyramidFields -> listOf(WorkoutExercise(order = 0, name = exerciseName, exerciseId = exerciseId))
    is StyleFormFields.StepLoadingFields -> listOf(
        WorkoutExercise(
            order = 0,
            name = exerciseName,
            exerciseId = exerciseId,
            targetReps = repsPerSet.toIntOrNull(),
            targetWeightKg = startWeightKg.toDoubleOrNull(),
        ),
    )
}
