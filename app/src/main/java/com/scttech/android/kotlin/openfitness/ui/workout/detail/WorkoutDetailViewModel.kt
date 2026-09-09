package com.scttech.android.kotlin.openfitness.ui.workout.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.scttech.android.kotlin.openfitness.data.repository.WorkoutRepository
import com.scttech.android.kotlin.openfitness.domain.model.Workout
import com.scttech.android.kotlin.openfitness.ui.navigation.WorkoutDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface WorkoutDetailUiState {
    data object Loading : WorkoutDetailUiState
    data class Success(val workout: Workout) : WorkoutDetailUiState
    data object NotFound : WorkoutDetailUiState
}

@HiltViewModel
class WorkoutDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {

    private val route: WorkoutDetailRoute = savedStateHandle.toRoute()

    val uiState: StateFlow<WorkoutDetailUiState> = workoutRepository.observeWorkout(route.workoutId)
        .map<Workout?, WorkoutDetailUiState> { workout -> workout?.let { WorkoutDetailUiState.Success(it) } ?: WorkoutDetailUiState.NotFound }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WorkoutDetailUiState.Loading,
        )

    private val _deleted = MutableSharedFlow<Unit>()
    val deleted: SharedFlow<Unit> = _deleted

    fun deleteWorkout() {
        val state = uiState.value
        if (state !is WorkoutDetailUiState.Success) return
        viewModelScope.launch {
            workoutRepository.deleteWorkout(state.workout)
            _deleted.emit(Unit)
        }
    }
}
