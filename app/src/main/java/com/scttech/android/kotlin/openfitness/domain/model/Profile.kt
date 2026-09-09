package com.scttech.android.kotlin.openfitness.domain.model

import kotlinx.datetime.Instant

/** An on-device user profile ("save slot"). No auth - purely a local data partition. */
data class Profile(
    val id: Long = 0L,
    val name: String,
    val colorIndex: Int = 0,
    val createdAt: Instant,
)
