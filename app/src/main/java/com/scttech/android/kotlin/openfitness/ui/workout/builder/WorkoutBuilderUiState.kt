package com.scttech.android.kotlin.openfitness.ui.workout.builder

import com.scttech.android.kotlin.openfitness.domain.model.WorkoutExercise
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutStyle

/** One slot in a circuit-style workout (Tabata, Density, EMOM) - always picked from the exercise library. */
data class CircuitExerciseField(
    val name: String = "",
    val exerciseId: Long? = null,
)

/** One step in a Follow Along sequence: either a library exercise with its own work duration, or a rest of its own length. */
data class FollowAlongStepField(
    val isRest: Boolean,
    val name: String = "",
    val exerciseId: Long? = null,
    val seconds: String = "30",
)

fun List<FollowAlongStepField>.toWorkoutExercises(): List<WorkoutExercise> =
    filter { it.isRest || it.name.isNotBlank() }
        .mapIndexed { i, step ->
            WorkoutExercise(
                order = i,
                name = if (step.isRest) "Rest" else step.name,
                exerciseId = if (step.isRest) null else step.exerciseId,
                targetDurationSeconds = step.seconds.toIntOrNull() ?: if (step.isRest) 15 else 30,
                isRest = step.isRest,
            )
        }

sealed interface WorkoutBuilderUiState {
    data object Loading : WorkoutBuilderUiState

    data class Loaded(
        val workoutId: Long,
        val isNew: Boolean,
        val style: WorkoutStyle,
        val name: String,
        val notes: String,
        val circuitExercises: List<CircuitExerciseField>,
        val followAlongSteps: List<FollowAlongStepField> = emptyList(),
        val fields: StyleFormFields,
        val saved: Boolean = false,
        /** Set after a save attempt collides with an existing workout's name for this profile. */
        val nameError: String? = null,
    ) : WorkoutBuilderUiState {
        val usesCircuitExercises: Boolean get() = fields is StyleFormFields.TabataFields ||
            fields is StyleFormFields.DensityFields ||
            fields is StyleFormFields.EmomFields
        val usesFollowAlongSteps: Boolean get() = fields is StyleFormFields.FollowAlongFields
        val canSave: Boolean get() = name.isNotBlank() && (
            when {
                usesCircuitExercises -> circuitExercises.any { it.name.isNotBlank() }
                usesFollowAlongSteps -> followAlongSteps.any { !it.isRest && it.name.isNotBlank() }
                else -> !fields.singleExerciseName().isNullOrBlank()
            }
        )
    }
}
