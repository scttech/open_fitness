package com.scttech.android.kotlin.openfitness.data.repository

import com.scttech.android.kotlin.openfitness.domain.model.AlertDestinationType
import com.scttech.android.kotlin.openfitness.domain.model.AppMessage
import kotlinx.coroutines.flow.Flow

/** The short-lived Message Center: copies of sent alerts, expiring [RETENTION_HOURS] after they arrive. */
interface MessageRepository {
    fun observeMessages(profileId: Long): Flow<List<AppMessage>>
    fun observeUnreadCount(profileId: Long): Flow<Int>

    /**
     * Records an alert and returns its message id, or null if one with the same [dedupeKey] was
     * already recorded (so callers can skip re-notifying).
     */
    suspend fun record(
        profileId: Long,
        dedupeKey: String,
        title: String,
        body: String,
        destinationType: AlertDestinationType?,
        destinationId: Long?,
    ): Long?

    suspend fun markRead(id: Long)
    suspend fun markAllRead(profileId: Long)
    suspend fun delete(id: Long)
    suspend fun clear(profileId: Long)
    suspend fun purgeExpired()

    companion object {
        const val RETENTION_HOURS = 48
    }
}
