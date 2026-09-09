package com.scttech.android.kotlin.openfitness.ui.workout.builder

import com.scttech.android.kotlin.openfitness.domain.model.WorkoutStyle

sealed interface WorkoutBuilderUiState {
    data object Loading : WorkoutBuilderUiState

    data class Loaded(
        val workoutId: Long,
        val isNew: Boolean,
        val style: WorkoutStyle,
        val name: String,
        val notes: String,
        val circuitExerciseNames: List<String>,
        val fields: StyleFormFields,
        val saved: Boolean = false,
    ) : WorkoutBuilderUiState {
        val usesCircuitExercises: Boolean get() = fields is StyleFormFields.TabataFields || fields is StyleFormFields.DensityFields
        val canSave: Boolean get() = name.isNotBlank() && (
            if (usesCircuitExercises) circuitExerciseNames.any { it.isNotBlank() }
            else !fields.singleExerciseName().isNullOrBlank()
        )
    }
}
