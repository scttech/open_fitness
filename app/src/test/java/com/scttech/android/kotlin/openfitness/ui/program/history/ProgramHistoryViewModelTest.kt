package com.scttech.android.kotlin.openfitness.ui.program.history

import com.scttech.android.kotlin.openfitness.domain.model.PerformedSet
import com.scttech.android.kotlin.openfitness.domain.model.ProgramSession
import com.scttech.android.kotlin.openfitness.domain.model.ProgramTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgramHistoryViewModelTest {

    private val utc = TimeZone.UTC

    private fun session(start: String, completed: Boolean, vararg reps: Int?) = ProgramSession(
        profileId = 1,
        programId = 1,
        programName = "100 Push-Ups",
        startedAt = Instant.parse(start),
        completedAt = if (completed) Instant.parse(start) else null,
        loggedSets = reps.mapIndexed { i, r -> PerformedSet(exerciseName = "Push-Up", setIndex = i, reps = r) },
    )

    @Test
    fun `session totals sum logged sets, skip incomplete sessions, and sort oldest first`() {
        val totals = listOf(
            session("2026-10-08T10:00:00Z", true, 15, 17, null),
            session("2026-10-01T10:00:00Z", true, 10, 12),
            session("2026-10-05T10:00:00Z", false, 99),
        ).toSessionTotals(utc)

        assertEquals(
            listOf(
                SessionTotal(LocalDate(2026, 10, 1), 22),
                SessionTotal(LocalDate(2026, 10, 8), 32),
            ),
            totals,
        )
    }

    @Test
    fun `test points are sorted oldest first`() {
        val points = listOf(
            ProgramTest(programId = 1, profileId = 1, testedAt = Instant.parse("2026-10-09T10:00:00Z"), result = 32),
            ProgramTest(programId = 1, profileId = 1, testedAt = Instant.parse("2026-10-02T10:00:00Z"), result = 28),
        ).toTestPoints(utc)

        assertEquals(listOf(TestPoint(LocalDate(2026, 10, 2), 28), TestPoint(LocalDate(2026, 10, 9), 32)), points)
    }
}
