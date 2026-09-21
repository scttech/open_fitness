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
import javax.inject.Singleton

/**
 * Marks whether this app process has already resolved which profile to resume into. Scoped to
 * the whole process (not the screen) so the resolution in [ProfileViewModel.uiState] runs exactly
 * once per cold start - a later, explicit "Switch profile" visit to the same screen must always
 * show the picker rather than being silently reconciled back into the app.
 */
@Singleton
class AppLaunchCoordinator @Inject constructor() {
    @Volatile
    var hasResolvedInitialProfile: Boolean = false
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val appLaunchCoordinator: AppLaunchCoordinator,
) : ViewModel() {

    /**
     * At cold start only, reconciles the remembered "current profile" against how many profiles
     * actually exist, before it ever reaches the UI: with exactly one profile, that profile is
     * always current (so the app resumes straight into it); with more than one, no profile is
     * current at startup (so the picker is shown instead of silently resuming whichever one was
     * last used). The reconciled id - not the raw persisted one - is what [ProfileUiState.Loaded]
     * carries, so the screen's auto-navigate effect never sees a stale value and briefly flashes
     * into the app before bouncing back to the picker.
     */
    val uiState: StateFlow<ProfileUiState> = combine(
        profileRepository.observeProfiles(),
        profileRepository.currentProfileId,
    ) { profiles, currentProfileId -> profiles to currentProfileId }
        .map<Pair<List<Profile>, Long?>, ProfileUiState> { (profiles, currentProfileId) ->
            val reconciledProfileId = if (appLaunchCoordinator.hasResolvedInitialProfile) {
                currentProfileId
            } else {
                appLaunchCoordinator.hasResolvedInitialProfile = true
                when {
                    profiles.size == 1 && currentProfileId != profiles[0].id -> {
                        profileRepository.setCurrentProfile(profiles[0].id)
                        profiles[0].id
                    }
                    profiles.size > 1 && currentProfileId != null -> {
                        profileRepository.setCurrentProfile(null)
                        null
                    }
                    else -> currentProfileId
                }
            }
            ProfileUiState.Loaded(profiles = profiles, currentProfileId = reconciledProfileId)
        }
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
