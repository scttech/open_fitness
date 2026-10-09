package com.scttech.android.kotlin.openfitness.domain.badge

import com.scttech.android.kotlin.openfitness.domain.model.Badge
import com.scttech.android.kotlin.openfitness.domain.model.BadgeStats
import com.scttech.android.kotlin.openfitness.domain.model.Program
import com.scttech.android.kotlin.openfitness.domain.model.ProgramGoalType
import com.scttech.android.kotlin.openfitness.domain.model.ProgramSession
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutSession
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutStyle
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BadgeEvaluatorTest {

    private val t = Instant.parse("2026-10-01T10:00:00Z")

    private fun workout(style: WorkoutStyle, completed: Boolean = true) = WorkoutSession(
        profileId = 1, workoutId = 1, workoutName = "w", style = style,
        startedAt = t, completedAt = if (completed) t else null,
    )

    private fun programSession(completed: Boolean = true) = ProgramSession(
        profileId = 1, programId = 1, programName = "p", startedAt = t, completedAt = if (completed) t else null,
    )

    private fun program(goal: Int, lastTest: Int?) = Program(
        profileId = 1, name = "p", exerciseId = null, exerciseName = "Push-Up",
        goalType = ProgramGoalType.REPS, goalTarget = goal, lastTestResult = lastTest, createdAt = t,
    )

    @Test
    fun `no activity earns nothing`() {
        assertEquals(emptySet<Badge>(), BadgeEvaluator.earned(BadgeStats()))
    }

    @Test
    fun `workout count badges unlock at their thresholds`() {
        assertEquals(setOf(Badge.FIRST_WORKOUT), BadgeEvaluator.earned(BadgeStats(completedWorkouts = 1)))
        assertEquals(
            setOf(Badge.FIRST_WORKOUT, Badge.WORKOUTS_10),
            BadgeEvaluator.earned(BadgeStats(completedWorkouts = 10)),
        )
        assertTrue(Badge.WORKOUTS_100 in BadgeEvaluator.earned(BadgeStats(completedWorkouts = 100)))
        assertFalse(Badge.WORKOUTS_100 in BadgeEvaluator.earned(BadgeStats(completedWorkouts = 99)))
    }

    @Test
    fun `stats ignore incomplete sessions and count distinct styles`() {
        val stats = BadgeEvaluator.statsFrom(
            workoutSessions = listOf(
                workout(WorkoutStyle.EMOM),
                workout(WorkoutStyle.EMOM),
                workout(WorkoutStyle.TABATA),
                workout(WorkoutStyle.DENSITY, completed = false),
            ),
            programSessions = listOf(programSession(), programSession(completed = false)),
            programs = emptyList(),
            weightEntryCount = 0,
        )
        assertEquals(3, stats.completedWorkouts)
        assertEquals(2, stats.distinctWorkoutStyles)
        assertEquals(1, stats.completedProgramSessions)
    }

    @Test
    fun `a program goal is reached once the latest test meets the target`() {
        assertFalse(BadgeEvaluator.isGoalReached(program(goal = 100, lastTest = null)))
        assertFalse(BadgeEvaluator.isGoalReached(program(goal = 100, lastTest = 99)))
        assertTrue(BadgeEvaluator.isGoalReached(program(goal = 100, lastTest = 100)))
    }

    @Test
    fun `program badges follow created, tested and goal reached counts`() {
        val stats = BadgeEvaluator.statsFrom(
            workoutSessions = emptyList(),
            programSessions = emptyList(),
            programs = listOf(program(100, 100), program(50, 20), program(10, null)),
            weightEntryCount = 2,
        )
        assertEquals(3, stats.programsCreated)
        assertEquals(2, stats.programsTested)
        assertEquals(1, stats.programsGoalReached)
        assertEquals(
            setOf(Badge.FIRST_PROGRAM, Badge.FIRST_TEST, Badge.PROGRAM_COMPLETE_1, Badge.FIRST_WEIGHT_ENTRY),
            BadgeEvaluator.earned(stats),
        )
    }

    @Test
    fun `streak badges unlock at their thresholds`() {
        assertEquals(
            setOf(Badge.DAY_STREAK_3),
            BadgeEvaluator.earned(BadgeStats(longestDayStreak = 3, longestWeekStreak = 3)),
        )
        assertEquals(
            setOf(Badge.DAY_STREAK_3, Badge.DAY_STREAK_7, Badge.WEEK_STREAK_4, Badge.WEEK_STREAK_8),
            BadgeEvaluator.earned(BadgeStats(longestDayStreak = 7, longestWeekStreak = 8)),
        )
        assertTrue(Badge.WEEK_STREAK_12 in BadgeEvaluator.earned(BadgeStats(longestWeekStreak = 12)))
    }

    @Test
    fun `streaks combine workout and program sessions on the same calendar days`() {
        fun at(day: Int) = Instant.parse("2026-10-0${day}T12:00:00Z")
        val stats = BadgeEvaluator.statsFrom(
            workoutSessions = listOf(
                WorkoutSession(profileId = 1, workoutId = 1, workoutName = "w", style = WorkoutStyle.EMOM, startedAt = at(1), completedAt = at(1)),
                WorkoutSession(profileId = 1, workoutId = 1, workoutName = "w", style = WorkoutStyle.EMOM, startedAt = at(3), completedAt = at(3)),
            ),
            programSessions = listOf(
                ProgramSession(profileId = 1, programId = 1, programName = "p", startedAt = at(2), completedAt = at(2)),
                // Incomplete sessions never extend a streak.
                ProgramSession(profileId = 1, programId = 1, programName = "p", startedAt = at(4)),
            ),
            programs = emptyList(),
            weightEntryCount = 0,
            timeZone = TimeZone.UTC,
        )
        assertEquals(3, stats.longestDayStreak)
        assertEquals(1, stats.longestWeekStreak)
    }
}
