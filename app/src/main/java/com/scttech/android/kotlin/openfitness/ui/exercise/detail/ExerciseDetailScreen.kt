package com.scttech.android.kotlin.openfitness.ui.exercise.detail

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
import com.scttech.android.kotlin.openfitness.ui.common.ConfirmDialog
import com.scttech.android.kotlin.openfitness.ui.common.FullScreenLoading

@Composable
internal fun ExerciseDetailRoute(
    onBackClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExerciseDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.deleted.collect { onDeleted() }
    }

    ExerciseDetailScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onEditClick = onEditClick,
        onDeleteClick = viewModel::deleteExercise,
        modifier = modifier,
    )
}

@Composable
internal fun ExerciseDetailScreen(
    uiState: ExerciseDetailUiState,
    onBackClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Exercise") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState is ExerciseDetailUiState.Success) {
                        IconButton(onClick = { onEditClick(uiState.exercise.id) }) {
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
            ExerciseDetailUiState.Loading -> FullScreenLoading(Modifier.padding(padding))
            ExerciseDetailUiState.NotFound -> FullScreenLoading(Modifier.padding(padding))
            is ExerciseDetailUiState.Success -> {
                val exercise = uiState.exercise
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(exercise.name, style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "${exercise.category.displayName} · ${exercise.equipment.displayName}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    if (exercise.formNotes.isNotBlank()) {
                        HorizontalDivider()
                        Text("Form & notes", style = MaterialTheme.typography.titleLarge)
                        Text(exercise.formNotes, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        ConfirmDialog(
            title = "Delete exercise?",
            text = "Any workouts or programs linking to it will keep their exercise name but lose the link.",
            confirmLabel = "Delete",
            onConfirm = {
                showDeleteConfirm = false
                onDeleteClick()
            },
            onDismiss = { showDeleteConfirm = false },
        )
    }
}
