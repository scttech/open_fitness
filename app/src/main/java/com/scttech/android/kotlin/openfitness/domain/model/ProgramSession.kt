package com.scttech.android.kotlin.openfitness.domain.model

import kotlinx.datetime.Instant

/** A single tracked instance of training against a [Program]'s current prescription. */
data class ProgramSession(
    val id: Long = 0L,
    val profileId: Long,
    val programId: Long,
    val programName: String,
    val startedAt: Instant,
    val completedAt: Instant? = null,
    val loggedSets: List<PerformedSet> = emptyList(),
    val notes: String = "",
) {
    val isCompleted: Boolean get() = completedAt != null
}
