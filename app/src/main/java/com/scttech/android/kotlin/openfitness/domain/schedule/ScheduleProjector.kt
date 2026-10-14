package com.scttech.android.kotlin.openfitness.domain.schedule

import com.scttech.android.kotlin.openfitness.domain.model.CalendarItem
import com.scttech.android.kotlin.openfitness.domain.model.CalendarItemStatus
import com.scttech.android.kotlin.openfitness.domain.model.CalendarItemType
import com.scttech.android.kotlin.openfitness.domain.model.Program
import com.scttech.android.kotlin.openfitness.domain.model.ProgramDaySkip
import com.scttech.android.kotlin.openfitness.domain.model.ProgramDayType
import com.scttech.android.kotlin.openfitness.domain.model.ProgramSession
import com.scttech.android.kotlin.openfitness.domain.model.ProgramTest
import com.scttech.android.kotlin.openfitness.domain.model.ScheduledEvent
import com.scttech.android.kotlin.openfitness.domain.model.ScheduledEventKind
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutSession
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/** Everything the calendar needs to know to turn schedules and history into [CalendarItem]s. */
data class ScheduleInputs(
    val events: List<ScheduledEvent> = emptyList(),
    val programs: List<Program> = emptyList(),
    val programTests: List<ProgramTest> = emptyList(),
    val programSessions: List<ProgramSession> = emptyList(),
    val workoutSessions: List<WorkoutSession> = emptyList(),
    val programDaySkips: List<ProgramDaySkip> = emptyList(),
)

/**
 * Pure logic behind the calendar: expands recurring events, projects each program's weekly
 * schedule and retest cadence, and marks every occurrence done, missed or upcoming against what
 * was actually logged.
 */
object ScheduleProjector {

    fun project(
        inputs: ScheduleInputs,
        from: LocalDate,
        to: LocalDate,
        today: LocalDate,
        timeZone: TimeZone = TimeZone.currentSystemDefault(),
    ): List<CalendarItem> {
        if (to < from) return emptyList()
        val completedWorkoutDays = inputs.workoutSessions
            .filter { it.isCompleted }
            .map { it.workoutId to it.startedAt.toLocalDateTime(timeZone).date }
            .toSet()
        val completedProgramDays = inputs.programSessions
            .filter { it.isCompleted }
            .map { it.programId to it.startedAt.toLocalDateTime(timeZone).date }
            .toSet()
        val testDays = inputs.programTests
            .map { it.programId to it.testedAt.toLocalDateTime(timeZone).date }
            .toSet()
        val programsById = inputs.programs.associateBy { it.id }

        fun statusFor(done: Boolean, day: LocalDate) = when {
            done -> CalendarItemStatus.DONE
            day < today -> CalendarItemStatus.MISSED
            else -> CalendarItemStatus.UPCOMING
        }

        val items = mutableListOf<CalendarItem>()
        val days = generateSequence(from) { it.plus(1, DateTimeUnit.DAY) }.takeWhile { it <= to }.toList()

        // Manual events (including expanded recurrences).
        val manualProgramDays = mutableSetOf<Pair<Long, LocalDate>>()
        val manualTestDays = mutableSetOf<Pair<Long, LocalDate>>()
        for (event in inputs.events) {
            for (day in days) {
                if (!event.occursOn(day) || day in event.skippedDates) continue
                val done = when (event.kind) {
                    ScheduledEventKind.WORKOUT -> (event.workoutId?.let { (it to day) in completedWorkoutDays } ?: false) ||
                        day in event.completedDates
                    ScheduledEventKind.PROGRAM_SESSION -> (event.programId?.let { (it to day) in completedProgramDays } ?: false) ||
                        day in event.completedDates
                    ScheduledEventKind.PROGRAM_TEST -> (event.programId?.let { (it to day) in testDays } ?: false) ||
                        day in event.completedDates
                    ScheduledEventKind.CUSTOM -> day in event.completedDates
                }
                if (event.programId != null) {
                    when (event.kind) {
                        ScheduledEventKind.PROGRAM_SESSION -> manualProgramDays += event.programId to day
                        ScheduledEventKind.PROGRAM_TEST -> manualTestDays += event.programId to day
                        else -> Unit
                    }
                }
                items += CalendarItem(
                    date = day,
                    startMinutes = event.startMinutes,
                    title = event.title,
                    subtitle = when (event.kind) {
                        ScheduledEventKind.WORKOUT -> "Workout"
                        ScheduledEventKind.PROGRAM_SESSION -> "Program session"
                        ScheduledEventKind.PROGRAM_TEST -> "Program test"
                        ScheduledEventKind.CUSTOM -> "Custom"
                    },
                    type = when (event.kind) {
                        ScheduledEventKind.WORKOUT -> CalendarItemType.WORKOUT
                        ScheduledEventKind.PROGRAM_SESSION -> CalendarItemType.PROGRAM_SESSION
                        ScheduledEventKind.PROGRAM_TEST -> CalendarItemType.PROGRAM_TEST
                        ScheduledEventKind.CUSTOM -> CalendarItemType.CUSTOM
                    },
                    status = statusFor(done, day),
                    eventId = event.id,
                    workoutId = event.workoutId,
                    programId = event.programId,
                    isRecurring = event.isRecurring,
                    notes = event.notes,
                )
            }
        }

        // Projected program days.
        val skips = inputs.programDaySkips.map { it.programId to it.date }.toSet()
        for (program in programsById.values) {
            if (program.isArchived) continue
            val createdDay = program.createdAt.toLocalDateTime(timeZone).date
            val schedule = program.config.weekSchedule
            val testWeekStarts = testWeekStarts(program, timeZone)
            for (day in days) {
                if (day < createdDay) continue
                val key = program.id to day
                if (key in skips) continue
                when (schedule[day.dayOfWeek]) {
                    ProgramDayType.WORKOUT -> {
                        if (key in manualProgramDays) continue
                        items += CalendarItem(
                            date = day,
                            startMinutes = null,
                            title = program.name,
                            subtitle = "Program workout · ${program.exerciseName}",
                            type = CalendarItemType.PROGRAM_SESSION,
                            status = statusFor(key in completedProgramDays, day),
                            programId = program.id,
                        )
                    }
                    ProgramDayType.TEST -> {
                        val weekStart = day.plus(-day.dayOfWeek.ordinal, DateTimeUnit.DAY)
                        val tested = key in testDays
                        if (key in manualTestDays) continue
                        if (tested || weekStart in testWeekStarts) {
                            items += CalendarItem(
                                date = day,
                                startMinutes = null,
                                title = "Test: ${program.name}",
                                subtitle = "Max-effort ${program.exerciseName} test",
                                type = CalendarItemType.PROGRAM_TEST,
                                status = statusFor(tested, day),
                                programId = program.id,
                            )
                        }
                    }
                    ProgramDayType.REST -> Unit
                }
            }
            // Tests recorded on days the schedule doesn't call a test day still belong on the calendar.
            for (day in days) {
                val key = program.id to day
                if (key in testDays && key !in manualTestDays && schedule[day.dayOfWeek] != ProgramDayType.TEST) {
                    items += CalendarItem(
                        date = day,
                        startMinutes = null,
                        title = "Test: ${program.name}",
                        subtitle = "Max-effort ${program.exerciseName} test",
                        type = CalendarItemType.PROGRAM_TEST,
                        status = CalendarItemStatus.DONE,
                        programId = program.id,
                    )
                }
            }
        }

        return items.sortedWith(
            compareBy<CalendarItem> { it.date }
                .thenBy { it.startMinutes ?: Int.MAX_VALUE }
                .thenBy { it.title },
        )
    }

    /**
     * Mondays of the weeks a program's test day recurs in: every `retestEveryWeeks` weeks after the
     * week of the last test (or creation, if never tested), matching
     * [com.scttech.android.kotlin.openfitness.domain.program.ProgramRetestPolicy.computeNextTestDueAt].
     */
    private fun testWeekStarts(program: Program, timeZone: TimeZone): Set<LocalDate> {
        val anchor = (program.lastTestedAt ?: program.createdAt).toLocalDateTime(timeZone).date
        val anchorWeekStart = anchor.plus(-anchor.dayOfWeek.ordinal, DateTimeUnit.DAY)
        val step = program.config.retestEveryWeeks.coerceAtLeast(1)
        return (1..MAX_PROJECTED_RETESTS).map { anchorWeekStart.plus(it * step, DateTimeUnit.WEEK) }.toSet()
    }

    /**
     * Consecutive scheduled days, counting back from [today], on which everything scheduled got
     * done. Days with nothing scheduled neither extend nor break the streak, and today only counts
     * once it's fully done (an unfinished today doesn't break it yet).
     */
    fun currentStreak(items: List<CalendarItem>, today: LocalDate): Int {
        val byDay = items.groupBy { it.date }
        var streak = 0
        var day = today
        val earliest = items.minOfOrNull { it.date } ?: return 0
        while (day >= earliest) {
            val dayItems = byDay[day]
            if (dayItems != null) {
                if (dayItems.all { it.isDone }) {
                    streak++
                } else if (day != today) {
                    break
                }
            }
            day = day.plus(-1, DateTimeUnit.DAY)
        }
        return streak
    }

    private const val MAX_PROJECTED_RETESTS = 260
}
