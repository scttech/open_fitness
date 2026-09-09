package com.scttech.android.kotlin.openfitness.ui.program.list

import com.scttech.android.kotlin.openfitness.domain.model.Program

sealed interface ProgramListUiState {
    data object Loading : ProgramListUiState
    data class Success(val programs: List<Program>) : ProgramListUiState
}
