package com.scttech.android.kotlin.openfitness.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.scttech.android.kotlin.openfitness.data.repository.ProfileRepository
import com.scttech.android.kotlin.openfitness.data.repository.ScheduleRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Runs every ~15 minutes (WorkManager's minimum period) and posts any calendar reminder whose
 * trigger time has passed since the previous run. The last-checked time is persisted so a late or
 * bunched run neither drops a reminder nor posts it twice.
 */
@HiltWorker
class ScheduleReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val profileRepository: ProfileRepository,
    private val scheduleRepository: ScheduleRepository,
    private val notifier: ScheduleReminderNotifier,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        notifier.ensureChannel()
        val timeZone = TimeZone.currentSystemDefault()
        val now = Clock.System.now()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastChecked = prefs.getLong(KEY_LAST_CHECKED, 0L)
            .let { kotlinx.datetime.Instant.fromEpochMilliseconds(it) }
            .coerceAtLeast(now - MAX_LOOKBACK)
        val today = now.toLocalDateTime(timeZone).date

        profileRepository.observeProfiles().first().forEach { profile ->
            scheduleRepository.getEvents(profile.id).forEach { event ->
                val startMinutes = event.startMinutes ?: return@forEach
                val reminderMinutes = event.reminderMinutesBefore ?: return@forEach
                // Yesterday through tomorrow covers reminders that straddle midnight.
                listOf(-1, 0, 1).map { today.plus(it, DateTimeUnit.DAY) }.forEach { day ->
                    if (!event.occursOn(day) || day in event.skippedDates || day in event.completedDates) return@forEach
                    val trigger = day.atStartOfDayIn(timeZone) + (startMinutes - reminderMinutes).minutes
                    if (trigger > lastChecked && trigger <= now) {
                        notifier.showReminder(event, day)
                    }
                }
            }
        }
        prefs.edit().putLong(KEY_LAST_CHECKED, now.toEpochMilliseconds()).apply()
        return Result.success()
    }

    private companion object {
        const val PREFS_NAME = "schedule_reminders"
        const val KEY_LAST_CHECKED = "last_checked_millis"
        val MAX_LOOKBACK = 6.hours
    }
}
