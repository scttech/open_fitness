package com.scttech.android.kotlin.openfitness.ui.program.testcheckin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.scttech.android.kotlin.openfitness.data.repository.ProgramRepository
import com.scttech.android.kotlin.openfitness.domain.model.Program
import com.scttech.android.kotlin.openfitness.ui.navigation.ProgramTestCheckInRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ProgramTestCheckInUiState {
    data object Loading : ProgramTestCheckInUiState

    data class Loaded(
        val program: Program,
        val resultInput: String = "",
        val notesInput: String = "",
        val saved: Boolean = false,
    ) : ProgramTestCheckInUiState {
        val canSave: Boolean get() = resultInput.toIntOrNull() != null
    }
}

@HiltViewModel
class ProgramTestCheckInViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val programRepository: ProgramRepository,
) : ViewModel() {

    private val route: ProgramTestCheckInRoute = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow<ProgramTestCheckInUiState>(ProgramTestCheckInUiState.Loading)
    val uiState: StateFlow<ProgramTestCheckInUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val program = programRepository.observeProgram(route.programId).first() ?: return@launch
            _uiState.value = ProgramTestCheckInUiState.Loaded(program = program)
        }
    }

    fun updateResult(value: String) = updateLoaded { copy(resultInput = value) }
    fun updateNotes(value: String) = updateLoaded { copy(notesInput = value) }

    private inline fun updateLoaded(block: ProgramTestCheckInUiState.Loaded.() -> ProgramTestCheckInUiState.Loaded) {
        val current = _uiState.value
        if (current is ProgramTestCheckInUiState.Loaded) {
            _uiState.update { current.block() }
        }
    }

    fun save() {
        val state = _uiState.value
        if (state !is ProgramTestCheckInUiState.Loaded || !state.canSave) return
        val result = state.resultInput.toIntOrNull() ?: return
        viewModelScope.launch {
            programRepository.recordTest(state.program.id, result, state.notesInput.trim())
            _uiState.update { (it as ProgramTestCheckInUiState.Loaded).copy(saved = true) }
        }
    }
}
