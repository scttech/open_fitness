package com.scttech.android.kotlin.openfitness.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scttech.android.kotlin.openfitness.data.repository.ProfileRepository
import com.scttech.android.kotlin.openfitness.data.repository.SessionRepository
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

private const val WEEK_COUNT = 8

data class WeekBucket(val weekStart: LocalDate, val sessionCount: Int)

sealed interface StatsUiState {
    data object Loading : StatsUiState
    data class Success(
        val weeklyBuckets: List<WeekBucket>,
        val totalSessions: Int,
        val sessionsByStyle: Map<String, Int>,
    ) : StatsUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StatsViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    profileRepository: ProfileRepository,
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = profileRepository.currentProfileId
        .filterNotNull()
        .flatMapLatest { profileId -> sessionRepository.observeSessionsForProfile(profileId) }
        .map<List<WorkoutSession>, StatsUiState> { sessions -> buildStats(sessions) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StatsUiState.Loading,
        )

    private fun buildStats(sessions: List<WorkoutSession>): StatsUiState.Success {
        val timeZone = TimeZone.currentSystemDefault()
        val today = Clock.System.todayIn(timeZone)
        val thisWeekStart = today.minus(today.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)
        val weekStarts = (WEEK_COUNT - 1 downTo 0).map { thisWeekStart.minus(it * 7, DateTimeUnit.DAY) }

        val counts = sessions
            .filter { it.isCompleted }
            .groupingBy { session ->
                val date = session.startedAt.toLocalDateTime(timeZone).date
                val weekStart = date.minus(date.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)
                weekStarts.lastOrNull { it <= weekStart && weekStart < it.plus(7, DateTimeUnit.DAY) } ?: weekStarts.first()
            }
            .eachCount()

        val buckets = weekStarts.map { weekStart -> WeekBucket(weekStart, counts[weekStart] ?: 0) }

        return StatsUiState.Success(
            weeklyBuckets = buckets,
            totalSessions = sessions.count { it.isCompleted },
            sessionsByStyle = sessions.filter { it.isCompleted }.groupingBy { it.style.displayName }.eachCount(),
        )
    }
}

private val DayOfWeek.isoDayNumber: Int get() = this.ordinal + 1
