package com.scttech.android.kotlin.openfitness.ui.program.builder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scttech.android.kotlin.openfitness.domain.model.Exercise
import com.scttech.android.kotlin.openfitness.domain.model.ProgramGoalType
import com.scttech.android.kotlin.openfitness.domain.model.RepStrategy
import com.scttech.android.kotlin.openfitness.ui.common.ExercisePickerDialog
import com.scttech.android.kotlin.openfitness.ui.common.FullScreenLoading
import com.scttech.android.kotlin.openfitness.ui.theme.OpenFitnessTheme

@Composable
internal fun ProgramBuilderRoute(
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProgramBuilderViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is ProgramBuilderUiState.Loaded && (uiState as ProgramBuilderUiState.Loaded).saved) {
            onSaved()
        }
    }

    ProgramBuilderScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onNameChange = viewModel::updateName,
        onPickExercise = viewModel::pickExercise,
        onGoalTypeChange = viewModel::updateGoalType,
        onGoalTargetChange = viewModel::updateGoalTarget,
        onRepStrategyChange = viewModel::updateRepStrategy,
        onCustomSetTargetChange = viewModel::updateCustomSetTarget,
        onAddCustomSet = viewModel::addCustomSet,
        onRemoveCustomSet = viewModel::removeCustomSet,
        onSessionsPerWeekChange = viewModel::updateSessionsPerWeek,
        onRetestIntervalDaysChange = viewModel::updateRetestIntervalDays,
        onRestSecondsChange = viewModel::updateRestSeconds,
        onSave = viewModel::save,
        modifier = modifier,
    )
}

@Composable
internal fun ProgramBuilderScreen(
    uiState: ProgramBuilderUiState,
    onBackClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onPickExercise: (Exercise) -> Unit,
    onGoalTypeChange: (ProgramGoalType) -> Unit,
    onGoalTargetChange: (String) -> Unit,
    onRepStrategyChange: (RepStrategy) -> Unit,
    onCustomSetTargetChange: (Int, String) -> Unit,
    onAddCustomSet: () -> Unit,
    onRemoveCustomSet: (Int) -> Unit,
    onSessionsPerWeekChange: (String) -> Unit,
    onRetestIntervalDaysChange: (String) -> Unit,
    onRestSecondsChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showExercisePicker by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (uiState is ProgramBuilderUiState.Loaded && uiState.programId != 0L) "Edit Program" else "New Program") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState is ProgramBuilderUiState.Loaded) {
                        TextButton(onClick = onSave, enabled = uiState.canSave) { Text("Save") }
                    }
                },
            )
        },
    ) { padding ->
        when (uiState) {
            ProgramBuilderUiState.Loading -> FullScreenLoading(Modifier.padding(padding))
            is ProgramBuilderUiState.Loaded -> {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        "Set a long-term goal, like \"100 push-ups\". Periodically record a max-effort test and the app " +
                            "will recalculate your training sets from it.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = onNameChange,
                        label = { Text("Program name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = uiState.exerciseName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Exercise") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                        )
                        IconButton(onClick = { showExercisePicker = true }) {
                            Icon(Icons.AutoMirrored.Filled.LibraryBooks, contentDescription = "Pick from exercise library")
                        }
                    }

                    HorizontalDivider()

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        ProgramGoalType.entries.forEachIndexed { index, type ->
                            SegmentedButton(
                                selected = uiState.goalType == type,
                                onClick = { onGoalTypeChange(type) },
                                shape = SegmentedButtonDefaults.itemShape(index, ProgramGoalType.entries.size),
                            ) {
                                Text(type.unitLabel)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = uiState.goalTarget,
                        onValueChange = onGoalTargetChange,
                        label = { Text("Goal target (${uiState.goalType.unitLabel})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )

                    HorizontalDivider()

                    RepStrategyPicker(
                        selected = uiState.repStrategy,
                        onSelect = onRepStrategyChange,
                    )

                    if (uiState.repStrategy == RepStrategy.CUSTOM_MANUAL) {
                        CustomSetTargetsEditor(
                            targets = uiState.customSetTargets,
                            unitLabel = uiState.goalType.unitLabel,
                            onTargetChange = onCustomSetTargetChange,
                            onAddSet = onAddCustomSet,
                            onRemoveSet = onRemoveCustomSet,
                        )
                    }

                    HorizontalDivider()

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = uiState.sessionsPerWeek,
                            onValueChange = onSessionsPerWeekChange,
                            label = { Text("Sessions/week") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = uiState.retestIntervalDays,
                            onValueChange = onRetestIntervalDaysChange,
                            label = { Text("Retest every (days)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                        )
                    }

                    OutlinedTextField(
                        value = uiState.restSeconds,
                        onValueChange = onRestSecondsChange,
                        label = { Text("Rest between sets (seconds)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )

                    Button(onClick = onSave, enabled = uiState.canSave, modifier = Modifier.fillMaxWidth()) {
                        Text("Save program")
                    }
                }
            }
        }
    }

    if (showExercisePicker) {
        ExercisePickerDialog(
            onSelected = { exercise ->
                onPickExercise(exercise)
                showExercisePicker = false
            },
            onDismiss = { showExercisePicker = false },
        )
    }
}

@Composable
private fun RepStrategyPicker(
    selected: RepStrategy,
    onSelect: (RepStrategy) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Rep strategy", style = MaterialTheme.typography.titleMedium)
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            RepStrategy.entries.forEachIndexed { index, strategy ->
                SegmentedButton(
                    selected = selected == strategy,
                    onClick = { onSelect(strategy) },
                    shape = SegmentedButtonDefaults.itemShape(index, RepStrategy.entries.size),
                    modifier = Modifier.weight(1f),
                    icon = {},
                ) {
                    Text(
                        strategy.displayName.asTwoLineLabel(),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 2,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        Text(
            selected.shortDescription,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Breaks at the last word boundary so a two-word label always renders as two lines. */
private fun String.asTwoLineLabel(): String {
    val lastSpace = lastIndexOf(' ')
    return if (lastSpace == -1) this else replaceRange(lastSpace, lastSpace + 1, "\n")
}

@Preview(showBackground = true)
@Composable
private fun RepStrategyPickerPreview() {
    var selected by remember { mutableStateOf(RepStrategy.PERCENTAGE_LADDER) }
    OpenFitnessTheme(dynamicColor = false) {
        RepStrategyPicker(
            selected = selected,
            onSelect = { selected = it },
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Dark", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RepStrategyPickerDarkPreview() {
    var selected by remember { mutableStateOf(RepStrategy.AMRAP_FINISHER) }
    OpenFitnessTheme(darkTheme = true, dynamicColor = false) {
        RepStrategyPicker(
            selected = selected,
            onSelect = { selected = it },
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
private fun CustomSetTargetsEditor(
    targets: List<String>,
    unitLabel: String,
    onTargetChange: (Int, String) -> Unit,
    onAddSet: () -> Unit,
    onRemoveSet: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        targets.forEachIndexed { index, target ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = target,
                    onValueChange = { onTargetChange(index, it) },
                    label = { Text("Set ${index + 1} target ($unitLabel)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                IconButton(onClick = { onRemoveSet(index) }, enabled = targets.size > 1) {
                    Icon(Icons.Filled.Close, contentDescription = "Remove set ${index + 1}")
                }
            }
        }
        TextButton(onClick = onAddSet) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Text("Add set")
        }
    }
}
