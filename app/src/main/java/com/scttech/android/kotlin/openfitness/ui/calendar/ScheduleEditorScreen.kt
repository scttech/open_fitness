package com.scttech.android.kotlin.openfitness.ui.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scttech.android.kotlin.openfitness.domain.model.ScheduledEventKind
import com.scttech.android.kotlin.openfitness.ui.common.ConfirmDialog
import com.scttech.android.kotlin.openfitness.ui.common.FullScreenLoading
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

private val EDITABLE_KINDS = listOf(
    ScheduledEventKind.WORKOUT,
    ScheduledEventKind.PROGRAM_SESSION,
    ScheduledEventKind.CUSTOM,
)

private val REMINDER_OPTIONS = listOf(null to "No reminder", 0 to "At start time", 10 to "10 minutes before", 30 to "30 minutes before", 60 to "1 hour before")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScheduleEditorRoute(
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScheduleEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    var showUntilPicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Edit schedule" else "Schedule") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    if (state.isEditing) {
                        IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Filled.Delete, contentDescription = "Delete") }
                    }
                    TextButton(enabled = state.canSave, onClick = { viewModel.save(onSaved) }) { Text("Save") }
                },
            )
        },
    ) { padding ->
        if (state.isLoading) {
            FullScreenLoading(Modifier.padding(padding))
            return@Scaffold
        }
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (state.isWorkoutLocked) {
                Text(state.title, style = MaterialTheme.typography.headlineSmall)
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EDITABLE_KINDS.forEach { kind ->
                        FilterChip(
                            selected = state.kind == kind,
                            onClick = { viewModel.setKind(kind) },
                            label = { Text(kind.displayName) },
                        )
                    }
                }
            }

            if (!state.isWorkoutLocked) when (state.kind) {
                ScheduledEventKind.WORKOUT -> Picker(
                    label = "Workout",
                    value = state.workouts.firstOrNull { it.id == state.workoutId }?.name,
                    options = state.workouts.map { it.name to { viewModel.selectWorkout(it) } },
                    emptyMessage = "No saved workouts yet.",
                )
                ScheduledEventKind.PROGRAM_SESSION, ScheduledEventKind.PROGRAM_TEST -> Picker(
                    label = "Program",
                    value = state.programs.firstOrNull { it.id == state.programId }?.name,
                    options = state.programs.map { it.name to { viewModel.selectProgram(it) } },
                    emptyMessage = "No programs yet.",
                )
                ScheduledEventKind.CUSTOM -> OutlinedTextField(
                    value = state.title,
                    onValueChange = viewModel::setTitle,
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            LabeledRow("Date") {
                OutlinedButton(onClick = { showDatePicker = true }) { Text(state.date.formatMedium()) }
            }

            LabeledRow("Time") {
                Switch(
                    checked = state.startMinutes != null,
                    onCheckedChange = { viewModel.setStartMinutes(if (it) DEFAULT_START_MINUTES else null) },
                )
                state.startMinutes?.let {
                    OutlinedButton(onClick = { showTimePicker = true }, modifier = Modifier.padding(start = 12.dp)) {
                        Text(formatMinutes(it))
                    }
                }
            }

            if (state.startMinutes != null) {
                Picker(
                    label = "Reminder",
                    value = REMINDER_OPTIONS.first { it.first == state.reminderMinutesBefore }.second,
                    options = REMINDER_OPTIONS.map { (minutes, label) -> label to { viewModel.setReminder(minutes) } },
                    emptyMessage = "",
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Repeat weekly on", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    DayOfWeek.entries.forEach { day ->
                        val selected = day in state.repeatDays
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .border(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, CircleShape)
                                .clickable { viewModel.toggleRepeatDay(day) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                java.time.DayOfWeek.of(day.ordinal + 1).getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                }
                if (state.repeatDays.isNotEmpty()) {
                    LabeledRow("Until") {
                        OutlinedButton(onClick = { showUntilPicker = true }) {
                            Text(state.repeatUntil?.formatMedium() ?: "No end date")
                        }
                        if (state.repeatUntil != null) {
                            TextButton(onClick = { viewModel.setRepeatUntil(null) }) { Text("Clear") }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = state.notes,
                onValueChange = viewModel::setNotes,
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )
        }
    }

    if (showDatePicker) {
        MoveDatePickerDialog(
            initial = state.date,
            onDismiss = { showDatePicker = false },
            onConfirm = {
                showDatePicker = false
                viewModel.setDate(it)
            },
        )
    }
    if (showUntilPicker) {
        MoveDatePickerDialog(
            initial = state.repeatUntil ?: state.date,
            onDismiss = { showUntilPicker = false },
            onConfirm = {
                showUntilPicker = false
                viewModel.setRepeatUntil(it)
            },
        )
    }
    if (showTimePicker) {
        TimePickerDialog(
            initialMinutes = state.startMinutes ?: DEFAULT_START_MINUTES,
            onDismiss = { showTimePicker = false },
            onConfirm = {
                showTimePicker = false
                viewModel.setStartMinutes(it)
            },
        )
    }
    if (confirmDelete) {
        ConfirmDialog(
            title = "Delete this schedule?",
            text = "This removes the event and every repeat of it.",
            confirmLabel = "Delete",
            onConfirm = {
                confirmDelete = false
                viewModel.delete(onSaved)
            },
            onDismiss = { confirmDelete = false },
        )
    }
}

private const val DEFAULT_START_MINUTES = 18 * 60

@Composable
private fun LabeledRow(label: String, content: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(end = 16.dp))
        content()
    }
}

@Composable
private fun Picker(
    label: String,
    value: String?,
    options: List<Pair<String, () -> Unit>>,
    emptyMessage: String,
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge)
        if (options.isEmpty()) {
            Text(emptyMessage, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(value ?: "Select…", modifier = Modifier.weight(1f))
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { (text, onSelect) ->
                    DropdownMenuItem(
                        text = { Text(text) },
                        onClick = {
                            expanded = false
                            onSelect()
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(initialMinutes: Int, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    val state = rememberTimePickerState(initialHour = initialMinutes / 60, initialMinute = initialMinutes % 60)
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { TimePicker(state = state) },
        confirmButton = { TextButton(onClick = { onConfirm(state.hour * 60 + state.minute) }) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

private fun LocalDate.formatMedium(): String =
    toJavaLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault()))
