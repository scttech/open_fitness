package com.scttech.android.kotlin.openfitness.ui.settings

import com.scttech.android.kotlin.openfitness.domain.model.Profile

sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Success(val currentProfile: Profile) : SettingsUiState
}
