package com.scttech.android.kotlin.openfitness.ui.exercise.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.scttech.android.kotlin.openfitness.data.repository.ExerciseRepository
import com.scttech.android.kotlin.openfitness.domain.model.Exercise
import com.scttech.android.kotlin.openfitness.ui.navigation.ExerciseDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ExerciseDetailUiState {
    data object Loading : ExerciseDetailUiState
    data class Success(val exercise: Exercise) : ExerciseDetailUiState
    data object NotFound : ExerciseDetailUiState
}

@HiltViewModel
class ExerciseDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {

    private val route: ExerciseDetailRoute = savedStateHandle.toRoute()

    val uiState: StateFlow<ExerciseDetailUiState> = exerciseRepository.observeExercise(route.exerciseId)
        .map<Exercise?, ExerciseDetailUiState> { exercise -> exercise?.let { ExerciseDetailUiState.Success(it) } ?: ExerciseDetailUiState.NotFound }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ExerciseDetailUiState.Loading,
        )

    private val _deleted = MutableSharedFlow<Unit>()
    val deleted: SharedFlow<Unit> = _deleted

    fun deleteExercise() {
        val state = uiState.value
        if (state !is ExerciseDetailUiState.Success) return
        viewModelScope.launch {
            exerciseRepository.deleteExercise(state.exercise)
            _deleted.emit(Unit)
        }
    }
}
