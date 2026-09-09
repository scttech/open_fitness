package com.scttech.android.kotlin.openfitness.ui.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scttech.android.kotlin.openfitness.data.repository.ProfileRepository
import com.scttech.android.kotlin.openfitness.data.repository.WeightRepository
import com.scttech.android.kotlin.openfitness.domain.model.WeightEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

sealed interface WeightUiState {
    data object Loading : WeightUiState
    data class Success(val entries: List<WeightEntry>) : WeightUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WeightViewModel @Inject constructor(
    private val weightRepository: WeightRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    val uiState: StateFlow<WeightUiState> = profileRepository.currentProfileId
        .filterNotNull()
        .flatMapLatest { profileId -> weightRepository.observeEntriesForProfile(profileId) }
        .map<List<WeightEntry>, WeightUiState> { WeightUiState.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WeightUiState.Loading,
        )

    fun addEntry(weightKg: Double) {
        viewModelScope.launch {
            val profileId = profileRepository.currentProfileId.filterNotNull().first()
            weightRepository.addEntry(
                WeightEntry(
                    profileId = profileId,
                    date = Clock.System.todayIn(TimeZone.currentSystemDefault()),
                    weightKg = weightKg,
                ),
            )
        }
    }

    fun deleteEntry(entry: WeightEntry) {
        viewModelScope.launch {
            weightRepository.deleteEntry(entry)
        }
    }
}
