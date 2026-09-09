package com.scttech.android.kotlin.openfitness.ui.program.session

import com.scttech.android.kotlin.openfitness.domain.model.PerformedSet
import com.scttech.android.kotlin.openfitness.domain.model.Program

sealed interface ActiveProgramSessionUiState {
    data object Loading : ActiveProgramSessionUiState

    data class InProgress(
        val program: Program,
        val loggedSets: List<PerformedSet>,
        val valueInput: String,
        val isFinished: Boolean = false,
    ) : ActiveProgramSessionUiState {
        val nextSetTarget: Int? get() = program.currentPrescription?.sets?.getOrNull(loggedSets.size)?.targetValue
    }
}
