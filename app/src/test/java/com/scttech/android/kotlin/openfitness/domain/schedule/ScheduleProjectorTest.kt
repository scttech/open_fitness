package com.scttech.android.kotlin.openfitness.domain.schedule

import com.scttech.android.kotlin.openfitness.domain.model.CalendarItemStatus
import com.scttech.android.kotlin.openfitness.domain.model.CalendarItemType
import com.scttech.android.kotlin.openfitness.domain.model.PerformedSet
import com.scttech.android.kotlin.openfitness.domain.model.Program
import com.scttech.android.kotlin.openfitness.domain.model.ProgramConfig
import com.scttech.android.kotlin.openfitness.domain.model.ProgramDaySkip
import com.scttech.android.kotlin.openfitness.domain.model.ProgramDayType
import com.scttech.android.kotlin.openfitness.domain.model.ProgramGoalType
import com.scttech.android.kotlin.openfitness.domain.model.ProgramSession
import com.scttech.android.kotlin.openfitness.domain.model.ProgramWeekSchedule
import com.scttech.android.kotlin.openfitness.domain.model.ScheduledEvent
import com.scttech.android.kotlin.openfitness.domain.model.ScheduledEventKind
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduleProjectorTest {

    private val tz = TimeZone.UTC

    // 2026-10-12 is a Monday.
    private val monday = LocalDate(2026, 10, 12)
    private val sunday = LocalDate(2026, 10, 18)
    private val nextMonday = LocalDate(2026, 10, 19)

    private fun program(
        schedule: ProgramWeekSchedule = ProgramWeekSchedule(
            monday = ProgramDayType.WORKOUT,
            tuesday = ProgramDayType.REST,
            wednesday = ProgramDayType.WORKOUT,
            thursday = ProgramDayType.REST,
            friday = ProgramDayType.TEST,
            saturday = ProgramDayType.REST,
            sunday = ProgramDayType.REST,
        ),
        retestEveryWeeks: Int = 1,
    ) = Program(
        id = 1,
        profileId = 1,
        name = "Push-ups",
        exerciseId = null,
        exerciseName = "Push-up",
        goalType = ProgramGoalType.REPS,
        goalTarget = 100,
        config = ProgramConfig(weekSchedule = schedule, retestEveryWeeks = retestEveryWeeks),
        createdAt = LocalDate(2026, 9, 1).atStartOfDayIn(tz),
    )

    private fun project(inputs: ScheduleInputs, today: LocalDate = monday) =
        ScheduleProjector.project(inputs, monday, sunday, today, tz)

    @Test
    fun `program workout and test days are projected from the weekly schedule`() {
        val items = project(ScheduleInputs(programs = listOf(program())))

        assertEquals(
            listOf(CalendarItemType.PROGRAM_SESSION, CalendarItemType.PROGRAM_SESSION, CalendarItemType.PROGRAM_TEST),
            items.map { it.type },
        )
        assertEquals(listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY), items.map { it.date.dayOfWeek })
    }

    @Test
    fun `retest cadence skips off weeks`() {
        val biweekly = program(retestEveryWeeks = 2)
        // Created 2026-09-01 (week of Aug 31), so test weeks are Sep 14, Sep 28, Oct 12, ... - this week is one.
        assertTrue(project(ScheduleInputs(programs = listOf(biweekly))).any { it.type == CalendarItemType.PROGRAM_TEST })

        val offWeek = ScheduleProjector.project(ScheduleInputs(programs = listOf(biweekly)), nextMonday, nextMonday.let { LocalDate(2026, 10, 25) }, monday, tz)
        assertTrue(offWeek.none { it.type == CalendarItemType.PROGRAM_TEST })
    }

    @Test
    fun `past unfinished days are missed and a logged session marks the day done`() {
        val today = LocalDate(2026, 10, 14)
        val session = ProgramSession(
            profileId = 1,
            programId = 1,
            programName = "Push-ups",
            startedAt = monday.atStartOfDayIn(tz),
            completedAt = monday.atStartOfDayIn(tz),
            loggedSets = emptyList<PerformedSet>(),
        )
        val items = project(ScheduleInputs(programs = listOf(program()), programSessions = listOf(session)), today)
            .filter { it.type == CalendarItemType.PROGRAM_SESSION }

        assertEquals(CalendarItemStatus.DONE, items.first { it.date == monday }.status)
        assertEquals(CalendarItemStatus.UPCOMING, items.first { it.date == LocalDate(2026, 10, 14) }.status)
    }

    @Test
    fun `skipped program day disappears`() {
        val skip = ProgramDaySkip(profileId = 1, programId = 1, date = monday)
        val items = project(ScheduleInputs(programs = listOf(program()), programDaySkips = listOf(skip)))

        assertTrue(items.none { it.date == monday })
    }

    @Test
    fun `manual program session replaces the projected one on its day`() {
        val manual = ScheduledEvent(
            profileId = 1, kind = ScheduledEventKind.PROGRAM_SESSION, title = "Push-ups", programId = 1, date = monday,
        )
        val items = project(ScheduleInputs(programs = listOf(program()), events = listOf(manual)))

        assertEquals(1, items.count { it.date == monday })
    }

    @Test
    fun `recurring event expands on its weekdays until its end date and honours skips`() {
        val event = ScheduledEvent(
            profileId = 1,
            kind = ScheduledEventKind.CUSTOM,
            title = "Run",
            date = monday,
            repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY),
            repeatUntil = LocalDate(2026, 10, 22),
            skippedDates = setOf(LocalDate(2026, 10, 15)),
        )
        val items = ScheduleProjector.project(ScheduleInputs(events = listOf(event)), monday, LocalDate(2026, 11, 1), monday, tz)

        assertEquals(listOf(monday, nextMonday, LocalDate(2026, 10, 22)), items.map { it.date })
    }

    @Test
    fun `custom event is done only when checked off`() {
        val event = ScheduledEvent(
            profileId = 1, kind = ScheduledEventKind.CUSTOM, title = "Run", date = monday, completedDates = setOf(monday),
        )
        val item = project(ScheduleInputs(events = listOf(event)), nextMonday).single()

        assertEquals(CalendarItemStatus.DONE, item.status)
    }

    @Test
    fun `streak counts back through fully done days and ignores empty ones`() {
        val events = (0..2).map {
            ScheduledEvent(
                profileId = 1, kind = ScheduledEventKind.CUSTOM, title = "Run",
                date = LocalDate(2026, 10, 12 + it * 2), // Mon, Wed, Fri
                completedDates = setOf(LocalDate(2026, 10, 12 + it * 2)).takeIf { _ -> it != 0 }.orEmpty(),
            )
        }
        val today = LocalDate(2026, 10, 17)
        val items = ScheduleProjector.project(ScheduleInputs(events = events), monday, today, today, tz)

        // Fri and Wed done, Mon missed -> streak of 2.
        assertEquals(2, ScheduleProjector.currentStreak(items, today))
    }
}
