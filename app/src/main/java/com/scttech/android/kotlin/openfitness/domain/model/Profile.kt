package com.scttech.android.kotlin.openfitness.domain.model

import kotlinx.datetime.Instant

/** An on-device user profile ("save slot"). No auth - purely a local data partition. */
data class Profile(
    val id: Long = 0L,
    val name: String,
    val colorIndex: Int = 0,
    val createdAt: Instant,
    /** Whether workout/program timers play tick and phase-change sounds for this profile. */
    val timerSoundEnabled: Boolean = true,
    /** Null means "use the default" - see [com.scttech.android.kotlin.openfitness.ui.theme.DefaultTimerWorkColor]. */
    val timerWorkColorArgb: Int? = null,
    val timerRestColorArgb: Int? = null,
)
