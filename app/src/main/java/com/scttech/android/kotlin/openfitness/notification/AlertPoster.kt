package com.scttech.android.kotlin.openfitness.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.scttech.android.kotlin.openfitness.MainActivity
import com.scttech.android.kotlin.openfitness.data.repository.MessageRepository
import com.scttech.android.kotlin.openfitness.domain.model.AlertDestinationType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Extras carried by an alert's tap intent, read back in [MainActivity] to open the right screen. */
object AlertExtras {
    const val PROFILE_ID = "alert_profile_id"
    const val DESTINATION_TYPE = "alert_destination_type"
    const val DESTINATION_ID = "alert_destination_id"
    const val MESSAGE_ID = "alert_message_id"
}

/**
 * Sends an alert: records it in the Message Center first (so it's there even if notifications are
 * blocked or dismissed), then posts the system notification, whose tap opens its destination.
 */
@Singleton
class AlertPoster @Inject constructor(
    @ApplicationContext private val context: Context,
    private val messageRepository: MessageRepository,
) {

    suspend fun post(
        channelId: String,
        notificationTag: String,
        profileId: Long,
        dedupeKey: String,
        title: String,
        body: String,
        destinationType: AlertDestinationType?,
        destinationId: Long?,
    ) {
        val messageId = messageRepository.record(profileId, dedupeKey, title, body, destinationType, destinationId)
            ?: return // already sent for this day
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            messageId.toInt(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(AlertExtras.PROFILE_ID, profileId)
                putExtra(AlertExtras.MESSAGE_ID, messageId)
                destinationType?.let { putExtra(AlertExtras.DESTINATION_TYPE, it.name) }
                destinationId?.let { putExtra(AlertExtras.DESTINATION_ID, it) }
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()
        NotificationManagerCompat.from(context).notify(notificationTag, messageId.toInt(), notification)
    }
}
