package com.scttech.android.kotlin.openfitness.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scttech.android.kotlin.openfitness.domain.model.SessionResult
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutSession
import com.scttech.android.kotlin.openfitness.ui.common.EmptyState
import com.scttech.android.kotlin.openfitness.ui.common.FullScreenLoading
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun HistoryRoute(
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HistoryScreen(uiState = uiState, modifier = modifier)
}

@Composable
internal fun HistoryScreen(
    uiState: HistoryUiState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("History") }) },
    ) { padding ->
        when (uiState) {
            HistoryUiState.Loading -> FullScreenLoading(Modifier.padding(padding))
            is HistoryUiState.Success -> {
                if (uiState.sessions.isEmpty()) {
                    EmptyState("No sessions logged yet. Start a workout to see it here.", Modifier.padding(padding))
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(uiState.sessions, key = { it.id }) { session ->
                            SessionCard(session)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionCard(session: WorkoutSession, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(session.style.displayName, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(session.workoutName, style = MaterialTheme.typography.titleLarge)
            val localDateTime = session.startedAt.toLocalDateTime(TimeZone.currentSystemDefault())
            Text(
                "${localDateTime.date}  ${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            session.result?.let { result ->
                Text(resultSummary(result), style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

private fun resultSummary(result: SessionResult): String = when (result) {
    is SessionResult.TabataResult -> "${result.roundsCompleted} rounds · ${result.cyclesCompleted} cycles"
    is SessionResult.GreaseTheGrooveResult -> "${result.setsLogged.size} sets · ${result.setsLogged.sumOf { it.reps ?: 0 }} total reps"
    is SessionResult.PyramidResult -> "${result.setsLogged.size} sets · ${result.setsLogged.sumOf { it.reps ?: 0 }} total reps"
    is SessionResult.DensityResult -> "${result.roundsCompleted} rounds in ${result.elapsedSeconds / 60}m ${result.elapsedSeconds % 60}s"
    is SessionResult.StepLoadingResult -> "${result.setsLogged.size} sets · top set ${result.setsLogged.maxOfOrNull { it.weightKg ?: 0.0 } ?: 0.0}kg"
}
