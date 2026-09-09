package com.scttech.android.kotlin.openfitness.ui.profile

import com.scttech.android.kotlin.openfitness.domain.model.Profile

sealed interface ProfileUiState {
    data object Loading : ProfileUiState

    data class Loaded(
        val profiles: List<Profile>,
        val currentProfileId: Long?,
    ) : ProfileUiState
}
