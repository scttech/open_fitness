package com.scttech.android.kotlin.openfitness.ui.program.session

import com.scttech.android.kotlin.openfitness.domain.model.PerformedSet
import com.scttech.android.kotlin.openfitness.domain.model.Program
import com.scttech.android.kotlin.openfitness.ui.common.timer.PhaseTimerState

sealed interface ActiveProgramSessionUiState {
    data object Loading : ActiveProgramSessionUiState

    data class InProgress(
        val program: Program,
        val loggedSets: List<PerformedSet>,
        val valueInput: String,
        val isFinished: Boolean = false,
        /** Set once [isFinished], so the screen can send the user off on a high note. */
        val completionMessage: String? = null,
        /** Non-null while resting between sets. */
        val restTimerState: PhaseTimerState? = null,
    ) : ActiveProgramSessionUiState {
        val totalSets: Int? get() = program.currentPrescription?.sets?.size
        val nextSetTarget: Int? get() = program.currentPrescription?.sets?.getOrNull(loggedSets.size)?.targetValue
        val nextSetIsAmrap: Boolean get() = program.currentPrescription?.sets?.getOrNull(loggedSets.size)?.isAmrap == true
    }
}
