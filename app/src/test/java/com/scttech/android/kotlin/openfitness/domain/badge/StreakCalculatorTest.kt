package com.scttech.android.kotlin.openfitness.domain.badge

import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class StreakCalculatorTest {

    private fun d(day: Int, month: Int = 10) = LocalDate(2026, month, day)

    @Test
    fun `no dates gives no streak`() {
        assertEquals(0, StreakCalculator.longestDayStreak(emptyList()))
        assertEquals(0, StreakCalculator.longestWeekStreak(emptyList()))
    }

    @Test
    fun `day streak is the longest consecutive run, ignoring duplicates and order`() {
        val dates = listOf(d(9), d(1), d(2), d(3), d(3), d(5), d(6), d(2))
        assertEquals(3, StreakCalculator.longestDayStreak(dates))
    }

    @Test
    fun `day streak crosses month boundaries`() {
        assertEquals(3, StreakCalculator.longestDayStreak(listOf(d(30, 9), d(1, 10), d(29, 9))))
    }

    @Test
    fun `several sessions in one week count as a single week`() {
        // 2026-10-05 is a Monday; 05-11 is one week.
        assertEquals(1, StreakCalculator.longestWeekStreak(listOf(d(5), d(7), d(11))))
    }

    @Test
    fun `week streak runs across Sunday to Monday and breaks on a skipped week`() {
        // Sun 4th (week of 09-28), Mon 5th (week of 10-05), then skip a week, then Mon 19th.
        assertEquals(2, StreakCalculator.longestWeekStreak(listOf(d(4), d(5), d(19))))
    }

    @Test
    fun `week streak spans a year boundary`() {
        val dates = listOf(LocalDate(2025, 12, 29), LocalDate(2026, 1, 5), LocalDate(2026, 1, 12))
        assertEquals(3, StreakCalculator.longestWeekStreak(dates))
    }
}
