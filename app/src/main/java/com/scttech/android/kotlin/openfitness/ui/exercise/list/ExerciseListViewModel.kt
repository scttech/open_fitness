package com.scttech.android.kotlin.openfitness.ui.exercise.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scttech.android.kotlin.openfitness.data.repository.ExerciseRepository
import com.scttech.android.kotlin.openfitness.domain.model.Exercise
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExerciseListViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")

    val uiState: StateFlow<ExerciseListUiState> = combine(
        query.flatMapLatest { q -> if (q.isBlank()) exerciseRepository.observeExercises() else exerciseRepository.searchExercises(q) },
        query,
    ) { exercises: List<Exercise>, q -> ExerciseListUiState.Success(exercises, q) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ExerciseListUiState.Loading,
        )

    fun updateQuery(value: String) {
        query.value = value
    }
}
