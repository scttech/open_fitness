package com.scttech.android.kotlin.openfitness.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.scttech.android.kotlin.openfitness.domain.model.AlertDestinationType
import com.scttech.android.kotlin.openfitness.domain.model.Program
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Posts "today's a workout/test day" notifications for programs. Uses a string tag (rather than a
 * bare int id, as [RetestReminderNotifier] does on its own channel) so a program's workout-day and
 * test-day alerts never collide with each other or with its retest-due reminder.
 */
@Singleton
class ProgramDayAlertNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alertPoster: AlertPoster,
) {

    fun ensureChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Workout & test day alerts",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Lets you know when today is a scheduled workout or test day for one of your programs."
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    suspend fun showWorkoutDayAlert(program: Program, date: LocalDate) = alertPoster.post(
        channelId = CHANNEL_ID,
        notificationTag = TAG_WORKOUT,
        profileId = program.profileId,
        dedupeKey = "program_workout:${program.id}:${date.toEpochDays()}",
        title = "Workout day: “${program.name}”",
        body = "Today's a scheduled workout day for this program.",
        destinationType = AlertDestinationType.PROGRAM,
        destinationId = program.id,
    )

    suspend fun showTestDayAlert(program: Program, date: LocalDate) = alertPoster.post(
        channelId = CHANNEL_ID,
        notificationTag = TAG_TEST,
        profileId = program.profileId,
        dedupeKey = "program_test:${program.id}:${date.toEpochDays()}",
        title = "Test day: “${program.name}”",
        body = "Today's your scheduled retest day - log a new max-effort test.",
        destinationType = AlertDestinationType.PROGRAM_TEST,
        destinationId = program.id,
    )

    companion object {
        const val CHANNEL_ID = "program_day_alerts"
        private const val TAG_WORKOUT = "program_workout_day"
        private const val TAG_TEST = "program_test_day"
    }
}
