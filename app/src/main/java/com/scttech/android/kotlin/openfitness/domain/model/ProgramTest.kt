package com.scttech.android.kotlin.openfitness.domain.model

import kotlinx.datetime.Instant

/** A recorded max-effort test for a [Program], the trigger for recalculating its prescription. */
data class ProgramTest(
    val id: Long = 0L,
    val programId: Long,
    val profileId: Long,
    val testedAt: Instant,
    val result: Double,
    val notes: String = "",
)
