package com.scttech.android.kotlin.openfitness.domain.program

import com.scttech.android.kotlin.openfitness.domain.model.Program
import com.scttech.android.kotlin.openfitness.domain.model.ProgramConfig
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/**
 * Decides when a [Program] is due for a retest, from whichever of its two cadences comes first:
 * [Program.nextTestDueAt] (elapsed time since the last test), or enough completed sessions to
 * match its weekly training cadence stretched over the retest interval - so a user training more
 * often than scheduled gets prompted sooner, instead of waiting out the full week count.
 */
object ProgramRetestPolicy {

    /** How many sessions a [Program]'s own cadence expects between tests, e.g. 3 workout days/week over 2 weeks is 6. */
    fun requiredSessionsPerRetest(program: Program): Int {
        val workoutDaysPerWeek = program.config.weekSchedule.workoutDayCount.coerceAtLeast(1)
        val retestEveryWeeks = program.config.retestEveryWeeks.coerceAtLeast(1)
        return (workoutDaysPerWeek * retestEveryWeeks).coerceAtLeast(1)
    }

    fun isRetestDue(program: Program, completedSessionsSinceLastTest: Int): Boolean {
        val dueByTime = program.lastTestedAt != null &&
            program.nextTestDueAt != null &&
            program.nextTestDueAt <= Clock.System.now()
        val dueBySessionCount = completedSessionsSinceLastTest >= requiredSessionsPerRetest(program)
        return dueByTime || dueBySessionCount
    }

    /**
     * The next time [config]'s retest is due, anchored to the calendar week containing
     * [ProgramConfig.weekSchedule]'s test day (or `from`'s own week if no test day is set), plus
     * [ProgramConfig.retestEveryWeeks] weeks - so testing a little early or late doesn't drift the
     * cadence onto a different weekday.
     */
    fun computeNextTestDueAt(
        config: ProgramConfig,
        from: Instant,
        timeZone: TimeZone = TimeZone.currentSystemDefault(),
    ): Instant {
        val today = from.toLocalDateTime(timeZone).date
        val retestEveryWeeks = config.retestEveryWeeks.coerceAtLeast(1)
        val testDay = config.weekSchedule.testDay
        val anchorDate = if (testDay != null) {
            // DayOfWeek's ordinal runs MONDAY=0..SUNDAY=6, which already matches the ISO weekday order.
            today.plus(testDay.ordinal - today.dayOfWeek.ordinal, DateTimeUnit.DAY)
        } else {
            today
        }
        return anchorDate.plus(retestEveryWeeks, DateTimeUnit.WEEK).atStartOfDayIn(timeZone)
    }
}
