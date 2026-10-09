package com.scttech.android.kotlin.openfitness.data.repository

import com.scttech.android.kotlin.openfitness.domain.model.Badge
import com.scttech.android.kotlin.openfitness.domain.model.EarnedBadge
import kotlinx.coroutines.flow.Flow

interface BadgeRepository {
    fun observeBadges(profileId: Long): Flow<List<EarnedBadge>>

    /** Records every badge in [badges] the profile doesn't have yet, returning just those newly earned. */
    suspend fun award(profileId: Long, badges: Set<Badge>): List<Badge>
}
