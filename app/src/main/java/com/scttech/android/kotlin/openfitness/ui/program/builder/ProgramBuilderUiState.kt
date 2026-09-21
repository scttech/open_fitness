package com.scttech.android.kotlin.openfitness.ui.program.builder

import com.scttech.android.kotlin.openfitness.domain.model.ProgramGoalType
import com.scttech.android.kotlin.openfitness.domain.model.RepStrategy

sealed interface ProgramBuilderUiState {
    data object Loading : ProgramBuilderUiState

    data class Loaded(
        val programId: Long,
        val name: String,
        val exerciseId: Long?,
        val exerciseName: String,
        val goalType: ProgramGoalType,
        val goalTarget: String,
        val repStrategy: RepStrategy,
        /** Only meaningful (and edited) when [repStrategy] is [RepStrategy.CUSTOM_MANUAL]. */
        val customSetTargets: List<String> = emptyList(),
        val sessionsPerWeek: String,
        val retestIntervalDays: String,
        val restSeconds: String,
        val saved: Boolean = false,
    ) : ProgramBuilderUiState {
        val canSave: Boolean
            get() {
                val baseValid = name.isNotBlank() && exerciseName.isNotBlank() && (goalTarget.toIntOrNull() ?: 0) > 0
                val customSetsValid = repStrategy != RepStrategy.CUSTOM_MANUAL ||
                    (customSetTargets.isNotEmpty() && customSetTargets.all { (it.toIntOrNull() ?: 0) > 0 })
                return baseValid && customSetsValid
            }
    }
}
