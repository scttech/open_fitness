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

/** Posts "time to retest" notifications for programs, one per program id so each can be dismissed independently. */
@Singleton
class RetestReminderNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alertPoster: AlertPoster,
) {

    fun ensureChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Retest reminders",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Reminds you to log a new max-effort test so a program's training prescription stays current."
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    suspend fun showRetestDue(program: Program, date: LocalDate) = alertPoster.post(
        channelId = CHANNEL_ID,
        notificationTag = "retest",
        profileId = program.profileId,
        dedupeKey = "retest:${program.id}:${date.toEpochDays()}",
        title = "Time to retest “${program.name}”",
        body = "Log a new max-effort test to update your training prescription.",
        destinationType = AlertDestinationType.PROGRAM_TEST,
        destinationId = program.id,
    )

    companion object {
        const val CHANNEL_ID = "retest_reminders"
    }
}
