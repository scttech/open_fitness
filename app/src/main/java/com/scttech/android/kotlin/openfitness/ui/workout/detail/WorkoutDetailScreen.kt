package com.scttech.android.kotlin.openfitness.ui.workout.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scttech.android.kotlin.openfitness.domain.model.Workout
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutStyleConfig
import com.scttech.android.kotlin.openfitness.ui.common.ConfirmDialog
import com.scttech.android.kotlin.openfitness.ui.common.FullScreenLoading

@Composable
internal fun WorkoutDetailRoute(
    onBackClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    onStartSession: (Long) -> Unit,
    onExerciseClick: (Long) -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkoutDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.deleted.collect { onDeleted() }
    }

    WorkoutDetailScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onEditClick = onEditClick,
        onStartSession = onStartSession,
        onExerciseClick = onExerciseClick,
        onDeleteClick = viewModel::deleteWorkout,
        modifier = modifier,
    )
}

@Composable
internal fun WorkoutDetailScreen(
    uiState: WorkoutDetailUiState,
    onBackClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    onStartSession: (Long) -> Unit,
    onExerciseClick: (Long) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Workout") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState is WorkoutDetailUiState.Success) {
                        IconButton(onClick = { onEditClick(uiState.workout.id) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
                        }
                    }
                },
            )
        },
    ) { padding ->
        when (uiState) {
            WorkoutDetailUiState.Loading -> FullScreenLoading(Modifier.padding(padding))
            WorkoutDetailUiState.NotFound -> FullScreenLoading(Modifier.padding(padding))
            is WorkoutDetailUiState.Success -> {
                val workout = uiState.workout
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(workout.style.displayName, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Text(workout.name, style = MaterialTheme.typography.headlineSmall)

                    HorizontalDivider()

                    Text("Plan", style = MaterialTheme.typography.titleLarge)
                    Text(styleSummary(workout.styleConfig), style = MaterialTheme.typography.bodyLarge)

                    HorizontalDivider()

                    Text("Exercises", style = MaterialTheme.typography.titleLarge)
                    workout.exercises.sortedBy { it.order }.forEach { exercise ->
                        val text = "• ${exercise.name}" + exerciseDetail(exercise)
                        val exerciseId = exercise.exerciseId
                        if (exerciseId != null) {
                            Text(
                                text,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { onExerciseClick(exerciseId) },
                            )
                        } else {
                            Text(text, style = MaterialTheme.typography.bodyLarge)
                        }
                    }

                    if (workout.notes.isNotBlank()) {
                        HorizontalDivider()
                        Text("Notes", style = MaterialTheme.typography.titleLarge)
                        Text(workout.notes, style = MaterialTheme.typography.bodyLarge)
                    }

                    Button(
                        onClick = { onStartSession(workout.id) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Text("Start session")
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        ConfirmDialog(
            title = "Delete workout?",
            text = "This can't be undone.",
            confirmLabel = "Delete",
            onConfirm = {
                showDeleteConfirm = false
                onDeleteClick()
            },
            onDismiss = { showDeleteConfirm = false },
        )
    }
}

private fun exerciseDetail(exercise: com.scttech.android.kotlin.openfitness.domain.model.WorkoutExercise): String {
    val parts = buildList {
        exercise.targetSets?.let { add("$it sets") }
        exercise.targetReps?.let { add("$it reps") }
        exercise.targetWeightKg?.let { add("${it}kg") }
        exercise.targetDurationSeconds?.let { add("${it}s") }
        exercise.targetDistanceMeters?.let { add("${it}m") }
    }
    return if (parts.isEmpty()) "" else " (${parts.joinToString(", ")})"
}

private fun styleSummary(config: WorkoutStyleConfig): String = when (config) {
    is WorkoutStyleConfig.Tabata -> buildString {
        append("${config.workSeconds}s work / ${config.restSeconds}s rest × ${config.roundsPerCycle} rounds")
        if (config.roundsPerCycle > 1) append(", ${config.restBetweenCyclesSeconds}s between rounds")
        if (config.cycles > 1) append(", ${config.cycles} cycles")
        if (config.countdownSoundEnabled) append(", countdown sound")
    }
    is WorkoutStyleConfig.GreaseTheGroove ->
        "${config.repsPerSet} reps × ${config.targetSetsPerDay} sets/day, at least ${config.minRestMinutesBetweenSets} min apart " +
            "(≈${config.dailyTargetReps} reps/day)"
    is WorkoutStyleConfig.Pyramid ->
        "${config.direction.name.replace('_', '/')} pyramid: ${config.repScheme().joinToString("-")} reps"
    is WorkoutStyleConfig.Density ->
        "Max quality volume in ${config.durationMinutes} minutes" + (config.targetRounds?.let { ", target $it rounds" } ?: "")
    is WorkoutStyleConfig.StepLoading ->
        "${config.setCount} sets × ${config.repsPerSet} reps, starting ${config.startWeightKg}kg +${config.stepWeightKg}kg/set" +
            (config.deloadEverySessions?.let { ", deload every $it sessions" } ?: "")
    is WorkoutStyleConfig.Emom ->
        "60s per exercise × ${config.rounds} rounds, goal ${config.repGoal} reps, ${config.restBetweenRoundsSeconds}s rest between rounds"
}
