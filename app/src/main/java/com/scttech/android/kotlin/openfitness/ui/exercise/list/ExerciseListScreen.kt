package com.scttech.android.kotlin.openfitness.ui.exercise.list

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scttech.android.kotlin.openfitness.domain.model.Exercise
import com.scttech.android.kotlin.openfitness.domain.model.ExerciseCategory
import com.scttech.android.kotlin.openfitness.domain.model.ExerciseEquipment
import com.scttech.android.kotlin.openfitness.ui.common.EmptyState
import com.scttech.android.kotlin.openfitness.ui.common.FullScreenLoading

@Composable
internal fun ExerciseListRoute(
    onExerciseClick: (Long) -> Unit,
    onNewExercise: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExerciseListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ExerciseListScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onQueryChange = viewModel::updateQuery,
        onCategoryFilterChange = viewModel::updateCategoryFilter,
        onEquipmentFilterChange = viewModel::updateEquipmentFilter,
        onExerciseClick = onExerciseClick,
        onNewExercise = onNewExercise,
        modifier = modifier,
    )
}

@Composable
internal fun ExerciseListScreen(
    uiState: ExerciseListUiState,
    onBackClick: () -> Unit,
    onQueryChange: (String) -> Unit,
    onCategoryFilterChange: (ExerciseCategory?) -> Unit,
    onEquipmentFilterChange: (ExerciseEquipment?) -> Unit,
    onExerciseClick: (Long) -> Unit,
    onNewExercise: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Exercises") },                 navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },) },
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
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        EnumFilterDropdown(
                            label = "Category",
                            options = ExerciseCategory.entries,
                            selected = uiState.selectedCategory,
                            optionLabel = { it.displayName },
                            onSelected = onCategoryFilterChange,
                            modifier = Modifier.weight(1f),
                        )
                        EnumFilterDropdown(
                            label = "Equipment",
                            options = ExerciseEquipment.entries,
                            selected = uiState.selectedEquipment,
                            optionLabel = { it.displayName },
                            onSelected = onEquipmentFilterChange,
                            modifier = Modifier.weight(1f),
                        )
                    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> EnumFilterDropdown(
    label: String,
    options: List<T>,
    selected: T?,
    optionLabel: (T) -> String,
    onSelected: (T?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selected?.let(optionLabel) ?: "All",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            singleLine = true,
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("All") },
                onClick = {
                    onSelected(null)
                    expanded = false
                },
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                )
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
