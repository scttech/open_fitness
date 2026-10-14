package com.scttech.android.kotlin.openfitness.ui.messages

import com.scttech.android.kotlin.openfitness.data.repository.ProgramRepository
import com.scttech.android.kotlin.openfitness.data.repository.WorkoutRepository
import com.scttech.android.kotlin.openfitness.domain.model.AlertDestinationType
import com.scttech.android.kotlin.openfitness.ui.navigation.CalendarRoute
import com.scttech.android.kotlin.openfitness.ui.navigation.MessageCenterRoute
import com.scttech.android.kotlin.openfitness.ui.navigation.ProgramDetailRoute
import com.scttech.android.kotlin.openfitness.ui.navigation.ProgramTestCheckInRoute
import com.scttech.android.kotlin.openfitness.ui.navigation.WorkoutDetailRoute
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/** Turns an alert's destination into the screen to open, falling back to the Message Center if the target was deleted. */
@Singleton
class AlertRouteResolver @Inject constructor(
    private val programRepository: ProgramRepository,
    private val workoutRepository: WorkoutRepository,
) {

    suspend fun resolve(type: AlertDestinationType?, id: Long?): Any = when (type) {
        AlertDestinationType.PROGRAM ->
            if (id != null && programRepository.observeProgram(id).first() != null) ProgramDetailRoute(id) else MessageCenterRoute
        AlertDestinationType.PROGRAM_TEST ->
            if (id != null && programRepository.observeProgram(id).first() != null) ProgramTestCheckInRoute(id) else MessageCenterRoute
        AlertDestinationType.WORKOUT ->
            if (id != null && workoutRepository.observeWorkout(id).first() != null) WorkoutDetailRoute(id) else MessageCenterRoute
        AlertDestinationType.CALENDAR -> CalendarRoute
        null -> MessageCenterRoute
    }
}
