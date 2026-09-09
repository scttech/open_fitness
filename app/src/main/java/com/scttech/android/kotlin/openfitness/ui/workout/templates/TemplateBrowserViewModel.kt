package com.scttech.android.kotlin.openfitness.ui.workout.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scttech.android.kotlin.openfitness.data.repository.ProfileRepository
import com.scttech.android.kotlin.openfitness.data.repository.WorkoutRepository
import com.scttech.android.kotlin.openfitness.domain.model.Workout
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface TemplateBrowserUiState {
    data object Loading : TemplateBrowserUiState
    data class Success(val templates: List<Workout>) : TemplateBrowserUiState
}

@HiltViewModel
class TemplateBrowserViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    val uiState: StateFlow<TemplateBrowserUiState> = workoutRepository.observeTemplates()
        .map<List<Workout>, TemplateBrowserUiState> { TemplateBrowserUiState.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TemplateBrowserUiState.Loading,
        )

    private val _workoutAdded = MutableSharedFlow<Long>()
    val workoutAdded: SharedFlow<Long> = _workoutAdded

    fun useTemplate(template: Workout) {
        viewModelScope.launch {
            val profileId = profileRepository.currentProfileId.filterNotNull().first()
            val newId = workoutRepository.copyTemplateToProfile(template, profileId)
            _workoutAdded.emit(newId)
        }
    }
}
