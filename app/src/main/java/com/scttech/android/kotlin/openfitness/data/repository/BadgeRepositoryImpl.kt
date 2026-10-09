package com.scttech.android.kotlin.openfitness.data.repository

import com.scttech.android.kotlin.openfitness.data.local.dao.BadgeDao
import com.scttech.android.kotlin.openfitness.data.local.entity.BadgeEntity
import com.scttech.android.kotlin.openfitness.domain.model.Badge
import com.scttech.android.kotlin.openfitness.domain.model.EarnedBadge
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BadgeRepositoryImpl @Inject constructor(
    private val badgeDao: BadgeDao,
) : BadgeRepository {

    override fun observeBadges(profileId: Long): Flow<List<EarnedBadge>> =
        badgeDao.observeBadgesForProfile(profileId).map { entities ->
            // A key with no matching Badge (e.g. written by a newer app version) is skipped, not fatal.
            entities.mapNotNull { entity ->
                Badge.entries.firstOrNull { it.name == entity.badgeKey }
                    ?.let { EarnedBadge(it, Instant.fromEpochMilliseconds(entity.earnedAtEpochMillis)) }
            }
        }

    override suspend fun award(profileId: Long, badges: Set<Badge>): List<Badge> {
        val already = badgeDao.getBadgeKeys(profileId).toSet()
        val now = Clock.System.now().toEpochMilliseconds()
        return badges
            .filter { it.name !in already }
            .filter { badgeDao.insert(BadgeEntity(profileId = profileId, badgeKey = it.name, earnedAtEpochMillis = now)) != -1L }
    }
}
