package com.scttech.android.kotlin.openfitness.ui.program.session

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scttech.android.kotlin.openfitness.ui.common.FullScreenLoading
import com.scttech.android.kotlin.openfitness.ui.common.KeepScreenOn
import com.scttech.android.kotlin.openfitness.ui.common.timer.PhaseTimerDisplay
import com.scttech.android.kotlin.openfitness.ui.common.timer.TimerColorPrefs

@Composable
internal fun ActiveProgramSessionRoute(
    onBackClick: () -> Unit,
    onFinished: () -> Unit,
    onExerciseClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActiveProgramSessionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val timerColors by viewModel.timerColors.collectAsStateWithLifecycle()

    KeepScreenOn()

    ActiveProgramSessionScreen(
        uiState = uiState,
        timerColors = timerColors,
        onBackClick = onBackClick,
        onValueInputChange = viewModel::updateValueInput,
        onLogSet = viewModel::logSet,
        onFinish = viewModel::finishSession,
        onToggleRestRunning = viewModel::toggleRestRunning,
        onSkipRest = viewModel::skipRest,
        onDone = onFinished,
        onExerciseClick = onExerciseClick,
        modifier = modifier,
    )
}

@Composable
internal fun ActiveProgramSessionScreen(
    uiState: ActiveProgramSessionUiState,
    timerColors: TimerColorPrefs,
    onBackClick: () -> Unit,
    onValueInputChange: (String) -> Unit,
    onLogSet: () -> Unit,
    onFinish: () -> Unit,
    onToggleRestRunning: () -> Unit,
    onSkipRest: () -> Unit,
    onDone: () -> Unit,
    onExerciseClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (uiState is ActiveProgramSessionUiState.InProgress) uiState.program.name else "Session") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        when (uiState) {
            ActiveProgramSessionUiState.Loading -> FullScreenLoading(Modifier.padding(padding))
            is ActiveProgramSessionUiState.InProgress -> {
                val restTimerState = uiState.restTimerState
                if (uiState.isFinished) {
                    SessionCompleteContent(
                        message = uiState.completionMessage.orEmpty(),
                        setsLogged = uiState.loggedSets.size,
                        onDone = onDone,
                        modifier = Modifier.padding(padding),
                    )
                } else if (restTimerState != null) {
                    Column(
                        modifier = Modifier.padding(padding).fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        PhaseTimerDisplay(
                            state = restTimerState,
                            workColor = timerColors.workColor,
                            restColor = timerColors.restColor,
                            onPauseResume = onToggleRestRunning,
                            onSkip = onSkipRest,
                            skipLabel = "Skip rest",
                            comingUpLabel = uiState.program.exerciseName,
                        )
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        val exerciseId = uiState.program.exerciseId
                        Text(
                            uiState.program.exerciseName,
                            style = MaterialTheme.typography.headlineSmall,
                            color = if (exerciseId != null) MaterialTheme.colorScheme.primary else Color.Unspecified,
                            modifier = if (exerciseId != null) Modifier.clickable { onExerciseClick(exerciseId) } else Modifier,
                        )
                        val target = uiState.nextSetTarget
                        val totalSets = uiState.totalSets
                        Text(
                            when {
                                target == null -> "Free set - no prescription target"
                                uiState.nextSetIsAmrap -> "Target: $target+ ${uiState.program.goalType.unitLabel} (AMRAP - go until failure)"
                                else -> "Target: $target ${uiState.program.goalType.unitLabel}"
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (totalSets != null) {
                            Text(
                                "Set ${uiState.loggedSets.size + 1} of $totalSets",
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }

                        OutlinedTextField(
                            value = uiState.valueInput,
                            onValueChange = onValueInputChange,
                            label = { Text(uiState.program.goalType.unitLabel.replaceFirstChar { it.uppercase() }) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )

                        Button(onClick = onLogSet, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                if (totalSets != null && uiState.loggedSets.size + 1 >= totalSets) {
                                    "Log final set"
                                } else {
                                    "Log set ${uiState.loggedSets.size + 1}"
                                },
                            )
                        }

                        Text("Logged sets", style = MaterialTheme.typography.titleLarge)
                        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(uiState.loggedSets) { set ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Set ${set.setIndex + 1}")
                                        Text("${set.reps ?: 0} ${uiState.program.goalType.unitLabel}")
                                    }
                                }
                            }
                        }

                        OutlinedButton(onClick = onFinish, modifier = Modifier.fillMaxWidth()) {
                            Text("Finish session")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionCompleteContent(
    message: String,
    setsLogged: Int,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Session complete! 🎉", style = MaterialTheme.typography.headlineSmall)
        Text(
            "$setsLogged ${if (setsLogged == 1) "set" else "sets"} logged.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            message,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp, bottom = 32.dp),
        )
        Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
            Text("Done")
        }
    }
}
