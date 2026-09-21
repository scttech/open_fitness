package com.scttech.android.kotlin.openfitness.ui.settings

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scttech.android.kotlin.openfitness.data.repository.ProfileRepository
import com.scttech.android.kotlin.openfitness.domain.model.Profile
import com.scttech.android.kotlin.openfitness.domain.model.ThemeMode
import com.scttech.android.kotlin.openfitness.ui.common.timer.timerColorPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        profileRepository.observeCurrentProfile(),
        profileRepository.themeMode,
    ) { profile: Profile?, themeMode: ThemeMode ->
        if (profile == null) {
            SettingsUiState.Loading
        } else {
            val colors = profile.timerColorPrefs()
            SettingsUiState.Success(
                currentProfile = profile,
                timerSoundEnabled = profile.timerSoundEnabled,
                timerWorkColor = colors.workColor,
                timerRestColor = colors.restColor,
                themeMode = themeMode,
            )
        }
    }
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

    fun setTimerSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val profileId = profileRepository.currentProfileId.filterNotNull().first()
            profileRepository.setTimerSoundEnabled(profileId, enabled)
        }
    }

    fun setTimerWorkColor(color: Color) {
        viewModelScope.launch {
            val profileId = profileRepository.currentProfileId.filterNotNull().first()
            profileRepository.setTimerWorkColor(profileId, color.toArgb())
        }
    }

    fun setTimerRestColor(color: Color) {
        viewModelScope.launch {
            val profileId = profileRepository.currentProfileId.filterNotNull().first()
            profileRepository.setTimerRestColor(profileId, color.toArgb())
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            profileRepository.setThemeMode(mode)
        }
    }
}
