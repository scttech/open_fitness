package com.scttech.android.kotlin.openfitness.ui.weight

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.scttech.android.kotlin.openfitness.domain.model.WeightEntry
import com.scttech.android.kotlin.openfitness.ui.common.EmptyState
import com.scttech.android.kotlin.openfitness.ui.common.FullScreenLoading
import com.scttech.android.kotlin.openfitness.ui.common.TextInputDialog

@Composable
internal fun WeightRoute(
    modifier: Modifier = Modifier,
    viewModel: WeightViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    WeightScreen(
        uiState = uiState,
        onAddEntry = viewModel::addEntry,
        onDeleteEntry = viewModel::deleteEntry,
        modifier = modifier,
    )
}

@Composable
internal fun WeightScreen(
    uiState: WeightUiState,
    onAddEntry: (Double) -> Unit,
    onDeleteEntry: (WeightEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Weight") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Log weight")
            }
        },
    ) { padding ->
        when (uiState) {
            WeightUiState.Loading -> FullScreenLoading(Modifier.padding(padding))
            is WeightUiState.Success -> {
                if (uiState.entries.isEmpty()) {
                    EmptyState("No weight entries yet. Tap + to log your first one.", Modifier.padding(padding))
                } else {
                    Column(modifier = Modifier.padding(padding)) {
                        WeightChart(entries = uiState.entries, modifier = Modifier.fillMaxWidth().height(220.dp).padding(16.dp))
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(uiState.entries.sortedByDescending { it.date }, key = { it.id }) { entry ->
                                WeightEntryRow(entry = entry, onDelete = { onDeleteEntry(entry) })
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        TextInputDialog(
            title = "Log weight",
            label = "Weight (kg)",
            confirmLabel = "Add",
            onConfirm = { value ->
                value.toDoubleOrNull()?.let(onAddEntry)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false },
        )
    }
}

@Composable
private fun WeightChart(entries: List<WeightEntry>, modifier: Modifier = Modifier) {
    val sorted = remember(entries) { entries.sortedBy { it.date } }
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(sorted) {
        modelProducer.runTransaction {
            lineModel {
                series(x = sorted.indices.toList(), y = sorted.map { it.weightKg })
            }
        }
    }

    val bottomFormatter = remember(sorted) {
        CartesianValueFormatter { _, value, _ -> sorted.getOrNull(value.toInt())?.date?.toString().orEmpty() }
    }

    ProvideVicoTheme(rememberM3VicoTheme()) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = bottomFormatter),
            ),
            modelProducer = modelProducer,
            modifier = modifier,
        )
    }
}

@Composable
private fun WeightEntryRow(entry: WeightEntry, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("${entry.weightKg} kg", style = MaterialTheme.typography.titleLarge)
                Text(entry.date.toString(), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete entry")
            }
        }
    }
}
