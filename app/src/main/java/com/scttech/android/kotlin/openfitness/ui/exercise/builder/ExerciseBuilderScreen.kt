package com.scttech.android.kotlin.openfitness.ui.exercise.builder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.scttech.android.kotlin.openfitness.domain.model.ExerciseCategory
import com.scttech.android.kotlin.openfitness.domain.model.ExerciseEquipment
import com.scttech.android.kotlin.openfitness.ui.common.FullScreenLoading

@Composable
internal fun ExerciseBuilderRoute(
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExerciseBuilderViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is ExerciseBuilderUiState.Loaded && (uiState as ExerciseBuilderUiState.Loaded).saved) {
            onSaved()
        }
    }

    ExerciseBuilderScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onNameChange = viewModel::updateName,
        onCategoryChange = viewModel::updateCategory,
        onEquipmentChange = viewModel::updateEquipment,
        onFormNotesChange = viewModel::updateFormNotes,
        onSave = viewModel::save,
        modifier = modifier,
    )
}

@Composable
internal fun ExerciseBuilderScreen(
    uiState: ExerciseBuilderUiState,
    onBackClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onCategoryChange: (ExerciseCategory) -> Unit,
    onEquipmentChange: (ExerciseEquipment) -> Unit,
    onFormNotesChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (uiState is ExerciseBuilderUiState.Loaded && uiState.exerciseId != 0L) "Edit Exercise" else "New Exercise") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState is ExerciseBuilderUiState.Loaded) {
                        TextButton(onClick = onSave, enabled = uiState.canSave) { Text("Save") }
                    }
                },
            )
        },
    ) { padding ->
        when (uiState) {
            ExerciseBuilderUiState.Loading -> FullScreenLoading(Modifier.padding(padding))
            is ExerciseBuilderUiState.Loaded -> {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = onNameChange,
                        label = { Text("Exercise name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )

                    EnumDropdown(
                        label = "Category",
                        options = ExerciseCategory.entries,
                        selected = uiState.category,
                        displayName = { it.displayName },
                        onSelected = onCategoryChange,
                    )

                    EnumDropdown(
                        label = "Equipment",
                        options = ExerciseEquipment.entries,
                        selected = uiState.equipment,
                        displayName = { it.displayName },
                        onSelected = onEquipmentChange,
                    )

                    HorizontalDivider()

                    OutlinedTextField(
                        value = uiState.formNotes,
                        onValueChange = onFormNotesChange,
                        label = { Text("Form notes") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                    )

                    Button(onClick = onSave, enabled = uiState.canSave, modifier = Modifier.fillMaxWidth()) {
                        Text("Save exercise")
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> EnumDropdown(
    label: String,
    options: List<T>,
    selected: T,
    displayName: (T) -> String,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = displayName(selected),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(displayName(option)) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}
