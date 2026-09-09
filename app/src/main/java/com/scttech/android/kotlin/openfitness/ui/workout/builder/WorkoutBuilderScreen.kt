package com.scttech.android.kotlin.openfitness.ui.workout.builder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutStyleConfig
import com.scttech.android.kotlin.openfitness.ui.common.FullScreenLoading

@Composable
internal fun WorkoutBuilderRoute(
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkoutBuilderViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is WorkoutBuilderUiState.Loaded && (uiState as WorkoutBuilderUiState.Loaded).saved) {
            onSaved()
        }
    }

    WorkoutBuilderScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onNameChange = viewModel::updateName,
        onNotesChange = viewModel::updateNotes,
        onFieldsChange = viewModel::updateFields,
        onCircuitExerciseChange = viewModel::updateCircuitExerciseName,
        onAddCircuitExercise = viewModel::addCircuitExercise,
        onRemoveCircuitExercise = viewModel::removeCircuitExercise,
        onSave = viewModel::save,
        modifier = modifier,
    )
}

@Composable
internal fun WorkoutBuilderScreen(
    uiState: WorkoutBuilderUiState,
    onBackClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onFieldsChange: (StyleFormFields) -> Unit,
    onCircuitExerciseChange: (Int, String) -> Unit,
    onAddCircuitExercise: () -> Unit,
    onRemoveCircuitExercise: (Int) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (uiState is WorkoutBuilderUiState.Loaded && !uiState.isNew) "Edit Workout" else "New Workout") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState is WorkoutBuilderUiState.Loaded) {
                        TextButton(onClick = onSave, enabled = uiState.canSave) { Text("Save") }
                    }
                },
            )
        },
    ) { padding ->
        when (uiState) {
            WorkoutBuilderUiState.Loading -> FullScreenLoading(Modifier.padding(padding))
            is WorkoutBuilderUiState.Loaded -> {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(uiState.style.displayName, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Text(uiState.style.shortDescription, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = onNameChange,
                        label = { Text("Workout name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )

                    HorizontalDivider()

                    StyleFieldsForm(
                        fields = uiState.fields,
                        circuitExerciseNames = uiState.circuitExerciseNames,
                        usesCircuitExercises = uiState.usesCircuitExercises,
                        onFieldsChange = onFieldsChange,
                        onCircuitExerciseChange = onCircuitExerciseChange,
                        onAddCircuitExercise = onAddCircuitExercise,
                        onRemoveCircuitExercise = onRemoveCircuitExercise,
                    )

                    HorizontalDivider()

                    OutlinedTextField(
                        value = uiState.notes,
                        onValueChange = onNotesChange,
                        label = { Text("Notes (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                    )

                    Button(onClick = onSave, enabled = uiState.canSave, modifier = Modifier.fillMaxWidth()) {
                        Text("Save workout")
                    }
                }
            }
        }
    }
}

@Composable
private fun StyleFieldsForm(
    fields: StyleFormFields,
    circuitExerciseNames: List<String>,
    usesCircuitExercises: Boolean,
    onFieldsChange: (StyleFormFields) -> Unit,
    onCircuitExerciseChange: (Int, String) -> Unit,
    onAddCircuitExercise: () -> Unit,
    onRemoveCircuitExercise: (Int) -> Unit,
) {
    val numberOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    val decimalOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)

    when (fields) {
        is StyleFormFields.TabataFields -> {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField("Work (sec)", fields.workSeconds, Modifier.weight(1f)) { onFieldsChange(fields.copy(workSeconds = it)) }
                NumberField("Rest (sec)", fields.restSeconds, Modifier.weight(1f)) { onFieldsChange(fields.copy(restSeconds = it)) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField("Rounds/cycle", fields.roundsPerCycle, Modifier.weight(1f)) { onFieldsChange(fields.copy(roundsPerCycle = it)) }
                NumberField("Cycles", fields.cycles, Modifier.weight(1f)) { onFieldsChange(fields.copy(cycles = it)) }
            }
            NumberField("Rest between cycles (sec)", fields.restBetweenCyclesSeconds, Modifier.fillMaxWidth()) {
                onFieldsChange(fields.copy(restBetweenCyclesSeconds = it))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Countdown sound at 10s remaining")
                Switch(
                    checked = fields.countdownSoundEnabled,
                    onCheckedChange = { onFieldsChange(fields.copy(countdownSoundEnabled = it)) },
                )
            }
        }
        is StyleFormFields.GtgFields -> {
            OutlinedTextField(
                value = fields.exerciseName,
                onValueChange = { onFieldsChange(fields.copy(exerciseName = it)) },
                label = { Text("Exercise") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField("Reps/set", fields.repsPerSet, Modifier.weight(1f)) { onFieldsChange(fields.copy(repsPerSet = it)) }
                NumberField("Sets/day", fields.targetSetsPerDay, Modifier.weight(1f)) { onFieldsChange(fields.copy(targetSetsPerDay = it)) }
            }
            NumberField("Min rest between sets (min)", fields.minRestMinutesBetweenSets, Modifier.fillMaxWidth()) {
                onFieldsChange(fields.copy(minRestMinutesBetweenSets = it))
            }
        }
        is StyleFormFields.PyramidFields -> {
            OutlinedTextField(
                value = fields.exerciseName,
                onValueChange = { onFieldsChange(fields.copy(exerciseName = it)) },
                label = { Text("Exercise") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                WorkoutStyleConfig.PyramidDirection.entries.forEachIndexed { index, direction ->
                    SegmentedButton(
                        selected = fields.direction == direction,
                        onClick = { onFieldsChange(fields.copy(direction = direction)) },
                        shape = SegmentedButtonDefaults.itemShape(index, WorkoutStyleConfig.PyramidDirection.entries.size),
                    ) {
                        Text(direction.name.replace('_', '/'))
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField("Start reps", fields.startReps, Modifier.weight(1f)) { onFieldsChange(fields.copy(startReps = it)) }
                NumberField("Step", fields.stepReps, Modifier.weight(1f)) { onFieldsChange(fields.copy(stepReps = it)) }
                NumberField("Peak reps", fields.peakReps, Modifier.weight(1f)) { onFieldsChange(fields.copy(peakReps = it)) }
            }
        }
        is StyleFormFields.DensityFields -> {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField("Duration (min)", fields.durationMinutes, Modifier.weight(1f)) { onFieldsChange(fields.copy(durationMinutes = it)) }
                NumberField("Target rounds (optional)", fields.targetRounds, Modifier.weight(1f)) { onFieldsChange(fields.copy(targetRounds = it)) }
            }
        }
        is StyleFormFields.StepLoadingFields -> {
            OutlinedTextField(
                value = fields.exerciseName,
                onValueChange = { onFieldsChange(fields.copy(exerciseName = it)) },
                label = { Text("Exercise") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = fields.startWeightKg,
                    onValueChange = { onFieldsChange(fields.copy(startWeightKg = it)) },
                    label = { Text("Start weight (kg)") },
                    keyboardOptions = decimalOptions,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = fields.stepWeightKg,
                    onValueChange = { onFieldsChange(fields.copy(stepWeightKg = it)) },
                    label = { Text("Step (kg)") },
                    keyboardOptions = decimalOptions,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField("Sets", fields.setCount, Modifier.weight(1f)) { onFieldsChange(fields.copy(setCount = it)) }
                NumberField("Reps/set", fields.repsPerSet, Modifier.weight(1f)) { onFieldsChange(fields.copy(repsPerSet = it)) }
            }
            NumberField("Deload every N sessions (optional)", fields.deloadEverySessions, Modifier.fillMaxWidth()) {
                onFieldsChange(fields.copy(deloadEverySessions = it))
            }
        }
    }

    if (usesCircuitExercises) {
        Text("Exercises", style = MaterialTheme.typography.titleLarge)
        circuitExerciseNames.forEachIndexed { index, name ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { onCircuitExerciseChange(index, it) },
                    label = { Text("Exercise ${index + 1}") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                IconButton(onClick = { onRemoveCircuitExercise(index) }, enabled = circuitExerciseNames.size > 1) {
                    Icon(Icons.Filled.Delete, contentDescription = "Remove exercise ${index + 1}")
                }
            }
        }
        TextButton(onClick = onAddCircuitExercise) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Text("Add exercise")
        }
    }
}

@Composable
private fun NumberField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
        singleLine = true,
    )
}
