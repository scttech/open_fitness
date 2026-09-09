package com.scttech.android.kotlin.openfitness.ui.exercise.builder

import com.scttech.android.kotlin.openfitness.domain.model.ExerciseCategory
import com.scttech.android.kotlin.openfitness.domain.model.ExerciseEquipment

sealed interface ExerciseBuilderUiState {
    data object Loading : ExerciseBuilderUiState

    data class Loaded(
        val exerciseId: Long,
        val name: String,
        val category: ExerciseCategory,
        val equipment: ExerciseEquipment,
        val formNotes: String,
        val isCustom: Boolean = true,
        val saved: Boolean = false,
    ) : ExerciseBuilderUiState {
        val canSave: Boolean get() = name.isNotBlank()
    }
}
