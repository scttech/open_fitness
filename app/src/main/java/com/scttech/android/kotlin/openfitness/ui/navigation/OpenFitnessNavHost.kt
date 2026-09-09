package com.scttech.android.kotlin.openfitness.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.scttech.android.kotlin.openfitness.ui.profile.ProfileUiState
import com.scttech.android.kotlin.openfitness.ui.exercise.builder.ExerciseBuilderRoute as ExerciseBuilderRouteScreen
import com.scttech.android.kotlin.openfitness.ui.exercise.detail.ExerciseDetailRoute as ExerciseDetailRouteScreen
import com.scttech.android.kotlin.openfitness.ui.exercise.list.ExerciseListRoute as ExerciseListRouteScreen
import com.scttech.android.kotlin.openfitness.ui.history.HistoryRoute as HistoryRouteScreen
import com.scttech.android.kotlin.openfitness.ui.profile.ProfileRoute as ProfileRouteScreen
import com.scttech.android.kotlin.openfitness.ui.program.builder.ProgramBuilderRoute as ProgramBuilderRouteScreen
import com.scttech.android.kotlin.openfitness.ui.program.detail.ProgramDetailRoute as ProgramDetailRouteScreen
import com.scttech.android.kotlin.openfitness.ui.program.list.ProgramListRoute as ProgramListRouteScreen
import com.scttech.android.kotlin.openfitness.ui.program.session.ActiveProgramSessionRoute as ActiveProgramSessionRouteScreen
import com.scttech.android.kotlin.openfitness.ui.program.testcheckin.ProgramTestCheckInRoute as ProgramTestCheckInRouteScreen
import com.scttech.android.kotlin.openfitness.ui.session.ActiveSessionRoute as ActiveSessionRouteScreen
import com.scttech.android.kotlin.openfitness.ui.settings.SettingsRoute as SettingsRouteScreen
import com.scttech.android.kotlin.openfitness.ui.stats.StatsRoute as StatsRouteScreen
import com.scttech.android.kotlin.openfitness.ui.weight.WeightRoute as WeightRouteScreen
import com.scttech.android.kotlin.openfitness.ui.workout.builder.WorkoutBuilderRoute as WorkoutBuilderRouteScreen
import com.scttech.android.kotlin.openfitness.ui.workout.detail.WorkoutDetailRoute as WorkoutDetailRouteScreen
import com.scttech.android.kotlin.openfitness.ui.workout.list.SelectWorkoutStyleRoute as SelectWorkoutStyleRouteScreen
import com.scttech.android.kotlin.openfitness.ui.workout.list.WorkoutListRoute as WorkoutListRouteScreen
import com.scttech.android.kotlin.openfitness.ui.workout.templates.TemplateBrowserRoute as TemplateBrowserRouteScreen

@Composable
fun OpenFitnessNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = ProfilePickerRoute,
        modifier = modifier,
    ) {
        composable<ProfilePickerRoute> {
            ProfileRouteScreen(
                onProfileSelected = {
                    navController.navigate(WorkoutsRoute) {
                        popUpTo(ProfilePickerRoute) { inclusive = true }
                    }
                },
            )
        }

        composable<WorkoutsRoute> {
            WorkoutListRouteScreen(
                onWorkoutClick = { id -> navController.navigate(WorkoutDetailRoute(id)) },
                onNewWorkout = { navController.navigate(SelectWorkoutStyleRoute) },
                onBrowseTemplates = { navController.navigate(TemplateBrowserRoute) },
            )
        }

        composable<SelectWorkoutStyleRoute> {
            SelectWorkoutStyleRouteScreen(
                onStyleSelected = { style ->
                    navController.popBackStack()
                    navController.navigate(WorkoutBuilderRoute(newWorkoutStyle = style.name))
                },
                onBackClick = { navController.popBackStack() },
            )
        }

        composable<HistoryRoute> { HistoryRouteScreen() }
        composable<WeightRoute> { WeightRouteScreen() }
        composable<StatsRoute> { StatsRouteScreen() }

        composable<TemplateBrowserRoute> {
            TemplateBrowserRouteScreen(
                onBackClick = { navController.popBackStack() },
                onWorkoutAdded = { id ->
                    navController.popBackStack()
                    navController.navigate(WorkoutDetailRoute(id))
                },
            )
        }

        composable<WorkoutBuilderRoute> {
            WorkoutBuilderRouteScreen(
                onBackClick = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable<WorkoutDetailRoute> {
            WorkoutDetailRouteScreen(
                onBackClick = { navController.popBackStack() },
                onEditClick = { id -> navController.navigate(WorkoutBuilderRoute(workoutId = id)) },
                onStartSession = { id -> navController.navigate(ActiveSessionRoute(id)) },
                onExerciseClick = { id -> navController.navigate(ExerciseDetailRoute(id)) },
                onDeleted = { navController.popBackStack() },
            )
        }

        composable<ActiveSessionRoute> {
            ActiveSessionRouteScreen(
                onBackClick = { navController.popBackStack() },
                onFinished = { navController.popBackStack() },
            )
        }

        composable<ExercisesRoute> {
            ExerciseListRouteScreen(
                onExerciseClick = { id -> navController.navigate(ExerciseDetailRoute(id)) },
                onNewExercise = { navController.navigate(ExerciseBuilderRoute()) },
            )
        }

        composable<ExerciseDetailRoute> {
            ExerciseDetailRouteScreen(
                onBackClick = { navController.popBackStack() },
                onEditClick = { id -> navController.navigate(ExerciseBuilderRoute(exerciseId = id)) },
                onDeleted = { navController.popBackStack() },
            )
        }

        composable<ExerciseBuilderRoute> {
            ExerciseBuilderRouteScreen(
                onBackClick = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable<ProgramsRoute> {
            ProgramListRouteScreen(
                onProgramClick = { id -> navController.navigate(ProgramDetailRoute(id)) },
                onNewProgram = { navController.navigate(ProgramBuilderRoute()) },
            )
        }

        composable<ProgramBuilderRoute> {
            ProgramBuilderRouteScreen(
                onBackClick = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable<ProgramDetailRoute> {
            ProgramDetailRouteScreen(
                onBackClick = { navController.popBackStack() },
                onEditClick = { id -> navController.navigate(ProgramBuilderRoute(programId = id)) },
                onRecordTest = { id -> navController.navigate(ProgramTestCheckInRoute(id)) },
                onStartSession = { id -> navController.navigate(ActiveProgramSessionRoute(id)) },
                onDeleted = { navController.popBackStack() },
            )
        }

        composable<ProgramTestCheckInRoute> {
            ProgramTestCheckInRouteScreen(
                onBackClick = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable<ActiveProgramSessionRoute> {
            ActiveProgramSessionRouteScreen(
                onBackClick = { navController.popBackStack() },
                onFinished = { navController.popBackStack() },
            )
        }

        composable<SettingsRoute> {
            SettingsRouteScreen(
                onSwitchProfile = {
                    navController.navigate(ProfilePickerRoute) { popUpTo(0) }
                },
                onHistoryClick = { navController.navigate(HistoryRoute) },
                onWeightClick = { navController.navigate(WeightRoute) },
                onStatsClick = { navController.navigate(StatsRoute) },
            )
        }
    }
}
