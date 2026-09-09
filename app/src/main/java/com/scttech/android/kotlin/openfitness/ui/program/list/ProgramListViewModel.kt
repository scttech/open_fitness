package com.scttech.android.kotlin.openfitness.ui.program.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scttech.android.kotlin.openfitness.data.repository.ProfileRepository
import com.scttech.android.kotlin.openfitness.data.repository.ProgramRepository
import com.scttech.android.kotlin.openfitness.domain.model.Program
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProgramListViewModel @Inject constructor(
    private val programRepository: ProgramRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    val uiState: StateFlow<ProgramListUiState> = profileRepository.currentProfileId
        .filterNotNull()
        .flatMapLatest { profileId -> programRepository.observeProgramsForProfile(profileId) }
        .map<List<Program>, ProgramListUiState> { ProgramListUiState.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProgramListUiState.Loading,
        )

    fun deleteProgram(program: Program) {
        viewModelScope.launch {
            programRepository.deleteProgram(program)
        }
    }
}
