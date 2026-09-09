package com.scttech.android.kotlin.openfitness.ui.exercise.builder

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.scttech.android.kotlin.openfitness.data.repository.ExerciseRepository
import com.scttech.android.kotlin.openfitness.domain.model.Exercise
import com.scttech.android.kotlin.openfitness.domain.model.ExerciseCategory
import com.scttech.android.kotlin.openfitness.domain.model.ExerciseEquipment
import com.scttech.android.kotlin.openfitness.ui.navigation.ExerciseBuilderRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import javax.inject.Inject

@HiltViewModel
class ExerciseBuilderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {

    private val route: ExerciseBuilderRoute = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow<ExerciseBuilderUiState>(ExerciseBuilderUiState.Loading)
    val uiState: StateFlow<ExerciseBuilderUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            if (route.exerciseId != 0L) {
                val exercise = exerciseRepository.observeExercise(route.exerciseId).first()
                if (exercise != null) {
                    _uiState.value = ExerciseBuilderUiState.Loaded(
                        exerciseId = exercise.id,
                        name = exercise.name,
                        category = exercise.category,
                        equipment = exercise.equipment,
                        formNotes = exercise.formNotes,
                        isCustom = exercise.isCustom,
                    )
                    return@launch
                }
            }
            _uiState.value = ExerciseBuilderUiState.Loaded(
                exerciseId = 0L,
                name = "",
                category = ExerciseCategory.FULL_BODY,
                equipment = ExerciseEquipment.BODYWEIGHT,
                formNotes = "",
            )
        }
    }

    fun updateName(name: String) = updateLoaded { copy(name = name) }
    fun updateCategory(category: ExerciseCategory) = updateLoaded { copy(category = category) }
    fun updateEquipment(equipment: ExerciseEquipment) = updateLoaded { copy(equipment = equipment) }
    fun updateFormNotes(notes: String) = updateLoaded { copy(formNotes = notes) }

    private inline fun updateLoaded(block: ExerciseBuilderUiState.Loaded.() -> ExerciseBuilderUiState.Loaded) {
        val current = _uiState.value
        if (current is ExerciseBuilderUiState.Loaded) {
            _uiState.update { current.block() }
        }
    }

    fun save() {
        val state = _uiState.value
        if (state !is ExerciseBuilderUiState.Loaded || !state.canSave) return
        viewModelScope.launch {
            val exercise = Exercise(
                id = state.exerciseId,
                name = state.name.trim(),
                category = state.category,
                equipment = state.equipment,
                formNotes = state.formNotes.trim(),
                isCustom = state.isCustom,
                createdAt = Clock.System.now(),
            )
            exerciseRepository.saveExercise(exercise)
            _uiState.update { (it as ExerciseBuilderUiState.Loaded).copy(saved = true) }
        }
    }
}
