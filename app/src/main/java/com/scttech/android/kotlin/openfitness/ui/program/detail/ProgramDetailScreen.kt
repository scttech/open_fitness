package com.scttech.android.kotlin.openfitness.ui.program.detail

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
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.scttech.android.kotlin.openfitness.domain.model.Program
import com.scttech.android.kotlin.openfitness.domain.model.ProgramTest
import com.scttech.android.kotlin.openfitness.ui.common.ConfirmDialog
import com.scttech.android.kotlin.openfitness.ui.common.FullScreenLoading
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun ProgramDetailRoute(
    onBackClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    onRecordTest: (Long) -> Unit,
    onStartSession: (Long) -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProgramDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.deleted.collect { onDeleted() }
    }

    ProgramDetailScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onEditClick = onEditClick,
        onRecordTest = onRecordTest,
        onStartSession = onStartSession,
        onDeleteClick = viewModel::deleteProgram,
        modifier = modifier,
    )
}

@Composable
internal fun ProgramDetailScreen(
    uiState: ProgramDetailUiState,
    onBackClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    onRecordTest: (Long) -> Unit,
    onStartSession: (Long) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Program") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState is ProgramDetailUiState.Success) {
                        IconButton(onClick = { onEditClick(uiState.program.id) }) {
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
            ProgramDetailUiState.Loading -> FullScreenLoading(Modifier.padding(padding))
            ProgramDetailUiState.NotFound -> FullScreenLoading(Modifier.padding(padding))
            is ProgramDetailUiState.Success -> {
                val program = uiState.program
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(program.name, style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Goal: ${program.goalTarget} ${program.goalType.unitLabel} of ${program.exerciseName}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    HorizontalDivider()

                    Text("Current prescription", style = MaterialTheme.typography.titleLarge)
                    Text(
                        program.config.repStrategyConfig.strategy.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    val prescription = program.currentPrescription
                    if (prescription != null) {
                        Text(
                            "Training max: ${prescription.trainingMax} ${program.goalType.unitLabel} " +
                                "(from a test of ${prescription.basedOnTestResult})",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        prescription.sets.forEach { set ->
                            val target = if (set.isAmrap) "${set.targetValue}+ (AMRAP)" else "${set.targetValue}"
                            Text("Set ${set.setIndex + 1}: $target ${program.goalType.unitLabel}")
                        }
                    } else {
                        Text(
                            "Record your first test to get a training prescription.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    HorizontalDivider()

                    Text("Testing", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Retest every ${program.config.retestIntervalDays} days" +
                            (program.nextTestDueAt?.let { ", next due ${it.toLocalDateTime(TimeZone.currentSystemDefault()).date}" } ?: ""),
                        style = MaterialTheme.typography.bodyLarge,
                    )

                    if (uiState.tests.isNotEmpty()) {
                        Text("Test history", style = MaterialTheme.typography.titleMedium)
                        uiState.tests.forEach { test -> TestRow(test, program.goalType.unitLabel) }
                    }

                    Button(onClick = { onStartSession(program.id) }, modifier = Modifier.fillMaxWidth(), enabled = prescription != null) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Text("Start session")
                    }
                    OutlinedButton(onClick = { onRecordTest(program.id) }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.Timeline, contentDescription = null)
                        Text("Record a test")
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        ConfirmDialog(
            title = "Delete program?",
            text = "This deletes its test history and sessions too. This can't be undone.",
            confirmLabel = "Delete",
            onConfirm = {
                showDeleteConfirm = false
                onDeleteClick()
            },
            onDismiss = { showDeleteConfirm = false },
        )
    }
}

@Composable
private fun TestRow(test: ProgramTest, unitLabel: String, modifier: Modifier = Modifier) {
    val localDate = test.testedAt.toLocalDateTime(TimeZone.currentSystemDefault()).date
    Text("$localDate — ${test.result} $unitLabel", modifier = modifier)
}
