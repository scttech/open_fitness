package com.scttech.android.kotlin.openfitness.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.scttech.android.kotlin.openfitness.domain.model.AlertDestinationType
import com.scttech.android.kotlin.openfitness.domain.model.ScheduledEvent
import com.scttech.android.kotlin.openfitness.domain.model.ScheduledEventKind
import com.scttech.android.kotlin.openfitness.ui.calendar.formatMinutes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/** Posts the reminder a user asked for on a [ScheduledEvent]. */
@Singleton
class ScheduleReminderNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alertPoster: AlertPoster,
) {

    fun ensureChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Scheduled workout reminders",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Reminders for workouts and events you've put on your calendar."
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    suspend fun showReminder(event: ScheduledEvent, date: LocalDate) {
        val startMinutes = event.startMinutes
        val whenText = if (startMinutes != null) "Starts at ${formatMinutes(startMinutes)}" else "Scheduled for today"
        val (type, id) = when (event.kind) {
            ScheduledEventKind.WORKOUT ->
                if (event.workoutId != null) AlertDestinationType.WORKOUT to event.workoutId else AlertDestinationType.CALENDAR to null
            ScheduledEventKind.PROGRAM_SESSION ->
                if (event.programId != null) AlertDestinationType.PROGRAM to event.programId else AlertDestinationType.CALENDAR to null
            ScheduledEventKind.PROGRAM_TEST ->
                if (event.programId != null) AlertDestinationType.PROGRAM_TEST to event.programId else AlertDestinationType.CALENDAR to null
            ScheduledEventKind.CUSTOM -> AlertDestinationType.CALENDAR to null
        }
        alertPoster.post(
            channelId = CHANNEL_ID,
            notificationTag = "schedule",
            profileId = event.profileId,
            dedupeKey = "schedule:${event.id}:${date.toEpochDays()}",
            title = event.title,
            body = whenText,
            destinationType = type,
            destinationId = id,
        )
    }

    companion object {
        const val CHANNEL_ID = "schedule_reminders"
    }
}
