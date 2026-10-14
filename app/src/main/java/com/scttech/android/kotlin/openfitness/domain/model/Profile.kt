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
    /** Tick sounds play during the last this-many seconds of a countdown. */
    val timerCountdownSeconds: Int = DEFAULT_TIMER_COUNTDOWN_SECONDS,
    /** Null means "use the default" - see [com.scttech.android.kotlin.openfitness.ui.theme.DefaultTimerWorkColor]. */
    val timerWorkColorArgb: Int? = null,
    val timerRestColorArgb: Int? = null,
    /** A bundled preset avatar's key (see `ui.theme.AvatarPresets`). Mutually exclusive with [avatarFilePath]. */
    val avatarPresetKey: String? = null,
    /** Absolute path to a user-supplied avatar photo on disk. Mutually exclusive with [avatarPresetKey]. */
    val avatarFilePath: String? = null,
    /** How this profile sees and enters weights; stored weights are always kilograms. */
    val weightUnit: WeightUnit = WeightUnit.KG,
)

const val DEFAULT_TIMER_COUNTDOWN_SECONDS = 5
const val MIN_TIMER_COUNTDOWN_SECONDS = 1
const val MAX_TIMER_COUNTDOWN_SECONDS = 15
