package com.scttech.android.kotlin.openfitness.domain.model

import kotlinx.datetime.Instant

enum class BadgeCategory(val displayName: String) {
    WORKOUTS("Workouts"),
    PROGRAMS("Programs"),
    BODY("Body"),
    STREAKS("Streaks"),
}

/**
 * An achievement a profile can earn. Persisted by [name], so entries must never be renamed - only
 * added. Unlock rules live in [com.scttech.android.kotlin.openfitness.domain.badge.BadgeEvaluator].
 */
enum class Badge(val displayName: String, val description: String, val category: BadgeCategory) {
    FIRST_WORKOUT("First Workout", "Complete your first workout.", BadgeCategory.WORKOUTS),
    WORKOUTS_10("Getting Going", "Complete 10 workouts.", BadgeCategory.WORKOUTS),
    WORKOUTS_25("Regular", "Complete 25 workouts.", BadgeCategory.WORKOUTS),
    WORKOUTS_50("Dedicated", "Complete 50 workouts.", BadgeCategory.WORKOUTS),
    WORKOUTS_100("Centurion", "Complete 100 workouts.", BadgeCategory.WORKOUTS),
    STYLE_SAMPLER("Style Sampler", "Complete workouts in 4 different styles.", BadgeCategory.WORKOUTS),

    FIRST_PROGRAM("Goal Setter", "Create your first program.", BadgeCategory.PROGRAMS),
    FIRST_TEST("Put to the Test", "Record your first program test.", BadgeCategory.PROGRAMS),
    FIRST_PROGRAM_SESSION("Program Started", "Complete your first program session.", BadgeCategory.PROGRAMS),
    PROGRAM_SESSIONS_10("Program Regular", "Complete 10 program sessions.", BadgeCategory.PROGRAMS),
    PROGRAM_SESSIONS_25("Program Devotee", "Complete 25 program sessions.", BadgeCategory.PROGRAMS),
    PROGRAM_COMPLETE_1("Goal Crusher", "Reach the goal of a program.", BadgeCategory.PROGRAMS),
    PROGRAM_COMPLETE_3("Hat Trick", "Reach the goal of 3 programs.", BadgeCategory.PROGRAMS),

    FIRST_WEIGHT_ENTRY("On the Scale", "Log your body weight for the first time.", BadgeCategory.BODY),

    DAY_STREAK_3("On a Roll", "Train 3 days in a row.", BadgeCategory.STREAKS),
    DAY_STREAK_7("Week Warrior", "Train 7 days in a row.", BadgeCategory.STREAKS),
    WEEK_STREAK_4("Consistent", "Train in 4 weeks in a row.", BadgeCategory.STREAKS),
    WEEK_STREAK_8("Habit Formed", "Train in 8 weeks in a row.", BadgeCategory.STREAKS),
    WEEK_STREAK_12("Iron Habit", "Train in 12 weeks in a row.", BadgeCategory.STREAKS),
}

/** A [Badge] a profile has earned, and when. */
data class EarnedBadge(val badge: Badge, val earnedAt: Instant)

/** The counts [com.scttech.android.kotlin.openfitness.domain.badge.BadgeEvaluator] decides badges from. */
data class BadgeStats(
    val completedWorkouts: Int = 0,
    val distinctWorkoutStyles: Int = 0,
    val programsCreated: Int = 0,
    val programsTested: Int = 0,
    val completedProgramSessions: Int = 0,
    val programsGoalReached: Int = 0,
    val weightEntries: Int = 0,
    /** Longest run of consecutive days with a completed workout or program session. */
    val longestDayStreak: Int = 0,
    /** Longest run of consecutive weeks (Mon-Sun) with a completed workout or program session. */
    val longestWeekStreak: Int = 0,
)
