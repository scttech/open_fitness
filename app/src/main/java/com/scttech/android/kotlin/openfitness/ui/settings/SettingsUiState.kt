package com.scttech.android.kotlin.openfitness.ui.settings

import androidx.compose.ui.graphics.Color
import com.scttech.android.kotlin.openfitness.domain.model.Profile
import com.scttech.android.kotlin.openfitness.domain.model.ThemeMode
import com.scttech.android.kotlin.openfitness.ui.theme.DefaultTimerRestColor
import com.scttech.android.kotlin.openfitness.ui.theme.DefaultTimerWorkColor

sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Success(
        val currentProfile: Profile,
        val timerSoundEnabled: Boolean = true,
        val timerWorkColor: Color = DefaultTimerWorkColor,
        val timerRestColor: Color = DefaultTimerRestColor,
        val themeMode: ThemeMode = ThemeMode.SYSTEM,
    ) : SettingsUiState
}
