package com.scttech.android.kotlin.openfitness.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.scttech.android.kotlin.openfitness.ui.navigation.ExercisesRoute
import com.scttech.android.kotlin.openfitness.ui.navigation.OpenFitnessNavHost
import com.scttech.android.kotlin.openfitness.ui.navigation.ProgramsRoute
import com.scttech.android.kotlin.openfitness.ui.navigation.SettingsRoute
import com.scttech.android.kotlin.openfitness.ui.navigation.TopLevelDestination
import com.scttech.android.kotlin.openfitness.ui.navigation.WorkoutsRoute

@Composable
fun OpenFitnessApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val showBottomBar = TopLevelDestination.entries.any { topLevel ->
        currentDestination?.hierarchy?.any { it.hasRoute(topLevel.routeClass()) } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { destination ->
                        val selected = currentDestination?.hierarchy?.any { it.hasRoute(destination.routeClass()) } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = { navigateToTopLevel(navController, destination) },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        OpenFitnessNavHost(navController = navController, modifier = Modifier.padding(padding))
    }
}

private fun navigateToTopLevel(navController: NavHostController, destination: TopLevelDestination) {
    val route = when (destination) {
        TopLevelDestination.PROGRAMS -> ProgramsRoute
        TopLevelDestination.WORKOUTS -> WorkoutsRoute
        TopLevelDestination.EXERCISES -> ExercisesRoute
        TopLevelDestination.SETTINGS -> SettingsRoute
    }
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

private fun TopLevelDestination.routeClass() = when (this) {
    TopLevelDestination.PROGRAMS -> ProgramsRoute::class
    TopLevelDestination.WORKOUTS -> WorkoutsRoute::class
    TopLevelDestination.EXERCISES -> ExercisesRoute::class
    TopLevelDestination.SETTINGS -> SettingsRoute::class
}
