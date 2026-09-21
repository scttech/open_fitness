package com.scttech.android.kotlin.openfitness.ui.common.timer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shared countdown UI for any phase-based workout/program timer (Tabata, program/set rest, ...).
 * Colors by [TimerPhase.kind] so work and rest phases are visually distinct.
 */
@Composable
fun PhaseTimerDisplay(
    state: PhaseTimerState,
    workColor: Color,
    restColor: Color,
    onPauseResume: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
    skipLabel: String = "Skip",
    onExerciseClick: ((Long) -> Unit)? = null,
    /** Label shown under "Coming up...". Defaults to the next phase in [state]; pass an override for single-phase (rest-only) timers, where the next thing isn't another phase. */
    comingUpLabel: String? = state.nextPhase?.label,
) {
    val phase = state.currentPhase ?: return
    val color = when (phase.kind) {
        TimerPhaseKind.WORK -> workColor
        TimerPhaseKind.REST -> restColor
    }
    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val exerciseId = phase.exerciseId
        Text(
            phase.label.uppercase(),
            style = MaterialTheme.typography.headlineSmall,
            color = color,
            modifier = if (exerciseId != null && onExerciseClick != null) {
                Modifier.clickable { onExerciseClick(exerciseId) }
            } else {
                Modifier
            },
        )
        phase.subtitle?.let { subtitle ->
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(8.dp))
        Text(state.secondsRemaining.toString(), fontSize = 96.sp, fontWeight = FontWeight.Bold, color = color)
        Spacer(Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = { state.progress },
            modifier = Modifier.fillMaxWidth(),
            color = color,
        )
        if (comingUpLabel != null) {
            Spacer(Modifier.height(16.dp))
            Text("Coming up...", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(comingUpLabel, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val runButtonLabel = when {
                !state.hasStarted -> "Start"
                state.isRunning -> "Pause"
                else -> "Resume"
            }
            FilledTonalButton(onClick = onPauseResume) {
                Icon(if (state.isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(runButtonLabel)
            }
            OutlinedButton(onClick = onSkip) {
                Icon(Icons.Filled.SkipNext, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(skipLabel)
            }
        }
    }
}
