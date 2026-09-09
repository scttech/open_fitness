package com.scttech.android.kotlin.openfitness.ui.workout.list

import com.scttech.android.kotlin.openfitness.domain.model.Workout

sealed interface WorkoutListUiState {
    data object Loading : WorkoutListUiState
    data class Success(val workouts: List<Workout>) : WorkoutListUiState
}
