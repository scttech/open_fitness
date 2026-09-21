package com.scttech.android.kotlin.openfitness.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.scttech.android.kotlin.openfitness.data.repository.ProfileRepository
import com.scttech.android.kotlin.openfitness.data.repository.ProgramRepository
import com.scttech.android.kotlin.openfitness.data.repository.ProgramSessionRepository
import com.scttech.android.kotlin.openfitness.domain.program.ProgramRetestPolicy
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * Runs roughly once a day (see [OpenFitnessApplication][com.scttech.android.kotlin.openfitness.OpenFitnessApplication])
 * and posts a notification for every non-archived program that's due for a retest, by either of
 * [ProgramRetestPolicy]'s two cadences.
 */
@HiltWorker
class RetestReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val profileRepository: ProfileRepository,
    private val programRepository: ProgramRepository,
    private val programSessionRepository: ProgramSessionRepository,
    private val notifier: RetestReminderNotifier,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        notifier.ensureChannel()
        val profiles = profileRepository.observeProfiles().first()
        profiles.forEach { profile ->
            val programs = programRepository.observeProgramsForProfile(profile.id).first()
            programs.filterNot { it.isArchived }.forEach { program ->
                val since = program.lastTestedAt ?: program.createdAt
                val completedSessions = programSessionRepository.countCompletedSessionsSince(program.id, since)
                if (ProgramRetestPolicy.isRetestDue(program, completedSessions)) {
                    notifier.showRetestDue(program)
                }
            }
        }
        return Result.success()
    }
}
