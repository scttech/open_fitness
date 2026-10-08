package com.scttech.android.kotlin.openfitness.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.scttech.android.kotlin.openfitness.MainActivity
import com.scttech.android.kotlin.openfitness.domain.model.Program
import dagger.hilt.android.qualifiers.ApplicationContext
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

    fun showWorkoutDayAlert(program: Program) = show(
        tag = TAG_WORKOUT,
        program = program,
        title = "Workout day: “${program.name}”",
        text = "Today's a scheduled workout day for this program.",
    )

    fun showTestDayAlert(program: Program) = show(
        tag = TAG_TEST,
        program = program,
        title = "Test day: “${program.name}”",
        text = "Today's your scheduled retest day - log a new max-effort test.",
    )

    private fun show(tag: String, program: Program, title: String, text: String) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val contentIntent = PendingIntent.getActivity(
            context,
            program.id.toInt(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        NotificationManagerCompat.from(context).notify(tag, program.id.toInt(), notification)
    }

    companion object {
        const val CHANNEL_ID = "program_day_alerts"
        private const val TAG_WORKOUT = "program_workout_day"
        private const val TAG_TEST = "program_test_day"
    }
}
