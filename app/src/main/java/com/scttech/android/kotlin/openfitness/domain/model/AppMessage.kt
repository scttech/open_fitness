package com.scttech.android.kotlin.openfitness.domain.model

import kotlinx.datetime.Instant

/** Where tapping an alert (or its Message Center entry) takes the user. */
enum class AlertDestinationType { PROGRAM, PROGRAM_TEST, WORKOUT, CALENDAR }

/**
 * A copy of an alert the app sent, kept briefly in the Message Center so it can still be read
 * after the system notification is dismissed.
 */
data class AppMessage(
    val id: Long = 0L,
    val profileId: Long,
    val title: String,
    val body: String,
    val createdAt: Instant,
    val isRead: Boolean = false,
    val destinationType: AlertDestinationType? = null,
    val destinationId: Long? = null,
)
