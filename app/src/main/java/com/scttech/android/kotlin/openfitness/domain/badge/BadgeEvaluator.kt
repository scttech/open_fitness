package com.scttech.android.kotlin.openfitness.domain.badge

import com.scttech.android.kotlin.openfitness.domain.model.Badge
import com.scttech.android.kotlin.openfitness.domain.model.BadgeStats
import com.scttech.android.kotlin.openfitness.domain.model.Program
import com.scttech.android.kotlin.openfitness.domain.model.ProgramSession
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutSession
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** Pure rules deciding which [Badge]s a profile's current [BadgeStats] qualify for. */
object BadgeEvaluator {

    fun earned(stats: BadgeStats): Set<Badge> = buildSet {
        if (stats.completedWorkouts >= 1) add(Badge.FIRST_WORKOUT)
        if (stats.completedWorkouts >= 10) add(Badge.WORKOUTS_10)
        if (stats.completedWorkouts >= 25) add(Badge.WORKOUTS_25)
        if (stats.completedWorkouts >= 50) add(Badge.WORKOUTS_50)
        if (stats.completedWorkouts >= 100) add(Badge.WORKOUTS_100)
        if (stats.distinctWorkoutStyles >= 4) add(Badge.STYLE_SAMPLER)

        if (stats.programsCreated >= 1) add(Badge.FIRST_PROGRAM)
        if (stats.programsTested >= 1) add(Badge.FIRST_TEST)
        if (stats.completedProgramSessions >= 1) add(Badge.FIRST_PROGRAM_SESSION)
        if (stats.completedProgramSessions >= 10) add(Badge.PROGRAM_SESSIONS_10)
        if (stats.completedProgramSessions >= 25) add(Badge.PROGRAM_SESSIONS_25)
        if (stats.programsGoalReached >= 1) add(Badge.PROGRAM_COMPLETE_1)
        if (stats.programsGoalReached >= 3) add(Badge.PROGRAM_COMPLETE_3)

        if (stats.weightEntries >= 1) add(Badge.FIRST_WEIGHT_ENTRY)

        if (stats.longestDayStreak >= 3) add(Badge.DAY_STREAK_3)
        if (stats.longestDayStreak >= 7) add(Badge.DAY_STREAK_7)
        if (stats.longestWeekStreak >= 4) add(Badge.WEEK_STREAK_4)
        if (stats.longestWeekStreak >= 8) add(Badge.WEEK_STREAK_8)
        if (stats.longestWeekStreak >= 12) add(Badge.WEEK_STREAK_12)
    }

    /** A program's goal is reached once its latest test meets or beats the goal target. */
    fun isGoalReached(program: Program): Boolean =
        program.lastTestResult?.let { it >= program.goalTarget } == true

    fun statsFrom(
        workoutSessions: List<WorkoutSession>,
        programSessions: List<ProgramSession>,
        programs: List<Program>,
        weightEntryCount: Int,
        timeZone: TimeZone = TimeZone.currentSystemDefault(),
    ): BadgeStats {
        val completedWorkouts = workoutSessions.filter { it.isCompleted }
        val completedProgramSessions = programSessions.filter { it.isCompleted }
        val activeDates = completedWorkouts.map { it.startedAt.toLocalDateTime(timeZone).date } +
            completedProgramSessions.map { it.startedAt.toLocalDateTime(timeZone).date }
        return BadgeStats(
            completedWorkouts = completedWorkouts.size,
            distinctWorkoutStyles = completedWorkouts.map { it.style }.distinct().size,
            programsCreated = programs.size,
            programsTested = programs.count { it.lastTestResult != null },
            completedProgramSessions = completedProgramSessions.size,
            programsGoalReached = programs.count(::isGoalReached),
            weightEntries = weightEntryCount,
            longestDayStreak = StreakCalculator.longestDayStreak(activeDates),
            longestWeekStreak = StreakCalculator.longestWeekStreak(activeDates),
        )
    }
}
