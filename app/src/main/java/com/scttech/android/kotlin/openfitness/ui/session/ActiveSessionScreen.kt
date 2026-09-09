package com.scttech.android.kotlin.openfitness.ui.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scttech.android.kotlin.openfitness.ui.common.FullScreenLoading

@Composable
internal fun ActiveSessionRoute(
    onBackClick: () -> Unit,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActiveSessionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        val finished = when (val state = uiState) {
            is ActiveSessionUiState.TabataSession -> state.isFinished
            is ActiveSessionUiState.DensitySession -> state.isFinished
            is ActiveSessionUiState.SetLoggingSession -> state.isFinished
            ActiveSessionUiState.Loading -> false
        }
        if (finished) onFinished()
    }

    ActiveSessionScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onToggleTabataRunning = viewModel::toggleTabataRunning,
        onToggleDensityRunning = viewModel::toggleDensityRunning,
        onIncrementDensityRounds = viewModel::incrementDensityRounds,
        onFinishDensity = viewModel::finishDensity,
        onRepsInputChange = viewModel::updateRepsInput,
        onWeightInputChange = viewModel::updateWeightInput,
        onLogSet = viewModel::logSet,
        onFinishSetLogging = viewModel::finishSetLoggingSession,
        modifier = modifier,
    )
}

@Composable
internal fun ActiveSessionScreen(
    uiState: ActiveSessionUiState,
    onBackClick: () -> Unit,
    onToggleTabataRunning: () -> Unit,
    onToggleDensityRunning: () -> Unit,
    onIncrementDensityRounds: () -> Unit,
    onFinishDensity: () -> Unit,
    onRepsInputChange: (String) -> Unit,
    onWeightInputChange: (String) -> Unit,
    onLogSet: () -> Unit,
    onFinishSetLogging: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(sessionTitle(uiState)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        when (uiState) {
            ActiveSessionUiState.Loading -> FullScreenLoading(Modifier.padding(padding))
            is ActiveSessionUiState.TabataSession -> TabataSessionContent(uiState, onToggleTabataRunning, Modifier.padding(padding))
            is ActiveSessionUiState.DensitySession -> DensitySessionContent(
                uiState,
                onToggleDensityRunning,
                onIncrementDensityRounds,
                onFinishDensity,
                Modifier.padding(padding),
            )
            is ActiveSessionUiState.SetLoggingSession -> SetLoggingSessionContent(
                uiState,
                onRepsInputChange,
                onWeightInputChange,
                onLogSet,
                onFinishSetLogging,
                Modifier.padding(padding),
            )
        }
    }
}

private fun sessionTitle(uiState: ActiveSessionUiState): String = when (uiState) {
    is ActiveSessionUiState.TabataSession -> uiState.workoutName
    is ActiveSessionUiState.DensitySession -> uiState.workoutName
    is ActiveSessionUiState.SetLoggingSession -> uiState.workoutName
    ActiveSessionUiState.Loading -> "Session"
}

@Composable
private fun TabataSessionContent(
    state: ActiveSessionUiState.TabataSession,
    onToggleRunning: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = when (state.phase) {
                TabataPhase.WORK -> "WORK"
                TabataPhase.REST -> "REST"
                TabataPhase.ROUND_REST -> "ROUND REST"
                TabataPhase.DONE -> "DONE"
            },
            style = MaterialTheme.typography.headlineSmall,
            color = if (state.phase == TabataPhase.WORK) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = state.secondsRemaining.toString(),
            fontSize = 96.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(16.dp))
        if (state.phase != TabataPhase.DONE) {
            Text(state.currentExerciseName, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text(
                "Round ${state.currentRound}/${state.config.roundsPerCycle}" +
                    if (state.config.cycles > 1) " · Cycle ${state.currentCycle}/${state.config.cycles}" else "",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(32.dp))
            FilledTonalButton(onClick = onToggleRunning) {
                Icon(if (state.isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (state.isRunning) "Pause" else "Start")
            }
        } else {
            Text("Workout complete! 🎉", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun DensitySessionContent(
    state: ActiveSessionUiState.DensitySession,
    onToggleRunning: () -> Unit,
    onIncrementRounds: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Target: ${state.config.durationMinutes} min", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(8.dp))
        Text(formatElapsed(state.elapsedSeconds), fontSize = 64.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        Text("Exercises: ${state.exercises.joinToString(", ")}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        Text("Rounds completed: ${state.roundsCompleted}", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Button(onClick = onIncrementRounds) { Text("+1 round") }
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilledTonalButton(onClick = onToggleRunning) {
                Icon(if (state.isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (state.isRunning) "Pause" else "Start")
            }
            OutlinedButton(onClick = onFinish) { Text("Finish") }
        }
    }
}

@Composable
private fun SetLoggingSessionContent(
    state: ActiveSessionUiState.SetLoggingSession,
    onRepsInputChange: (String) -> Unit,
    onWeightInputChange: (String) -> Unit,
    onLogSet: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(state.exerciseName, style = MaterialTheme.typography.headlineSmall)
        Text(state.targetDescription, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.repsInput,
                onValueChange = onRepsInputChange,
                label = { Text("Reps") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
            OutlinedTextField(
                value = state.weightInput,
                onValueChange = onWeightInputChange,
                label = { Text("Weight (kg, optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
        }

        Button(onClick = onLogSet, modifier = Modifier.fillMaxWidth()) {
            Text("Log set ${state.loggedSets.size + 1}")
        }

        Text("Logged sets", style = MaterialTheme.typography.titleLarge)
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.loggedSets) { set ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Set ${set.setIndex + 1}")
                        Text(
                            buildString {
                                set.reps?.let { append("$it reps") }
                                set.weightKg?.let { append(if (isNotEmpty()) " · ${it}kg" else "${it}kg") }
                            },
                        )
                    }
                }
            }
        }

        OutlinedButton(onClick = onFinish, modifier = Modifier.fillMaxWidth()) {
            Text("Finish session")
        }
    }
}

private fun formatElapsed(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
