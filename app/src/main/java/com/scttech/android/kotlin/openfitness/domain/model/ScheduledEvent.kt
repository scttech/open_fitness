package com.scttech.android.kotlin.openfitness.domain.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

/** What a user-scheduled [ScheduledEvent] points at. */
enum class ScheduledEventKind(val displayName: String) {
    WORKOUT("Workout"),
    PROGRAM_SESSION("Program session"),
    /** Only created when a projected test day is moved; not offered in the editor. */
    PROGRAM_TEST("Program test"),
    CUSTOM("Custom"),
}

/**
 * Something the user put on the calendar by hand. One-off when [repeatDays] is empty, otherwise it
 * recurs on those weekdays from [date] until [repeatUntil] (open-ended if null). Per-occurrence
 * state lives on the series: [completedDates] for manual check-offs, [skippedDates] for occurrences
 * removed or moved elsewhere.
 */
data class ScheduledEvent(
    val id: Long = 0L,
    val profileId: Long,
    val kind: ScheduledEventKind,
    val title: String,
    val workoutId: Long? = null,
    val programId: Long? = null,
    val date: LocalDate,
    /** Minutes after midnight, or null for an all-day entry. */
    val startMinutes: Int? = null,
    val notes: String = "",
    /** Minutes before [startMinutes] to notify, or null for no reminder. Needs a start time. */
    val reminderMinutesBefore: Int? = null,
    val repeatDays: Set<DayOfWeek> = emptySet(),
    val repeatUntil: LocalDate? = null,
    val completedDates: Set<LocalDate> = emptySet(),
    val skippedDates: Set<LocalDate> = emptySet(),
) {
    val isRecurring: Boolean get() = repeatDays.isNotEmpty()

    /** True if this event has an occurrence on [day], ignoring [skippedDates]. */
    fun occursOn(day: LocalDate): Boolean {
        if (day < date) return false
        if (!isRecurring) return day == date
        if (repeatUntil != null && day > repeatUntil) return false
        return day.dayOfWeek in repeatDays || day == date
    }
}

/** A program day the user told the calendar not to project (usually because they moved it). */
data class ProgramDaySkip(
    val profileId: Long,
    val programId: Long,
    val date: LocalDate,
)
