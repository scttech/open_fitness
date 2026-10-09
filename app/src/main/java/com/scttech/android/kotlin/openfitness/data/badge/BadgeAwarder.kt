package com.scttech.android.kotlin.openfitness.data.badge

import com.scttech.android.kotlin.openfitness.data.repository.BadgeRepository
import com.scttech.android.kotlin.openfitness.data.repository.ProfileRepository
import com.scttech.android.kotlin.openfitness.data.repository.ProgramRepository
import com.scttech.android.kotlin.openfitness.data.repository.ProgramSessionRepository
import com.scttech.android.kotlin.openfitness.data.repository.SessionRepository
import com.scttech.android.kotlin.openfitness.data.repository.WeightRepository
import com.scttech.android.kotlin.openfitness.di.ApplicationScope
import com.scttech.android.kotlin.openfitness.domain.badge.BadgeEvaluator
import com.scttech.android.kotlin.openfitness.domain.model.Badge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Watches the active profile's workouts, programs and weight log and awards any [Badge] newly
 * satisfied, so no individual screen needs to know about badges.
 *
 * The first evaluation per profile after launch is silent - it backfills badges for history that
 * predates this feature (or was recorded while the app wasn't running) without a burst of
 * announcements. Anything earned after that is published on [newlyEarned].
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class BadgeAwarder @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val sessionRepository: SessionRepository,
    private val programSessionRepository: ProgramSessionRepository,
    private val programRepository: ProgramRepository,
    private val weightRepository: WeightRepository,
    private val badgeRepository: BadgeRepository,
    @ApplicationScope private val scope: CoroutineScope,
) {
    private val _newlyEarned = MutableSharedFlow<Badge>(extraBufferCapacity = 16)
    val newlyEarned: SharedFlow<Badge> = _newlyEarned

    private val evaluatedProfiles = mutableSetOf<Long>()

    fun start() {
        scope.launch {
            profileRepository.currentProfileId
                .filterNotNull()
                .flatMapLatest { profileId ->
                    combine(
                        sessionRepository.observeSessionsForProfile(profileId),
                        programSessionRepository.observeSessionsForProfile(profileId),
                        programRepository.observeProgramsForProfile(profileId),
                        weightRepository.observeEntriesForProfile(profileId),
                    ) { workouts, programSessions, programs, weights ->
                        profileId to BadgeEvaluator.earned(
                            BadgeEvaluator.statsFrom(workouts, programSessions, programs, weights.size),
                        )
                    }
                }
                .distinctUntilChanged()
                .collect { (profileId, earned) ->
                    val isBackfill = profileId !in evaluatedProfiles
                    val awarded = badgeRepository.award(profileId, earned)
                    evaluatedProfiles += profileId
                    if (!isBackfill) awarded.forEach { _newlyEarned.tryEmit(it) }
                }
        }
    }
}
