package com.scttech.android.kotlin.openfitness.domain.template

import com.scttech.android.kotlin.openfitness.domain.model.Workout
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutExercise
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutStyle
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutStyleConfig
import kotlinx.datetime.Instant

/** A handful of ready-to-use example workouts, one or two per style, seeded on first launch. */
object StarterTemplates {

    fun all(): List<Workout> = listOf(
        Workout(
            profileId = null,
            name = "Classic Tabata Squats",
            style = WorkoutStyle.TABATA,
            styleConfig = WorkoutStyleConfig.Tabata(
                workSeconds = 20,
                restSeconds = 10,
                roundsPerCycle = 8,
                cycles = 1,
                restBetweenCyclesSeconds = 10,
            ),
            exercises = listOf(WorkoutExercise(order = 0, name = "Bodyweight Squat")),
            notes = "The original Tabata protocol: 20s all-out, 10s rest, 8 rounds.",
            isTemplate = true,
            createdAt = TEMPLATE_EPOCH,
        ),
        Workout(
            profileId = null,
            name = "Tabata Full-Body Circuit",
            style = WorkoutStyle.TABATA,
            styleConfig = WorkoutStyleConfig.Tabata(
                workSeconds = 20,
                restSeconds = 10,
                roundsPerCycle = 4,
                cycles = 1,
                restBetweenCyclesSeconds = 60,
            ),
            exercises = listOf(
                WorkoutExercise(order = 0, name = "Push-Up"),
                WorkoutExercise(order = 1, name = "Squat"),
                WorkoutExercise(order = 2, name = "Mountain Climber"),
                WorkoutExercise(order = 3, name = "Plank"),
            ),
            notes = "4 exercises per round, 20s work / 10s rest between exercises, 60s rest between rounds, 4 rounds.",
            isTemplate = true,
            createdAt = TEMPLATE_EPOCH,
        ),
        Workout(
            profileId = null,
            name = "Daily Pull-Up Grease",
            style = WorkoutStyle.GREASE_THE_GROOVE,
            styleConfig = WorkoutStyleConfig.GreaseTheGroove(
                exerciseName = "Pull-Up",
                repsPerSet = 5,
                targetSetsPerDay = 10,
                minRestMinutesBetweenSets = 30,
            ),
            exercises = listOf(WorkoutExercise(order = 0, name = "Pull-Up", targetReps = 5)),
            notes = "5 easy reps, ~10 times a day, well short of failure.",
            isTemplate = true,
            createdAt = TEMPLATE_EPOCH,
        ),
        Workout(
            profileId = null,
            name = "Push-Up Endurance Grease",
            style = WorkoutStyle.GREASE_THE_GROOVE,
            styleConfig = WorkoutStyleConfig.GreaseTheGroove(
                exerciseName = "Push-Up",
                repsPerSet = 10,
                targetSetsPerDay = 8,
                minRestMinutesBetweenSets = 20,
            ),
            exercises = listOf(WorkoutExercise(order = 0, name = "Push-Up", targetReps = 10)),
            notes = "10 submaximal reps spread across the day.",
            isTemplate = true,
            createdAt = TEMPLATE_EPOCH,
        ),
        Workout(
            profileId = null,
            name = "Kettlebell Swing Pyramid",
            style = WorkoutStyle.PYRAMID,
            styleConfig = WorkoutStyleConfig.Pyramid(
                direction = WorkoutStyleConfig.PyramidDirection.UP_DOWN,
                startReps = 2,
                stepReps = 2,
                peakReps = 10,
            ),
            exercises = listOf(WorkoutExercise(order = 0, name = "Kettlebell Swing")),
            notes = "2-4-6-8-10-8-6-4-2 reps, resting as needed between sets.",
            isTemplate = true,
            createdAt = TEMPLATE_EPOCH,
        ),
        Workout(
            profileId = null,
            name = "Burpee Ascending Pyramid",
            style = WorkoutStyle.PYRAMID,
            styleConfig = WorkoutStyleConfig.Pyramid(
                direction = WorkoutStyleConfig.PyramidDirection.UP,
                startReps = 1,
                stepReps = 1,
                peakReps = 10,
            ),
            exercises = listOf(WorkoutExercise(order = 0, name = "Burpee")),
            notes = "Climb 1 rep at a time up to 10.",
            isTemplate = true,
            createdAt = TEMPLATE_EPOCH,
        ),
        Workout(
            profileId = null,
            name = "15-Minute Density Circuit",
            style = WorkoutStyle.DENSITY,
            styleConfig = WorkoutStyleConfig.Density(durationMinutes = 15, targetRounds = null),
            exercises = listOf(
                WorkoutExercise(order = 0, name = "Air Squat", targetReps = 10),
                WorkoutExercise(order = 1, name = "Push-Up", targetReps = 10),
                WorkoutExercise(order = 2, name = "Sit-Up", targetReps = 10),
            ),
            notes = "As many quality rounds as possible in 15 minutes.",
            isTemplate = true,
            createdAt = TEMPLATE_EPOCH,
        ),
        Workout(
            profileId = null,
            name = "20-Minute Density Row & Carry",
            style = WorkoutStyle.DENSITY,
            styleConfig = WorkoutStyleConfig.Density(durationMinutes = 20, targetRounds = 8),
            exercises = listOf(
                WorkoutExercise(order = 0, name = "Row (calories)", targetReps = 10),
                WorkoutExercise(order = 1, name = "Farmer's Carry", targetDistanceMeters = 40.0),
            ),
            notes = "Target 8 rounds in 20 minutes.",
            isTemplate = true,
            createdAt = TEMPLATE_EPOCH,
        ),
        Workout(
            profileId = null,
            name = "Barbell Squat Step-Loading",
            style = WorkoutStyle.STEP_LOADING,
            styleConfig = WorkoutStyleConfig.StepLoading(
                startWeightKg = 40.0,
                stepWeightKg = 10.0,
                setCount = 5,
                repsPerSet = 5,
                deloadEverySessions = 4,
            ),
            exercises = listOf(WorkoutExercise(order = 0, name = "Back Squat", targetReps = 5)),
            notes = "5 sets of 5, adding 10kg each set; deload every 4th session.",
            isTemplate = true,
            createdAt = TEMPLATE_EPOCH,
        ),
        Workout(
            profileId = null,
            name = "Deadlift Step-Loading",
            style = WorkoutStyle.STEP_LOADING,
            styleConfig = WorkoutStyleConfig.StepLoading(
                startWeightKg = 60.0,
                stepWeightKg = 15.0,
                setCount = 4,
                repsPerSet = 3,
                deloadEverySessions = null,
            ),
            exercises = listOf(WorkoutExercise(order = 0, name = "Deadlift", targetReps = 3)),
            notes = "4 sets of 3, adding 15kg each set.",
            isTemplate = true,
            createdAt = TEMPLATE_EPOCH,
        ),
        Workout(
            profileId = null,
            name = "Full-Body EMOM",
            style = WorkoutStyle.EMOM,
            styleConfig = WorkoutStyleConfig.Emom(repGoal = 12, rounds = 4, restBetweenRoundsSeconds = 30),
            exercises = listOf(
                WorkoutExercise(order = 0, name = "Kettlebell Swing"),
                WorkoutExercise(order = 1, name = "Push-Up"),
                WorkoutExercise(order = 2, name = "Air Squat"),
            ),
            notes = "Every minute on the minute: hit 12 reps of each exercise, rest what's left, 4 rounds through.",
            isTemplate = true,
            createdAt = TEMPLATE_EPOCH,
        ),
    )

    private val TEMPLATE_EPOCH = Instant.fromEpochMilliseconds(0L)
}
