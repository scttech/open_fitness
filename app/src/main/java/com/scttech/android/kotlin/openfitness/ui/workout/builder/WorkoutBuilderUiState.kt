package com.scttech.android.kotlin.openfitness.ui.workout.builder

import com.scttech.android.kotlin.openfitness.domain.model.WorkoutStyle

/** One slot in a circuit-style workout (Tabata, Density, EMOM) - always picked from the exercise library. */
data class CircuitExerciseField(
    val name: String = "",
    val exerciseId: Long? = null,
)

sealed interface WorkoutBuilderUiState {
    data object Loading : WorkoutBuilderUiState

    data class Loaded(
        val workoutId: Long,
        val isNew: Boolean,
        val style: WorkoutStyle,
        val name: String,
        val notes: String,
        val circuitExercises: List<CircuitExerciseField>,
        val fields: StyleFormFields,
        val saved: Boolean = false,
        /** Set after a save attempt collides with an existing workout's name for this profile. */
        val nameError: String? = null,
    ) : WorkoutBuilderUiState {
        val usesCircuitExercises: Boolean get() = fields is StyleFormFields.TabataFields ||
            fields is StyleFormFields.DensityFields ||
            fields is StyleFormFields.EmomFields
        val canSave: Boolean get() = name.isNotBlank() && (
            if (usesCircuitExercises) circuitExercises.any { it.name.isNotBlank() }
            else !fields.singleExerciseName().isNullOrBlank()
        )
    }
}
