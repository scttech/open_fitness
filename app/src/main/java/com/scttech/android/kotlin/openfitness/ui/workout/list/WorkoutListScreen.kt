package com.scttech.android.kotlin.openfitness.ui.workout.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.scttech.android.kotlin.openfitness.domain.model.Workout
import com.scttech.android.kotlin.openfitness.ui.common.EmptyState
import com.scttech.android.kotlin.openfitness.ui.common.FullScreenLoading

@Composable
internal fun WorkoutListRoute(
    onWorkoutClick: (Long) -> Unit,
    onNewWorkout: () -> Unit,
    onBrowseTemplates: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkoutListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    WorkoutListScreen(
        uiState = uiState,
        onWorkoutClick = onWorkoutClick,
        onNewWorkout = onNewWorkout,
        onBrowseTemplates = onBrowseTemplates,
        onDeleteWorkout = viewModel::deleteWorkout,
        modifier = modifier,
    )
}

@Composable
internal fun WorkoutListScreen(
    uiState: WorkoutListUiState,
    onWorkoutClick: (Long) -> Unit,
    onNewWorkout: () -> Unit,
    onBrowseTemplates: () -> Unit,
    onDeleteWorkout: (Workout) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("My Workouts") },
                actions = {
                    IconButton(onClick = onBrowseTemplates) {
                        Icon(Icons.Filled.LibraryBooks, contentDescription = "Browse templates")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewWorkout) {
                Icon(Icons.Filled.Add, contentDescription = "New workout")
            }
        },
    ) { padding ->
        when (uiState) {
            WorkoutListUiState.Loading -> FullScreenLoading(Modifier.padding(padding))
            is WorkoutListUiState.Success -> {
                if (uiState.workouts.isEmpty()) {
                    EmptyState(
                        message = "No workouts yet. Tap + to build one, or browse starter templates.",
                        modifier = Modifier.padding(padding),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(uiState.workouts, key = { it.id }) { workout ->
                            WorkoutRow(
                                workout = workout,
                                onClick = { onWorkoutClick(workout.id) },
                                onDelete = { onDeleteWorkout(workout) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutRow(
    workout: Workout,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(workout.name, style = MaterialTheme.typography.titleLarge)
                Text(
                    workout.style.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete ${workout.name}")
            }
        }
    }
}
