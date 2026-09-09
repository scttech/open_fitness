package com.scttech.android.kotlin.openfitness.ui.program.builder

import com.scttech.android.kotlin.openfitness.domain.model.ProgramGoalType

sealed interface ProgramBuilderUiState {
    data object Loading : ProgramBuilderUiState

    data class Loaded(
        val programId: Long,
        val name: String,
        val exerciseId: Long?,
        val exerciseName: String,
        val goalType: ProgramGoalType,
        val goalTarget: String,
        val sessionsPerWeek: String,
        val retestIntervalDays: String,
        val saved: Boolean = false,
    ) : ProgramBuilderUiState {
        val canSave: Boolean get() = name.isNotBlank() && exerciseName.isNotBlank() && goalTarget.toDoubleOrNull() != null
    }
}
