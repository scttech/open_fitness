package com.scttech.android.kotlin.openfitness.domain.program

import com.scttech.android.kotlin.openfitness.domain.model.Program
import com.scttech.android.kotlin.openfitness.domain.model.ProgramConfig
import com.scttech.android.kotlin.openfitness.domain.model.ProgramDayType
import com.scttech.android.kotlin.openfitness.domain.model.ProgramGoalType
import com.scttech.android.kotlin.openfitness.domain.model.ProgramWeekSchedule
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.Clock
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.days

class ProgramRetestPolicyTest {

    private val timeZone = TimeZone.UTC

    private fun program(config: ProgramConfig, lastTestedAt: Instant? = null, nextTestDueAt: Instant? = null) = Program(
        profileId = 1L,
        name = "Push-ups",
        exerciseId = 1L,
        exerciseName = "Push-ups",
        goalType = ProgramGoalType.REPS,
        goalTarget = 100,
        config = config,
        lastTestedAt = lastTestedAt,
        nextTestDueAt = nextTestDueAt,
        createdAt = Instant.fromEpochMilliseconds(0),
    )

    private fun weekScheduleWith(workoutDays: Int, testDay: DayOfWeek? = null): ProgramWeekSchedule {
        var schedule = ProgramWeekSchedule(
            monday = ProgramDayType.REST, tuesday = ProgramDayType.REST, wednesday = ProgramDayType.REST,
            thursday = ProgramDayType.REST, friday = ProgramDayType.REST, saturday = ProgramDayType.REST, sunday = ProgramDayType.REST,
        )
        DayOfWeek.entries.take(workoutDays).forEach { schedule = schedule.with(it, ProgramDayType.WORKOUT) }
        testDay?.let { schedule = schedule.with(it, ProgramDayType.TEST) }
        return schedule
    }

    @Test
    fun `requiredSessionsPerRetest multiplies workout days by retest cadence`() {
        val config = ProgramConfig(weekSchedule = weekScheduleWith(workoutDays = 3), retestEveryWeeks = 2)

        assertEquals(6, ProgramRetestPolicy.requiredSessionsPerRetest(program(config)))
    }

    @Test
    fun `requiredSessionsPerRetest coerces zero workout days up to one`() {
        val config = ProgramConfig(weekSchedule = weekScheduleWith(workoutDays = 0), retestEveryWeeks = 1)

        assertEquals(1, ProgramRetestPolicy.requiredSessionsPerRetest(program(config)))
    }

    @Test
    fun `isRetestDue is true once nextTestDueAt has passed`() {
        val config = ProgramConfig(weekSchedule = weekScheduleWith(workoutDays = 3))
        val now = Instant.fromEpochMilliseconds(0)
        val program = program(config, lastTestedAt = now, nextTestDueAt = now - 1.days)

        assertTrue(ProgramRetestPolicy.isRetestDue(program, completedSessionsSinceLastTest = 0))
    }

    @Test
    fun `isRetestDue is true once enough sessions are logged even before nextTestDueAt`() {
        val config = ProgramConfig(weekSchedule = weekScheduleWith(workoutDays = 3), retestEveryWeeks = 1)
        val now = Clock.System.now()
        val program = program(config, lastTestedAt = now, nextTestDueAt = now + 30.days)

        assertTrue(ProgramRetestPolicy.isRetestDue(program, completedSessionsSinceLastTest = 3))
    }

    @Test
    fun `isRetestDue is false when neither cadence has elapsed`() {
        val config = ProgramConfig(weekSchedule = weekScheduleWith(workoutDays = 3), retestEveryWeeks = 1)
        val now = Clock.System.now()
        val program = program(config, lastTestedAt = now, nextTestDueAt = now + 30.days)

        assertFalse(ProgramRetestPolicy.isRetestDue(program, completedSessionsSinceLastTest = 1))
    }

    @Test
    fun `computeNextTestDueAt lands on the same test weekday regardless of which day the test was recorded`() {
        val config = ProgramConfig(weekSchedule = weekScheduleWith(workoutDays = 3, testDay = DayOfWeek.WEDNESDAY), retestEveryWeeks = 2)

        // A Monday 2026-01-05 and a Thursday 2026-01-08 fall in the same calendar week.
        val fromMonday = LocalDate(2026, 1, 5).atStartOfDayIn(timeZone)
        val fromThursday = LocalDate(2026, 1, 8).atStartOfDayIn(timeZone)

        val dueFromMonday = ProgramRetestPolicy.computeNextTestDueAt(config, fromMonday, timeZone)
        val dueFromThursday = ProgramRetestPolicy.computeNextTestDueAt(config, fromThursday, timeZone)

        assertEquals(dueFromMonday, dueFromThursday)
        assertEquals(DayOfWeek.WEDNESDAY, dueFromMonday.toLocalDateTime(timeZone).date.dayOfWeek)
    }

    @Test
    fun `computeNextTestDueAt falls back to today's week when no test day is configured`() {
        val config = ProgramConfig(weekSchedule = weekScheduleWith(workoutDays = 3), retestEveryWeeks = 1)
        val from = LocalDate(2026, 1, 6).atStartOfDayIn(timeZone)

        val dueAt = ProgramRetestPolicy.computeNextTestDueAt(config, from, timeZone)

        assertEquals(LocalDate(2026, 1, 13), dueAt.toLocalDateTime(timeZone).date)
    }
}
