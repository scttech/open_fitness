package com.scttech.android.kotlin.openfitness.ui.session

/** Actions for a running [ActiveSessionUiState.TabataSession]. */
data class TabataActions(
    val onToggleRunning: () -> Unit,
    val onSkip: () -> Unit,
)

/** Actions for a running [ActiveSessionUiState.DensitySession]. */
data class DensityActions(
    val onToggleRunning: () -> Unit,
    val onIncrementRounds: () -> Unit,
    val onFinish: () -> Unit,
)

/** Actions for a running [ActiveSessionUiState.SetLoggingSession]. */
data class SetLoggingActions(
    val onRepsInputChange: (String) -> Unit,
    val onWeightInputChange: (String) -> Unit,
    val onLogSet: () -> Unit,
    val onFinish: () -> Unit,
    val onToggleRestRunning: () -> Unit,
    val onSkipRest: () -> Unit,
)

/** Actions for a running [ActiveSessionUiState.EmomSession]. */
data class EmomActions(
    val onToggleRunning: () -> Unit,
    val onSkip: () -> Unit,
)

/**
 * Every style-specific action group, mirroring [ActiveSessionUiState]'s variants one-for-one -
 * [ActiveSessionScreen] hands each session content composable only the slice it needs, instead of
 * every composable threading the full flat callback list regardless of which style is showing.
 */
data class ActiveSessionActions(
    val tabata: TabataActions,
    val density: DensityActions,
    val setLogging: SetLoggingActions,
    val emom: EmomActions,
    /** Shared across every style's completion screen. */
    val onDone: () -> Unit,
    /** Shared across every style - opens the exercise library entry for the tapped exercise name. */
    val onExerciseClick: (Long) -> Unit,
)
