package com.scttech.android.kotlin.openfitness.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.scttech.android.kotlin.openfitness.data.repository.ProfileRepository
import com.scttech.android.kotlin.openfitness.data.repository.ProgramRepository
import com.scttech.android.kotlin.openfitness.domain.model.ProgramDayType
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * Runs roughly once a day (see [OpenFitnessApplication][com.scttech.android.kotlin.openfitness.OpenFitnessApplication])
 * and posts a notification for every non-archived program whose weekly schedule marks today as a
 * workout or test day. Rest days are silent - the rest-day warning instead lives at the
 * "Start Session" tap (see `ui/program/detail/ProgramDetailScreen.kt`).
 */
@HiltWorker
class ProgramDayAlertWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val profileRepository: ProfileRepository,
    private val programRepository: ProgramRepository,
    private val notifier: ProgramDayAlertNotifier,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        notifier.ensureChannel()
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).dayOfWeek
        val profiles = profileRepository.observeProfiles().first()
        profiles.forEach { profile ->
            val programs = programRepository.observeProgramsForProfile(profile.id).first()
            programs.filterNot { it.isArchived }.forEach { program ->
                when (program.config.weekSchedule[today]) {
                    ProgramDayType.WORKOUT -> notifier.showWorkoutDayAlert(program)
                    ProgramDayType.TEST -> notifier.showTestDayAlert(program)
                    ProgramDayType.REST -> Unit
                }
            }
        }
        return Result.success()
    }
}
