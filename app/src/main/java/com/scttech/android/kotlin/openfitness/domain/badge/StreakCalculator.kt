package com.scttech.android.kotlin.openfitness.domain.badge

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

/** Longest-run calculations over the dates a profile was active. Weeks run Monday to Sunday. */
object StreakCalculator {

    /** The most consecutive calendar days that all appear in [activeDates]. */
    fun longestDayStreak(activeDates: Collection<LocalDate>): Int =
        longestRun(activeDates.map { it.toEpochDays() }.toSet())

    /** The most consecutive calendar weeks that each contain at least one of [activeDates]. */
    fun longestWeekStreak(activeDates: Collection<LocalDate>): Int =
        longestRun(activeDates.map { weekStart(it).toEpochDays() / 7 }.toSet())

    private fun weekStart(date: LocalDate): LocalDate =
        date.minus(date.dayOfWeek.ordinal, DateTimeUnit.DAY)

    private fun longestRun(values: Set<Int>): Int {
        var longest = 0
        for (value in values) {
            if (value - 1 in values) continue // not the start of a run
            var length = 1
            while (value + length in values) length++
            longest = maxOf(longest, length)
        }
        return longest
    }
}
