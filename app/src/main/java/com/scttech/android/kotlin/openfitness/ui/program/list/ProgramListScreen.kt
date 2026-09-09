package com.scttech.android.kotlin.openfitness.ui.program.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scttech.android.kotlin.openfitness.domain.model.Program
import com.scttech.android.kotlin.openfitness.ui.common.EmptyState
import com.scttech.android.kotlin.openfitness.ui.common.FullScreenLoading

@Composable
internal fun ProgramListRoute(
    onProgramClick: (Long) -> Unit,
    onNewProgram: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProgramListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProgramListScreen(
        uiState = uiState,
        onProgramClick = onProgramClick,
        onNewProgram = onNewProgram,
        onDeleteProgram = viewModel::deleteProgram,
        modifier = modifier,
    )
}

@Composable
internal fun ProgramListScreen(
    uiState: ProgramListUiState,
    onProgramClick: (Long) -> Unit,
    onNewProgram: () -> Unit,
    onDeleteProgram: (Program) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Programs") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewProgram) {
                Icon(Icons.Filled.Add, contentDescription = "New program")
            }
        },
    ) { padding ->
        when (uiState) {
            ProgramListUiState.Loading -> FullScreenLoading(Modifier.padding(padding))
            is ProgramListUiState.Success -> {
                if (uiState.programs.isEmpty()) {
                    EmptyState(
                        message = "No programs yet. Tap + to set a goal like \"100 push-ups\" and start testing your progress.",
                        modifier = Modifier.padding(padding),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(uiState.programs, key = { it.id }) { program ->
                            ProgramRow(
                                program = program,
                                onClick = { onProgramClick(program.id) },
                                onDelete = { onDeleteProgram(program) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgramRow(
    program: Program,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(program.name, style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Goal: ${program.goalTarget.toCleanString()} ${program.goalType.unitLabel} · ${program.exerciseName}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete ${program.name}")
                }
            }
            val lastResult = program.lastTestResult
            if (lastResult != null) {
                val progress = (lastResult / program.goalTarget).toFloat().coerceIn(0f, 1f)
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                Text(
                    "Last test: ${lastResult.toCleanString()} ${program.goalType.unitLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    "No test recorded yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun Double.toCleanString(): String = if (this == this.toLong().toDouble()) this.toLong().toString() else toString()

