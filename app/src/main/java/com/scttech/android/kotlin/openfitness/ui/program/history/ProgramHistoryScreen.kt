package com.scttech.android.kotlin.openfitness.ui.program.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnModel
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.scttech.android.kotlin.openfitness.ui.common.FullScreenLoading

@Composable
internal fun ProgramHistoryRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProgramHistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProgramHistoryScreen(uiState = uiState, onBackClick = onBackClick, modifier = modifier)
}

@Composable
internal fun ProgramHistoryScreen(
    uiState: ProgramHistoryUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Program history") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        when (uiState) {
            ProgramHistoryUiState.Loading, ProgramHistoryUiState.NotFound ->
                FullScreenLoading(Modifier.padding(padding))
            is ProgramHistoryUiState.Success -> {
                val unit = uiState.program.goalType.unitLabel
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(uiState.program.name, style = MaterialTheme.typography.headlineSmall)

                    Text("Test results ($unit)", style = MaterialTheme.typography.titleLarge)
                    if (uiState.tests.isEmpty()) {
                        Text(
                            "Record a test to start charting your results.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        HistoryChart(
                            labels = uiState.tests.map { it.date.toString() },
                            values = uiState.tests.map { it.result },
                            asColumns = false,
                            modifier = Modifier.fillMaxWidth().height(220.dp),
                        )
                    }

                    Text("Total ${uiState.program.exerciseName} $unit per session", style = MaterialTheme.typography.titleLarge)
                    if (uiState.sessionTotals.isEmpty()) {
                        Text(
                            "Complete a session to start charting your totals.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        HistoryChart(
                            labels = uiState.sessionTotals.map { it.date.toString() },
                            values = uiState.sessionTotals.map { it.total },
                            asColumns = true,
                            modifier = Modifier.fillMaxWidth().height(220.dp),
                        )
                    }
                }
            }
        }
    }
}

/** A line (for a trend over time) or column (for per-session totals) chart over evenly spaced points. */
@Composable
private fun HistoryChart(
    labels: List<String>,
    values: List<Int>,
    asColumns: Boolean,
    modifier: Modifier = Modifier,
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(values, asColumns) {
        modelProducer.runTransaction {
            if (asColumns) {
                columnModel { series(y = values) }
            } else {
                lineModel { series(x = values.indices.toList(), y = values) }
            }
        }
    }

    val bottomFormatter = remember(labels) {
        CartesianValueFormatter { _, value, _ -> labels.getOrNull(value.toInt()).orEmpty() }
    }

    ProvideVicoTheme(rememberM3VicoTheme()) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                if (asColumns) rememberColumnCartesianLayer() else rememberLineCartesianLayer(),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = bottomFormatter),
            ),
            modelProducer = modelProducer,
            modifier = modifier,
        )
    }
}
