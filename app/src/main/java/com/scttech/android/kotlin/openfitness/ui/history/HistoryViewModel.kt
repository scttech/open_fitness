package com.scttech.android.kotlin.openfitness.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scttech.android.kotlin.openfitness.data.repository.ProfileRepository
import com.scttech.android.kotlin.openfitness.data.repository.SessionRepository
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface HistoryUiState {
    data object Loading : HistoryUiState
    data class Success(val sessions: List<WorkoutSession>) : HistoryUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    profileRepository: ProfileRepository,
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = profileRepository.currentProfileId
        .filterNotNull()
        .flatMapLatest { profileId -> sessionRepository.observeSessionsForProfile(profileId) }
        .map<List<WorkoutSession>, HistoryUiState> { HistoryUiState.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUiState.Loading,
        )
}
