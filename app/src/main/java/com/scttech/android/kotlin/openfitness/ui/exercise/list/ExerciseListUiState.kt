package com.scttech.android.kotlin.openfitness.ui.exercise.list

import com.scttech.android.kotlin.openfitness.domain.model.Exercise
import com.scttech.android.kotlin.openfitness.domain.model.ExerciseCategory
import com.scttech.android.kotlin.openfitness.domain.model.ExerciseEquipment

sealed interface ExerciseListUiState {
    data object Loading : ExerciseListUiState
    data class Success(
        val exercises: List<Exercise>,
        val query: String,
        val selectedCategory: ExerciseCategory? = null,
        val selectedEquipment: ExerciseEquipment? = null,
    ) : ExerciseListUiState
}
