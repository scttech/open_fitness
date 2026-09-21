package com.scttech.android.kotlin.openfitness.ui.exercise.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scttech.android.kotlin.openfitness.data.repository.ExerciseRepository
import com.scttech.android.kotlin.openfitness.domain.model.ExerciseCategory
import com.scttech.android.kotlin.openfitness.domain.model.ExerciseEquipment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExerciseListViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val categoryFilter = MutableStateFlow<ExerciseCategory?>(null)
    private val equipmentFilter = MutableStateFlow<ExerciseEquipment?>(null)

    val uiState: StateFlow<ExerciseListUiState> = combine(
        query,
        categoryFilter,
        equipmentFilter,
    ) { q, category, equipment -> Triple(q, category, equipment) }
        .flatMapLatest { (q, category, equipment) ->
            val exercisesFlow =
                if (q.isBlank()) exerciseRepository.observeExercises() else exerciseRepository.searchExercises(q)
            exercisesFlow.map { exercises ->
                val filtered = exercises.filter { exercise ->
                    (category == null || exercise.category == category) &&
                        (equipment == null || exercise.equipment == equipment)
                }
                ExerciseListUiState.Success(filtered, q, category, equipment)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ExerciseListUiState.Loading,
        )

    fun updateQuery(value: String) {
        query.value = value
    }

    fun updateCategoryFilter(value: ExerciseCategory?) {
        categoryFilter.value = value
    }

    fun updateEquipmentFilter(value: ExerciseEquipment?) {
        equipmentFilter.value = value
    }
}
