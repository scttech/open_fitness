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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryBooks
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutStyleConfig
import com.scttech.android.kotlin.openfitness.ui.common.ExercisePickerDialog
import com.scttech.android.kotlin.openfitness.ui.common.FullScreenLoading
import com.scttech.android.kotlin.openfitness.ui.common.RestGuidanceDialog
import com.scttech.android.kotlin.openfitness.ui.common.SetsRepsGuidanceDialog

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
        onPickCircuitExercise = viewModel::pickCircuitExercise,
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
    onPickCircuitExercise: (Int, String, Long?) -> Unit,
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
                        isError = uiState.nameError != null,
                        supportingText = uiState.nameError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )

                    HorizontalDivider()

                    StyleFieldsForm(
                        fields = uiState.fields,
                        circuitExercises = uiState.circuitExercises,
                        usesCircuitExercises = uiState.usesCircuitExercises,
                        onFieldsChange = onFieldsChange,
                        onPickCircuitExercise = onPickCircuitExercise,
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
    circuitExercises: List<CircuitExerciseField>,
    usesCircuitExercises: Boolean,
    onFieldsChange: (StyleFormFields) -> Unit,
    onPickCircuitExercise: (Int, String, Long?) -> Unit,
    onAddCircuitExercise: () -> Unit,
    onRemoveCircuitExercise: (Int) -> Unit,
) {
    val numberOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    val decimalOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    var showExercisePicker by remember { mutableStateOf(false) }
    var circuitPickerIndex by remember { mutableStateOf<Int?>(null) }
    var showRestGuidance by remember { mutableStateOf(false) }
    var showSetsRepsGuidance by remember { mutableStateOf(false) }

    when (fields) {
        is StyleFormFields.TabataFields -> {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField("Work (sec)", fields.workSeconds, Modifier.weight(1f)) { onFieldsChange(fields.copy(workSeconds = it)) }
                NumberField(
                    "Rest (sec)",
                    fields.restSeconds,
                    Modifier.weight(1f),
                    onHelpClick = { showRestGuidance = true },
                    helpContentDescription = "How much rest do I need?",
                ) { onFieldsChange(fields.copy(restSeconds = it)) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField("Rounds/cycle", fields.roundsPerCycle, Modifier.weight(1f)) { onFieldsChange(fields.copy(roundsPerCycle = it)) }
                NumberField("Cycles", fields.cycles, Modifier.weight(1f)) { onFieldsChange(fields.copy(cycles = it)) }
            }
            NumberField(
                "Rest between cycles (sec)",
                fields.restBetweenCyclesSeconds,
                Modifier.fillMaxWidth(),
                onHelpClick = { showRestGuidance = true },
                helpContentDescription = "How much rest do I need?",
            ) {
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
            ExerciseNameField(
                name = fields.exerciseName,
                onPickClick = { showExercisePicker = true },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField(
                    "Reps/set",
                    fields.repsPerSet,
                    Modifier.weight(1f),
                    onHelpClick = { showSetsRepsGuidance = true },
                    helpContentDescription = "How many sets and reps do I need?",
                ) { onFieldsChange(fields.copy(repsPerSet = it)) }
                NumberField("Sets/day", fields.targetSetsPerDay, Modifier.weight(1f)) { onFieldsChange(fields.copy(targetSetsPerDay = it)) }
            }
            NumberField(
                "Min rest between sets (min)",
                fields.minRestMinutesBetweenSets,
                Modifier.fillMaxWidth(),
                onHelpClick = { showRestGuidance = true },
                helpContentDescription = "How much rest do I need?",
            ) {
                onFieldsChange(fields.copy(minRestMinutesBetweenSets = it))
            }
        }
        is StyleFormFields.PyramidFields -> {
            ExerciseNameField(
                name = fields.exerciseName,
                onPickClick = { showExercisePicker = true },
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
                NumberField(
                    "Start reps",
                    fields.startReps,
                    Modifier.weight(1f),
                    onHelpClick = { showSetsRepsGuidance = true },
                    helpContentDescription = "How many sets and reps do I need?",
                ) { onFieldsChange(fields.copy(startReps = it)) }
                NumberField("Step", fields.stepReps, Modifier.weight(1f)) { onFieldsChange(fields.copy(stepReps = it)) }
                NumberField("Peak reps", fields.peakReps, Modifier.weight(1f)) { onFieldsChange(fields.copy(peakReps = it)) }
            }
            NumberField(
                "Rest between sets (sec)",
                fields.restSeconds,
                Modifier.fillMaxWidth(),
                onHelpClick = { showRestGuidance = true },
                helpContentDescription = "How much rest do I need?",
            ) {
                onFieldsChange(fields.copy(restSeconds = it))
            }
        }
        is StyleFormFields.DensityFields -> {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField("Duration (min)", fields.durationMinutes, Modifier.weight(1f)) { onFieldsChange(fields.copy(durationMinutes = it)) }
                NumberField("Target rounds (optional)", fields.targetRounds, Modifier.weight(1f)) { onFieldsChange(fields.copy(targetRounds = it)) }
            }
        }
        is StyleFormFields.StepLoadingFields -> {
            ExerciseNameField(
                name = fields.exerciseName,
                onPickClick = { showExercisePicker = true },
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
                NumberField(
                    "Reps/set",
                    fields.repsPerSet,
                    Modifier.weight(1f),
                    onHelpClick = { showSetsRepsGuidance = true },
                    helpContentDescription = "How many sets and reps do I need?",
                ) { onFieldsChange(fields.copy(repsPerSet = it)) }
            }
            NumberField(
                "Rest between sets (sec)",
                fields.restSeconds,
                Modifier.fillMaxWidth(),
                onHelpClick = { showRestGuidance = true },
                helpContentDescription = "How much rest do I need?",
            ) {
                onFieldsChange(fields.copy(restSeconds = it))
            }
            NumberField("Deload every N sessions (optional)", fields.deloadEverySessions, Modifier.fillMaxWidth()) {
                onFieldsChange(fields.copy(deloadEverySessions = it))
            }
        }
        is StyleFormFields.EmomFields -> {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField(
                    "Rep goal",
                    fields.repGoal,
                    Modifier.weight(1f),
                    onHelpClick = { showSetsRepsGuidance = true },
                    helpContentDescription = "How many sets and reps do I need?",
                ) { onFieldsChange(fields.copy(repGoal = it)) }
                NumberField("Rounds", fields.rounds, Modifier.weight(1f)) { onFieldsChange(fields.copy(rounds = it)) }
            }
            NumberField(
                "Rest between rounds (sec)",
                fields.restBetweenRoundsSeconds,
                Modifier.fillMaxWidth(),
                onHelpClick = { showRestGuidance = true },
                helpContentDescription = "How much rest do I need?",
            ) {
                onFieldsChange(fields.copy(restBetweenRoundsSeconds = it))
            }
        }
    }

    if (usesCircuitExercises) {
        Text("Exercises", style = MaterialTheme.typography.titleLarge)
        circuitExercises.forEachIndexed { index, circuitExercise ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = circuitExercise.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Exercise ${index + 1}") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                IconButton(onClick = { circuitPickerIndex = index }) {
                    Icon(Icons.Filled.LibraryBooks, contentDescription = "Pick exercise ${index + 1} from library")
                }
                IconButton(onClick = { onRemoveCircuitExercise(index) }, enabled = circuitExercises.size > 1) {
                    Icon(Icons.Filled.Delete, contentDescription = "Remove exercise ${index + 1}")
                }
            }
        }
        TextButton(onClick = onAddCircuitExercise) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Text("Add exercise")
        }
    }

    if (showExercisePicker) {
        ExercisePickerDialog(
            onSelected = { exercise ->
                onFieldsChange(fields.withPickedExercise(exercise.name, exercise.id))
                showExercisePicker = false
            },
            onDismiss = { showExercisePicker = false },
        )
    }

    val pickerIndex = circuitPickerIndex
    if (pickerIndex != null) {
        ExercisePickerDialog(
            onSelected = { exercise ->
                onPickCircuitExercise(pickerIndex, exercise.name, exercise.id)
                circuitPickerIndex = null
            },
            onDismiss = { circuitPickerIndex = null },
        )
    }

    if (showSetsRepsGuidance) {
        SetsRepsGuidanceDialog(onDismiss = { showSetsRepsGuidance = false })
    }

    if (showRestGuidance) {
        RestGuidanceDialog(onDismiss = { showRestGuidance = false })
    }
}

@Composable
private fun ExerciseNameField(
    name: String,
    onPickClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Exercise") },
            modifier = Modifier.weight(1f),
            singleLine = true,
        )
        IconButton(onClick = onPickClick) {
            Icon(Icons.Filled.LibraryBooks, contentDescription = "Pick from exercise library")
        }
    }
}

@Composable
private fun NumberField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onHelpClick: (() -> Unit)? = null,
    helpContentDescription: String = "Help",
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
        singleLine = true,
        trailingIcon = onHelpClick?.let {
            {
                IconButton(onClick = it) {
                    Icon(Icons.Filled.Info, contentDescription = helpContentDescription)
                }
            }
        },
    )
}
