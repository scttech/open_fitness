package com.scttech.android.kotlin.openfitness.domain.model

import kotlinx.datetime.LocalDate

enum class CalendarItemType { WORKOUT, PROGRAM_SESSION, PROGRAM_TEST, CUSTOM }

enum class CalendarItemStatus { UPCOMING, DONE, MISSED }

/** One occurrence on a specific day, either scheduled by hand or projected from a program. */
data class CalendarItem(
    val date: LocalDate,
    val startMinutes: Int?,
    val title: String,
    val subtitle: String,
    val type: CalendarItemType,
    val status: CalendarItemStatus,
    /** The backing [ScheduledEvent], or null for items projected from a program's weekly schedule. */
    val eventId: Long? = null,
    val workoutId: Long? = null,
    val programId: Long? = null,
    val isRecurring: Boolean = false,
    val notes: String = "",
) {
    val isDone: Boolean get() = status == CalendarItemStatus.DONE
}
