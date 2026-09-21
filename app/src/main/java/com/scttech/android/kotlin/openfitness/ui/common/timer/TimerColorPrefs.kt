package com.scttech.android.kotlin.openfitness.ui.common.timer

import androidx.compose.ui.graphics.Color
import com.scttech.android.kotlin.openfitness.domain.model.Profile
import com.scttech.android.kotlin.openfitness.ui.theme.DefaultTimerRestColor
import com.scttech.android.kotlin.openfitness.ui.theme.DefaultTimerWorkColor

/** The active profile's chosen work/rest phase colors, resolved to defaults when unset. */
data class TimerColorPrefs(
    val workColor: Color = DefaultTimerWorkColor,
    val restColor: Color = DefaultTimerRestColor,
)

fun Profile?.timerColorPrefs(): TimerColorPrefs = TimerColorPrefs(
    workColor = this?.timerWorkColorArgb?.let { Color(it) } ?: DefaultTimerWorkColor,
    restColor = this?.timerRestColorArgb?.let { Color(it) } ?: DefaultTimerRestColor,
)
