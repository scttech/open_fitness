package com.scttech.android.kotlin.openfitness.ui.program.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.scttech.android.kotlin.openfitness.data.repository.ProgramRepository
import com.scttech.android.kotlin.openfitness.domain.model.Program
import com.scttech.android.kotlin.openfitness.domain.model.ProgramTest
import com.scttech.android.kotlin.openfitness.ui.navigation.ProgramDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ProgramDetailUiState {
    data object Loading : ProgramDetailUiState
    data class Success(val program: Program, val tests: List<ProgramTest>) : ProgramDetailUiState
    data object NotFound : ProgramDetailUiState
}

@HiltViewModel
class ProgramDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val programRepository: ProgramRepository,
) : ViewModel() {

    private val route: ProgramDetailRoute = savedStateHandle.toRoute()

    val uiState: StateFlow<ProgramDetailUiState> = combine(
        programRepository.observeProgram(route.programId),
        programRepository.observeTestsForProgram(route.programId),
    ) { program, tests ->
        if (program != null) ProgramDetailUiState.Success(program, tests) else ProgramDetailUiState.NotFound
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProgramDetailUiState.Loading,
        )

    private val _deleted = MutableSharedFlow<Unit>()
    val deleted: SharedFlow<Unit> = _deleted

    fun deleteProgram() {
        val state = uiState.value
        if (state !is ProgramDetailUiState.Success) return
        viewModelScope.launch {
            programRepository.deleteProgram(state.program)
            _deleted.emit(Unit)
        }
    }
}
