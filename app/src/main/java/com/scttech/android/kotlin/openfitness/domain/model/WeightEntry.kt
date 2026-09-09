package com.scttech.android.kotlin.openfitness.domain.model

import kotlinx.datetime.LocalDate

/** A single body-weight log entry for a profile. */
data class WeightEntry(
    val id: Long = 0L,
    val profileId: Long,
    val date: LocalDate,
    val weightKg: Double,
    val note: String = "",
)
