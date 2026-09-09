package com.scttech.android.kotlin.openfitness.ui.workout.templates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
internal fun TemplateBrowserRoute(
    onBackClick: () -> Unit,
    onWorkoutAdded: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TemplateBrowserViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.workoutAdded.collect { onWorkoutAdded(it) }
    }

    TemplateBrowserScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onUseTemplate = viewModel::useTemplate,
        modifier = modifier,
    )
}

@Composable
internal fun TemplateBrowserScreen(
    uiState: TemplateBrowserUiState,
    onBackClick: () -> Unit,
    onUseTemplate: (Workout) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Starter Templates") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        when (uiState) {
            TemplateBrowserUiState.Loading -> FullScreenLoading(Modifier.padding(padding))
            is TemplateBrowserUiState.Success -> {
                if (uiState.templates.isEmpty()) {
                    EmptyState("No templates available.", Modifier.padding(padding))
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(uiState.templates, key = { it.id }) { template ->
                            TemplateCard(template = template, onUseTemplate = { onUseTemplate(template) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplateCard(
    template: Workout,
    onUseTemplate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(template.style.displayName, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(template.name, style = MaterialTheme.typography.titleLarge)
            if (template.notes.isNotBlank()) {
                Text(
                    template.notes,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(onClick = onUseTemplate) { Text("Add to my workouts") }
            }
        }
    }
}
