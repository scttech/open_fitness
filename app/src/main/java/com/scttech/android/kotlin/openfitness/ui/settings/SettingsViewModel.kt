package com.scttech.android.kotlin.openfitness.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scttech.android.kotlin.openfitness.data.repository.ProfileRepository
import com.scttech.android.kotlin.openfitness.domain.model.Profile
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
class SettingsViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = profileRepository.currentProfileId
        .filterNotNull()
        .flatMapLatest { profileId -> profileRepository.observeProfile(profileId) }
        .map<Profile?, SettingsUiState> { profile -> profile?.let { SettingsUiState.Success(it) } ?: SettingsUiState.Loading }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState.Loading,
        )

    fun renameProfile(profile: Profile, newName: String) {
        viewModelScope.launch {
            profileRepository.renameProfile(profile, newName)
        }
    }

    fun deleteProfile(profile: Profile) {
        viewModelScope.launch {
            profileRepository.deleteProfile(profile)
            profileRepository.setCurrentProfile(null)
        }
    }

    /** Clears the active profile so the profile picker doesn't immediately navigate back here. */
    suspend fun switchProfile() {
        profileRepository.setCurrentProfile(null)
    }
}
