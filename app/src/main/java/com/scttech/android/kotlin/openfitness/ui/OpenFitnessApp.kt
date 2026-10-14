package com.scttech.android.kotlin.openfitness.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scttech.android.kotlin.openfitness.ui.badges.BadgeAnnouncerViewModel
import com.scttech.android.kotlin.openfitness.ui.messages.AppAlertsViewModel
import com.scttech.android.kotlin.openfitness.ui.navigation.ProfilePickerRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.scttech.android.kotlin.openfitness.ui.navigation.CalendarRoute
import com.scttech.android.kotlin.openfitness.ui.navigation.ExercisesRoute
import com.scttech.android.kotlin.openfitness.ui.navigation.OpenFitnessNavHost
import com.scttech.android.kotlin.openfitness.ui.navigation.ProgramsRoute
import com.scttech.android.kotlin.openfitness.ui.navigation.ProfileTabRoute
import com.scttech.android.kotlin.openfitness.ui.navigation.TopLevelDestination
import com.scttech.android.kotlin.openfitness.ui.navigation.WorkoutsRoute

@Composable
fun OpenFitnessApp(
    badgeAnnouncer: BadgeAnnouncerViewModel = hiltViewModel(),
    alerts: AppAlertsViewModel = hiltViewModel(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(badgeAnnouncer) {
        badgeAnnouncer.newlyEarned.collect { badge ->
            snackbarHostState.showSnackbar("Badge earned: ${badge.displayName}")
        }
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val unreadAlerts by alerts.unreadCount.collectAsStateWithLifecycle()
    val pendingLink by alerts.pendingLink.collectAsStateWithLifecycle()
    val onProfilePicker = currentDestination?.hasRoute(ProfilePickerRoute::class) != false

    // A tapped alert opens its screen once the user is past the profile picker.
    LaunchedEffect(pendingLink, onProfilePicker) {
        if (pendingLink != null && !onProfilePicker) {
            alerts.openPendingLink()?.let { navController.navigate(it) }
        }
    }

    val showBottomBar = TopLevelDestination.entries.any { topLevel ->
        currentDestination?.hierarchy?.any { it.hasRoute(topLevel.routeClass()) } == true
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { destination ->
                        val selected = currentDestination?.hierarchy?.any { it.hasRoute(destination.routeClass()) } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = { navigateToTopLevel(navController, destination) },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (destination == TopLevelDestination.PROFILE && unreadAlerts > 0) Badge()
                                    },
                                ) { Icon(destination.icon, contentDescription = destination.label) }
                            },
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
        TopLevelDestination.CALENDAR -> CalendarRoute
        TopLevelDestination.WORKOUTS -> WorkoutsRoute
        TopLevelDestination.EXERCISES -> ExercisesRoute
        TopLevelDestination.PROFILE -> ProfileTabRoute
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
    TopLevelDestination.CALENDAR -> CalendarRoute::class
    TopLevelDestination.WORKOUTS -> WorkoutsRoute::class
    TopLevelDestination.EXERCISES -> ExercisesRoute::class
    TopLevelDestination.PROFILE -> ProfileTabRoute::class
}
