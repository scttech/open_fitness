package com.scttech.android.kotlin.openfitness.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scttech.android.kotlin.openfitness.data.repository.ProfileRepository
import com.scttech.android.kotlin.openfitness.domain.model.Profile
import com.scttech.android.kotlin.openfitness.ui.theme.ProfileAccentColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = combine(
        profileRepository.observeProfiles(),
        profileRepository.currentProfileId,
    ) { profiles, currentProfileId ->
        ProfileUiState.Loaded(profiles = profiles, currentProfileId = currentProfileId)
    }
        .map<ProfileUiState.Loaded, ProfileUiState> { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProfileUiState.Loading,
        )

    fun createAndSelectProfile(name: String) {
        viewModelScope.launch {
            val colorIndex = (uiState.value as? ProfileUiState.Loaded)?.profiles?.size?.rem(ProfileAccentColors.size) ?: 0
            val id = profileRepository.createProfile(name, colorIndex)
            profileRepository.setCurrentProfile(id)
        }
    }

    fun selectProfile(profile: Profile) {
        viewModelScope.launch {
            profileRepository.setCurrentProfile(profile.id)
        }
    }

    fun renameProfile(profile: Profile, newName: String) {
        viewModelScope.launch {
            profileRepository.renameProfile(profile, newName)
        }
    }

    fun deleteProfile(profile: Profile) {
        viewModelScope.launch {
            val wasCurrent = (uiState.value as? ProfileUiState.Loaded)?.currentProfileId == profile.id
            profileRepository.deleteProfile(profile)
            if (wasCurrent) {
                profileRepository.setCurrentProfile(null)
            }
        }
    }
}
