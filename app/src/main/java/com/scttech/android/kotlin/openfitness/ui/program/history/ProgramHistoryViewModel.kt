package com.scttech.android.kotlin.openfitness.ui.program.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.scttech.android.kotlin.openfitness.data.repository.ProgramRepository
import com.scttech.android.kotlin.openfitness.data.repository.ProgramSessionRepository
import com.scttech.android.kotlin.openfitness.domain.model.Program
import com.scttech.android.kotlin.openfitness.domain.model.ProgramSession
import com.scttech.android.kotlin.openfitness.domain.model.ProgramTest
import com.scttech.android.kotlin.openfitness.ui.navigation.ProgramHistoryRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

/** One completed training session's total for the program's exercise (sum of its logged sets). */
data class SessionTotal(val date: LocalDate, val total: Int)

/** One recorded max-effort test result. */
data class TestPoint(val date: LocalDate, val result: Int)

sealed interface ProgramHistoryUiState {
    data object Loading : ProgramHistoryUiState
    data class Success(
        val program: Program,
        val tests: List<TestPoint>,
        val sessionTotals: List<SessionTotal>,
    ) : ProgramHistoryUiState
    data object NotFound : ProgramHistoryUiState
}

@HiltViewModel
class ProgramHistoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    programRepository: ProgramRepository,
    programSessionRepository: ProgramSessionRepository,
) : ViewModel() {

    private val route: ProgramHistoryRoute = savedStateHandle.toRoute()

    val uiState: StateFlow<ProgramHistoryUiState> = combine(
        programRepository.observeProgram(route.programId),
        programRepository.observeTestsForProgram(route.programId),
        programSessionRepository.observeSessionsForProgram(route.programId),
    ) { program, tests, sessions ->
        if (program == null) {
            ProgramHistoryUiState.NotFound
        } else {
            ProgramHistoryUiState.Success(
                program = program,
                tests = tests.toTestPoints(),
                sessionTotals = sessions.toSessionTotals(),
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProgramHistoryUiState.Loading,
    )
}

internal fun List<ProgramTest>.toTestPoints(timeZone: TimeZone = TimeZone.currentSystemDefault()): List<TestPoint> =
    sortedBy { it.testedAt }.map { TestPoint(it.testedAt.toLocalDateTime(timeZone).date, it.result) }

/** Completed sessions only, oldest first, each summing every logged set's value. */
internal fun List<ProgramSession>.toSessionTotals(timeZone: TimeZone = TimeZone.currentSystemDefault()): List<SessionTotal> =
    filter { it.isCompleted }
        .sortedBy { it.startedAt }
        .map { session ->
            SessionTotal(
                date = session.startedAt.toLocalDateTime(timeZone).date,
                total = session.loggedSets.sumOf { it.reps ?: 0 },
            )
        }
