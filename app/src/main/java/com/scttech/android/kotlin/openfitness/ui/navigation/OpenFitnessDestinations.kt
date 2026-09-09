package com.scttech.android.kotlin.openfitness.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
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

@Serializable
data object ExercisesRoute

@Serializable
data class ExerciseDetailRoute(val exerciseId: Long)

@Serializable
data class ExerciseBuilderRoute(val exerciseId: Long = 0L)

@Serializable
data object ProgramsRoute

@Serializable
data class ProgramBuilderRoute(val programId: Long = 0L)

@Serializable
data class ProgramDetailRoute(val programId: Long)

@Serializable
data class ProgramTestCheckInRoute(val programId: Long)

@Serializable
data class ActiveProgramSessionRoute(val programId: Long)

@Serializable
data object SettingsRoute

enum class TopLevelDestination(
    val label: String,
    val icon: ImageVector,
) {
    PROGRAMS("Programs", Icons.Filled.EmojiEvents),
    WORKOUTS("Workouts", Icons.Filled.FitnessCenter),
    EXERCISES("Exercises", Icons.Filled.MenuBook),
    SETTINGS("Settings", Icons.Filled.Settings),
}
