package com.scttech.android.kotlin.openfitness.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
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
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.scttech.android.kotlin.openfitness.ui.common.EmptyState
import com.scttech.android.kotlin.openfitness.ui.common.FullScreenLoading

@Composable
internal fun StatsRoute(
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    StatsScreen(uiState = uiState, modifier = modifier)
}

@Composable
internal fun StatsScreen(
    uiState: StatsUiState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Stats") }) },
    ) { padding ->
        when (uiState) {
            StatsUiState.Loading -> FullScreenLoading(Modifier.padding(padding))
            is StatsUiState.Success -> {
                if (uiState.totalSessions == 0) {
                    EmptyState("Complete a session to start seeing stats here.", Modifier.padding(padding))
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Total sessions", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(uiState.totalSessions.toString(), style = MaterialTheme.typography.headlineSmall)
                                }
                            }
                        }
                        item {
                            Text("Sessions per week", style = MaterialTheme.typography.titleLarge)
                            FrequencyChart(
                                buckets = uiState.weeklyBuckets,
                                modifier = Modifier.fillMaxWidth().height(200.dp).padding(top = 8.dp),
                            )
                        }
                        item { HorizontalDivider() }
                        item { Text("By style", style = MaterialTheme.typography.titleLarge) }
                        items(uiState.sessionsByStyle.entries.toList()) { (style, count) ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(style)
                                Text(count.toString())
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FrequencyChart(buckets: List<WeekBucket>, modifier: Modifier = Modifier) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(buckets) {
        modelProducer.runTransaction {
            columnModel {
                series(y = buckets.map { it.sessionCount })
            }
        }
    }

    val bottomFormatter = remember(buckets) {
        CartesianValueFormatter { _, value, _ -> buckets.getOrNull(value.toInt())?.weekStart?.toString().orEmpty() }
    }

    ProvideVicoTheme(rememberM3VicoTheme()) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = bottomFormatter),
            ),
            modelProducer = modelProducer,
            modifier = modifier,
        )
    }
}
