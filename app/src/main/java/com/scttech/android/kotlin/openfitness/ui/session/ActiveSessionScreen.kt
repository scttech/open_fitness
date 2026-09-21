package com.scttech.android.kotlin.openfitness.ui.session

import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutExercise
import com.scttech.android.kotlin.openfitness.ui.common.FullScreenLoading
import com.scttech.android.kotlin.openfitness.ui.common.timer.PhaseTimerDisplay
import com.scttech.android.kotlin.openfitness.ui.common.timer.TimerColorPrefs
import com.scttech.android.kotlin.openfitness.ui.common.timer.TimerPhaseKind

@Composable
internal fun ActiveSessionRoute(
    onBackClick: () -> Unit,
    onFinished: () -> Unit,
    onExerciseClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActiveSessionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val timerColors by viewModel.timerColors.collectAsStateWithLifecycle()

    // Every session type shows its own completion screen (with a Done button) instead of
    // auto-navigating away, so the user actually sees the motivational message before leaving.
    ActiveSessionScreen(
        uiState = uiState,
        timerColors = timerColors,
        onBackClick = onBackClick,
        actions = ActiveSessionActions(
            tabata = TabataActions(
                onToggleRunning = viewModel::toggleTabataRunning,
                onSkip = viewModel::skipTabataPhase,
            ),
            density = DensityActions(
                onToggleRunning = viewModel::toggleDensityRunning,
                onIncrementRounds = viewModel::incrementDensityRounds,
                onFinish = viewModel::finishDensity,
            ),
            setLogging = SetLoggingActions(
                onRepsInputChange = viewModel::updateRepsInput,
                onWeightInputChange = viewModel::updateWeightInput,
                onLogSet = viewModel::logSet,
                onFinish = viewModel::finishSetLoggingSession,
                onToggleRestRunning = viewModel::toggleRestRunning,
                onSkipRest = viewModel::skipRest,
            ),
            emom = EmomActions(
                onToggleRunning = viewModel::toggleEmomRunning,
                onSkip = viewModel::skipEmomPhase,
            ),
            onDone = onFinished,
            onExerciseClick = onExerciseClick,
        ),
        modifier = modifier,
    )
}

@Composable
internal fun ActiveSessionScreen(
    uiState: ActiveSessionUiState,
    timerColors: TimerColorPrefs,
    onBackClick: () -> Unit,
    actions: ActiveSessionActions,
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
            is ActiveSessionUiState.TabataSession -> TabataSessionContent(
                uiState,
                timerColors,
                actions.tabata,
                actions.onDone,
                actions.onExerciseClick,
                Modifier.padding(padding),
            )
            is ActiveSessionUiState.DensitySession -> DensitySessionContent(
                uiState,
                actions.density,
                actions.onDone,
                actions.onExerciseClick,
                Modifier.padding(padding),
            )
            is ActiveSessionUiState.SetLoggingSession -> SetLoggingSessionContent(
                uiState,
                timerColors,
                actions.setLogging,
                actions.onDone,
                actions.onExerciseClick,
                Modifier.padding(padding),
            )
            is ActiveSessionUiState.EmomSession -> EmomSessionContent(
                uiState,
                timerColors,
                actions.emom,
                actions.onDone,
                actions.onExerciseClick,
                Modifier.padding(padding),
            )
        }
    }
}

private fun sessionTitle(uiState: ActiveSessionUiState): String = when (uiState) {
    is ActiveSessionUiState.TabataSession -> uiState.workoutName
    is ActiveSessionUiState.DensitySession -> uiState.workoutName
    is ActiveSessionUiState.SetLoggingSession -> uiState.workoutName
    is ActiveSessionUiState.EmomSession -> uiState.workoutName
    ActiveSessionUiState.Loading -> "Session"
}

@Composable
private fun TabataSessionContent(
    state: ActiveSessionUiState.TabataSession,
    timerColors: TimerColorPrefs,
    actions: TabataActions,
    onDone: () -> Unit,
    onExerciseClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.isFinished) {
        val workIntervals = state.timerState.phases.count { it.kind == TimerPhaseKind.WORK }
        SessionCompleteContent(
            message = state.completionMessage.orEmpty(),
            summary = "$workIntervals ${if (workIntervals == 1) "interval" else "intervals"} completed.",
            onDone = onDone,
            modifier = modifier,
        )
        return
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        PhaseTimerDisplay(
            state = state.timerState,
            workColor = timerColors.workColor,
            restColor = timerColors.restColor,
            onPauseResume = actions.onToggleRunning,
            onSkip = actions.onSkip,
            onExerciseClick = onExerciseClick,
        )
    }
}

@Composable
private fun EmomSessionContent(
    state: ActiveSessionUiState.EmomSession,
    timerColors: TimerColorPrefs,
    actions: EmomActions,
    onDone: () -> Unit,
    onExerciseClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.isFinished) {
        SessionCompleteContent(
            message = state.completionMessage.orEmpty(),
            summary = "${state.config.rounds} ${if (state.config.rounds == 1) "round" else "rounds"} completed.",
            onDone = onDone,
            modifier = modifier,
        )
        return
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Rep goal: ${state.config.repGoal}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        PhaseTimerDisplay(
            state = state.timerState,
            workColor = timerColors.workColor,
            restColor = timerColors.restColor,
            onPauseResume = actions.onToggleRunning,
            onSkip = actions.onSkip,
            onExerciseClick = onExerciseClick,
        )
    }
}

@Composable
private fun DensitySessionContent(
    state: ActiveSessionUiState.DensitySession,
    actions: DensityActions,
    onDone: () -> Unit,
    onExerciseClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.isFinished) {
        SessionCompleteContent(
            message = state.completionMessage.orEmpty(),
            summary = "${state.roundsCompleted} ${if (state.roundsCompleted == 1) "round" else "rounds"} in ${formatElapsed(state.elapsedSeconds)}.",
            onDone = onDone,
            modifier = modifier,
        )
        return
    }

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Target: ${state.config.durationMinutes} min", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(8.dp))
        Text(formatElapsed(state.elapsedSeconds), fontSize = 64.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        DensityExerciseList(exercises = state.exercises, onExerciseClick = onExerciseClick)
        Spacer(Modifier.height(24.dp))
        Text("Rounds completed: ${state.roundsCompleted}", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Button(onClick = actions.onIncrementRounds) { Text("+1 round") }
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilledTonalButton(onClick = actions.onToggleRunning) {
                Icon(if (state.isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (state.isRunning) "Pause" else "Start")
            }
            OutlinedButton(onClick = actions.onFinish) { Text("Finish") }
        }
    }
}

/** "Exercises: A, B, C" with each name individually tappable when it links into the exercise library. */
@Composable
private fun DensityExerciseList(
    exercises: List<WorkoutExercise>,
    onExerciseClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        Text("Exercises: ", color = MaterialTheme.colorScheme.onSurfaceVariant)
        exercises.forEachIndexed { index, exercise ->
            val exerciseId = exercise.exerciseId
            Text(
                exercise.name + if (index < exercises.lastIndex) ", " else "",
                color = if (exerciseId != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = if (exerciseId != null) Modifier.clickable { onExerciseClick(exerciseId) } else Modifier,
            )
        }
    }
}

@Composable
private fun SetLoggingSessionContent(
    state: ActiveSessionUiState.SetLoggingSession,
    timerColors: TimerColorPrefs,
    actions: SetLoggingActions,
    onDone: () -> Unit,
    onExerciseClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.isFinished) {
        val setsLogged = state.loggedSets.size
        SessionCompleteContent(
            message = state.completionMessage.orEmpty(),
            summary = "$setsLogged ${if (setsLogged == 1) "set" else "sets"} logged.",
            onDone = onDone,
            modifier = modifier,
        )
        return
    }

    val restTimerState = state.restTimerState
    if (restTimerState != null) {
        Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            PhaseTimerDisplay(
                state = restTimerState,
                workColor = timerColors.workColor,
                restColor = timerColors.restColor,
                onPauseResume = actions.onToggleRestRunning,
                onSkip = actions.onSkipRest,
                skipLabel = "Skip rest",
                comingUpLabel = state.exerciseName,
            )
        }
        return
    }

    val totalSets = state.totalSets
    Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        val exerciseId = state.exerciseId
        Text(
            state.exerciseName,
            style = MaterialTheme.typography.headlineSmall,
            color = if (exerciseId != null) MaterialTheme.colorScheme.primary else Color.Unspecified,
            modifier = if (exerciseId != null) Modifier.clickable { onExerciseClick(exerciseId) } else Modifier,
        )
        Text(state.targetDescription, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (totalSets != null) {
            Text("Set ${state.loggedSets.size + 1} of $totalSets", style = MaterialTheme.typography.labelLarge)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.repsInput,
                onValueChange = actions.onRepsInputChange,
                label = { Text("Reps") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
            OutlinedTextField(
                value = state.weightInput,
                onValueChange = actions.onWeightInputChange,
                label = { Text("Weight (kg, optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
        }

        Button(onClick = actions.onLogSet, modifier = Modifier.fillMaxWidth()) {
            Text(
                if (totalSets != null && state.loggedSets.size + 1 >= totalSets) {
                    "Log final set"
                } else {
                    "Log set ${state.loggedSets.size + 1}"
                },
            )
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

        OutlinedButton(onClick = actions.onFinish, modifier = Modifier.fillMaxWidth()) {
            Text("Finish session")
        }
    }
}

@Composable
private fun SessionCompleteContent(
    message: String,
    summary: String,
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
            summary,
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

private fun formatElapsed(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
