package com.scttech.android.kotlin.openfitness.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

@Serializable
data object ProfilePickerRoute

@Serializable
data object WorkoutsRoute

@Serializable
data object HistoryRoute

@Serializable
data object WeightRoute

@Serializable
data object StatsRoute

@Serializable
data object TemplateBrowserRoute

@Serializable
data object SelectWorkoutStyleRoute

@Serializable
data class WorkoutBuilderRoute(val workoutId: Long = 0L, val newWorkoutStyle: String? = null)

@Serializable
data class WorkoutDetailRoute(val workoutId: Long)

@Serializable
data class ActiveSessionRoute(val workoutId: Long)

@Serializable
data class SessionDetailRoute(val sessionId: Long)

enum class TopLevelDestination(
    val label: String,
    val icon: ImageVector,
) {
    WORKOUTS("Workouts", Icons.Filled.FitnessCenter),
    HISTORY("History", Icons.Filled.History),
    WEIGHT("Weight", Icons.Filled.MonitorWeight),
    STATS("Stats", Icons.Filled.Assessment),
}
