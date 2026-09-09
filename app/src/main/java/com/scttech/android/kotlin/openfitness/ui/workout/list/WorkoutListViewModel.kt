package com.scttech.android.kotlin.openfitness.ui.workout.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scttech.android.kotlin.openfitness.data.repository.ProfileRepository
import com.scttech.android.kotlin.openfitness.data.repository.WorkoutRepository
import com.scttech.android.kotlin.openfitness.domain.model.Workout
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WorkoutListViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    val uiState: StateFlow<WorkoutListUiState> = profileRepository.currentProfileId
        .filterNotNull()
        .flatMapLatest { profileId -> workoutRepository.observeWorkoutsForProfile(profileId) }
        .map<List<Workout>, WorkoutListUiState> { WorkoutListUiState.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WorkoutListUiState.Loading,
        )

    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch {
            workoutRepository.deleteWorkout(workout)
        }
    }

    /** Clears the active profile so the profile picker doesn't immediately navigate back here. */
    suspend fun switchProfile() {
        profileRepository.setCurrentProfile(null)
    }
}
