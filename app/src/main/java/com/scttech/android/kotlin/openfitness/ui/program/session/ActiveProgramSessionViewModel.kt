package com.scttech.android.kotlin.openfitness.ui.program.session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.scttech.android.kotlin.openfitness.data.repository.ProfileRepository
import com.scttech.android.kotlin.openfitness.data.repository.ProgramRepository
import com.scttech.android.kotlin.openfitness.data.repository.ProgramSessionRepository
import com.scttech.android.kotlin.openfitness.domain.model.PerformedSet
import com.scttech.android.kotlin.openfitness.domain.model.Program
import com.scttech.android.kotlin.openfitness.domain.model.ProgramSession
import com.scttech.android.kotlin.openfitness.ui.navigation.ActiveProgramSessionRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import javax.inject.Inject

@HiltViewModel
class ActiveProgramSessionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val programRepository: ProgramRepository,
    private val programSessionRepository: ProgramSessionRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val route: ActiveProgramSessionRoute = savedStateHandle.toRoute()
    private val startedAt = Clock.System.now()
    private lateinit var program: Program

    private val _uiState = MutableStateFlow<ActiveProgramSessionUiState>(ActiveProgramSessionUiState.Loading)
    val uiState: StateFlow<ActiveProgramSessionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val loaded = programRepository.observeProgram(route.programId).first() ?: return@launch
            program = loaded
            _uiState.value = ActiveProgramSessionUiState.InProgress(
                program = loaded,
                loggedSets = emptyList(),
                valueInput = loaded.currentPrescription?.sets?.firstOrNull()?.targetValue?.toString().orEmpty(),
            )
        }
    }

    fun updateValueInput(value: String) {
        _uiState.update { (it as? ActiveProgramSessionUiState.InProgress)?.copy(valueInput = value) ?: it }
    }

    fun logSet() {
        val state = _uiState.value as? ActiveProgramSessionUiState.InProgress ?: return
        val newSet = PerformedSet(
            exerciseName = state.program.exerciseName,
            setIndex = state.loggedSets.size,
            reps = state.valueInput.toIntOrNull(),
        )
        val loggedSets = state.loggedSets + newSet
        val nextTarget = state.program.currentPrescription?.sets?.getOrNull(loggedSets.size)?.targetValue
        _uiState.update {
            state.copy(
                loggedSets = loggedSets,
                valueInput = nextTarget?.toString() ?: state.valueInput,
            )
        }
    }

    fun finishSession() {
        val state = _uiState.value as? ActiveProgramSessionUiState.InProgress ?: return
        _uiState.update { state.copy(isFinished = true) }
        viewModelScope.launch {
            val profileId = profileRepository.currentProfileId.filterNotNull().first()
            programSessionRepository.startSession(
                ProgramSession(
                    profileId = profileId,
                    programId = state.program.id,
                    programName = state.program.name,
                    startedAt = startedAt,
                    completedAt = Clock.System.now(),
                    loggedSets = state.loggedSets,
                ),
            )
        }
    }
}
