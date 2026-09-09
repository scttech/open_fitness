package com.scttech.android.kotlin.openfitness.ui.exercise.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scttech.android.kotlin.openfitness.domain.model.Exercise
import com.scttech.android.kotlin.openfitness.ui.common.EmptyState
import com.scttech.android.kotlin.openfitness.ui.common.FullScreenLoading

@Composable
internal fun ExerciseListRoute(
    onExerciseClick: (Long) -> Unit,
    onNewExercise: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExerciseListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ExerciseListScreen(
        uiState = uiState,
        onQueryChange = viewModel::updateQuery,
        onExerciseClick = onExerciseClick,
        onNewExercise = onNewExercise,
        modifier = modifier,
    )
}

@Composable
internal fun ExerciseListScreen(
    uiState: ExerciseListUiState,
    onQueryChange: (String) -> Unit,
    onExerciseClick: (Long) -> Unit,
    onNewExercise: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Exercises") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewExercise) {
                Icon(Icons.Filled.Add, contentDescription = "New exercise")
            }
        },
    ) { padding ->
        when (uiState) {
            ExerciseListUiState.Loading -> FullScreenLoading(Modifier.padding(padding))
            is ExerciseListUiState.Success -> {
                Column(modifier = Modifier.padding(padding)) {
                    OutlinedTextField(
                        value = uiState.query,
                        onValueChange = onQueryChange,
                        label = { Text("Search exercises") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                    if (uiState.exercises.isEmpty()) {
                        EmptyState("No exercises found.")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(uiState.exercises, key = { it.id }) { exercise ->
                                ExerciseRow(exercise = exercise, onClick = { onExerciseClick(exercise.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun ExerciseRow(exercise: Exercise, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(exercise.name, style = MaterialTheme.typography.titleLarge)
            Text(
                "${exercise.category.displayName} · ${exercise.equipment.displayName}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
