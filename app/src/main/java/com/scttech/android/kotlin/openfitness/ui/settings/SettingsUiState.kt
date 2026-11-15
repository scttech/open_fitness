package com.scttech.android.kotlin.openfitness.ui.settings

import androidx.compose.ui.graphics.Color
import com.scttech.android.kotlin.openfitness.domain.model.DEFAULT_TIMER_COUNTDOWN_SECONDS
import com.scttech.android.kotlin.openfitness.domain.model.Profile
import com.scttech.android.kotlin.openfitness.domain.model.SkinTone
import com.scttech.android.kotlin.openfitness.domain.model.ThemeMode
import com.scttech.android.kotlin.openfitness.ui.theme.DefaultTimerRestColor
import com.scttech.android.kotlin.openfitness.ui.theme.DefaultTimerWorkColor

sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Success(
        val currentProfile: Profile,
        val timerSoundEnabled: Boolean = true,
        val timerCountdownSeconds: Int = DEFAULT_TIMER_COUNTDOWN_SECONDS,
        val timerWorkColor: Color = DefaultTimerWorkColor,
        val timerRestColor: Color = DefaultTimerRestColor,
        val themeMode: ThemeMode = ThemeMode.SYSTEM,
        val skinTone: SkinTone = SkinTone.ORIGINAL,
    ) : SettingsUiState
}
