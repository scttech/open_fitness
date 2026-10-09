package com.scttech.android.kotlin.openfitness.ui.badges

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scttech.android.kotlin.openfitness.data.repository.BadgeRepository
import com.scttech.android.kotlin.openfitness.data.repository.ProfileRepository
import com.scttech.android.kotlin.openfitness.domain.model.Badge
import com.scttech.android.kotlin.openfitness.domain.model.EarnedBadge
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface BadgesUiState {
    data object Loading : BadgesUiState
    data class Success(val earned: List<EarnedBadge>) : BadgesUiState {
        val earnedBadges: Set<Badge> get() = earned.map { it.badge }.toSet()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BadgesViewModel @Inject constructor(
    profileRepository: ProfileRepository,
    badgeRepository: BadgeRepository,
) : ViewModel() {

    val uiState: StateFlow<BadgesUiState> = profileRepository.currentProfileId
        .filterNotNull()
        .flatMapLatest { badgeRepository.observeBadges(it) }
        .map<List<EarnedBadge>, BadgesUiState> { BadgesUiState.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BadgesUiState.Loading,
        )
}
