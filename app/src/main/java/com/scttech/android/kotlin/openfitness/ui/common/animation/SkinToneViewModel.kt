package com.scttech.android.kotlin.openfitness.ui.common.animation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scttech.android.kotlin.openfitness.data.repository.ProfileRepository
import com.scttech.android.kotlin.openfitness.domain.model.SkinTone
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Feeds the user's skin tone setting to the animated figure. */
@HiltViewModel
class SkinToneViewModel @Inject constructor(
    profileRepository: ProfileRepository,
) : ViewModel() {
    val skinTone: StateFlow<SkinTone> = profileRepository.skinTone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SkinTone.ORIGINAL)
}
