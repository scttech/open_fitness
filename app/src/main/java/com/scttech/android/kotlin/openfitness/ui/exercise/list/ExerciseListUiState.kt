package com.scttech.android.kotlin.openfitness.ui.exercise.list

import com.scttech.android.kotlin.openfitness.domain.model.Exercise

sealed interface ExerciseListUiState {
    data object Loading : ExerciseListUiState
    data class Success(val exercises: List<Exercise>, val query: String) : ExerciseListUiState
}
