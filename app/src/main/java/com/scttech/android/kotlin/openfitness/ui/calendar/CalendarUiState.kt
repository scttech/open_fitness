package com.scttech.android.kotlin.openfitness.ui.calendar

import com.scttech.android.kotlin.openfitness.domain.model.CalendarItem
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

enum class CalendarMode(val label: String) { DAY("Day"), WEEK("Week"), MONTH("Month") }

data class CalendarUiState(
    val mode: CalendarMode,
    val selectedDate: LocalDate,
    val today: LocalDate,
    /** Items inside [visibleRange] only. */
    val items: List<CalendarItem>,
    val streak: Int,
    val isLoading: Boolean,
) {
    val visibleRange: ClosedRange<LocalDate> get() = visibleRangeFor(mode, selectedDate)
    val itemsByDate: Map<LocalDate, List<CalendarItem>> get() = items.groupBy { it.date }
}

/** Monday of the week containing [this]. */
internal fun LocalDate.startOfWeek(): LocalDate = plus(-dayOfWeek.ordinal, DateTimeUnit.DAY)

/** The first of the month containing [this]. */
internal fun LocalDate.startOfMonth(): LocalDate = LocalDate(year, month, 1)

/**
 * The days a [mode] needs data for: the day itself, its Monday-to-Sunday week, or the whole month
 * padded out to full weeks so the grid's leading/trailing cells show real items too.
 */
internal fun visibleRangeFor(mode: CalendarMode, date: LocalDate): ClosedRange<LocalDate> = when (mode) {
    CalendarMode.DAY -> date..date
    CalendarMode.WEEK -> date.startOfWeek()..date.startOfWeek().plus(6, DateTimeUnit.DAY)
    CalendarMode.MONTH -> {
        val first = date.startOfMonth()
        val last = first.plus(1, DateTimeUnit.MONTH).plus(-1, DateTimeUnit.DAY)
        first.startOfWeek()..last.startOfWeek().plus(6, DateTimeUnit.DAY)
    }
}
