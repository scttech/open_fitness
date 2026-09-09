package com.scttech.android.kotlin.openfitness.ui.program.session

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scttech.android.kotlin.openfitness.ui.common.FullScreenLoading

@Composable
internal fun ActiveProgramSessionRoute(
    onBackClick: () -> Unit,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActiveProgramSessionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is ActiveProgramSessionUiState.InProgress && (uiState as ActiveProgramSessionUiState.InProgress).isFinished) {
            onFinished()
        }
    }

    ActiveProgramSessionScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onValueInputChange = viewModel::updateValueInput,
        onLogSet = viewModel::logSet,
        onFinish = viewModel::finishSession,
        modifier = modifier,
    )
}

@Composable
internal fun ActiveProgramSessionScreen(
    uiState: ActiveProgramSessionUiState,
    onBackClick: () -> Unit,
    onValueInputChange: (String) -> Unit,
    onLogSet: () -> Unit,
    onFinish: () -> Unit,
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
                Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(uiState.program.exerciseName, style = MaterialTheme.typography.headlineSmall)
                    val target = uiState.nextSetTarget
                    Text(
                        if (target != null) "Target: $target ${uiState.program.goalType.unitLabel}" else "Free set - no prescription target",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    OutlinedTextField(
                        value = uiState.valueInput,
                        onValueChange = onValueInputChange,
                        label = { Text(uiState.program.goalType.unitLabel.replaceFirstChar { it.uppercase() }) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )

                    Button(onClick = onLogSet, modifier = Modifier.fillMaxWidth()) {
                        Text("Log set ${uiState.loggedSets.size + 1}")
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
