package com.scttech.android.kotlin.openfitness.domain.program

import com.scttech.android.kotlin.openfitness.domain.model.Program
import kotlinx.datetime.Clock
import kotlin.math.ceil

/**
 * Decides when a [Program] is due for a retest, from whichever of its two cadences comes first:
 * [Program.nextTestDueAt] (elapsed time since the last test), or enough completed sessions to
 * match its weekly training cadence stretched over the retest interval - so a user training more
 * often than scheduled gets prompted sooner, instead of waiting out the full day count.
 */
object ProgramRetestPolicy {

    /** How many sessions a [Program]'s own cadence expects between tests, e.g. 3/week over a 7-day interval is 3. */
    fun requiredSessionsPerRetest(program: Program): Int {
        val sessionsPerWeek = program.config.sessionsPerWeek.coerceAtLeast(1)
        val retestIntervalDays = program.config.retestIntervalDays.coerceAtLeast(1)
        return ceil(sessionsPerWeek * retestIntervalDays / 7.0).toInt().coerceAtLeast(1)
    }

    fun isRetestDue(program: Program, completedSessionsSinceLastTest: Int): Boolean {
        val dueByTime = program.lastTestedAt != null &&
            program.nextTestDueAt != null &&
            program.nextTestDueAt <= Clock.System.now()
        val dueBySessionCount = completedSessionsSinceLastTest >= requiredSessionsPerRetest(program)
        return dueByTime || dueBySessionCount
    }
}
