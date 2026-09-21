package com.scttech.android.kotlin.openfitness.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutStyleConfigTest {

    @Test
    fun `pyramid up_down produces symmetric rep scheme`() {
        val config = WorkoutStyleConfig.Pyramid(
            direction = WorkoutStyleConfig.PyramidDirection.UP_DOWN,
            startReps = 2,
            stepReps = 2,
            peakReps = 10,
        )
        assertEquals(listOf(2, 4, 6, 8, 10, 8, 6, 4, 2), config.repScheme())
    }

    @Test
    fun `pyramid up only ascends`() {
        val config = WorkoutStyleConfig.Pyramid(
            direction = WorkoutStyleConfig.PyramidDirection.UP,
            startReps = 1,
            stepReps = 1,
            peakReps = 5,
        )
        assertEquals(listOf(1, 2, 3, 4, 5), config.repScheme())
    }

    @Test
    fun `pyramid down only descends`() {
        val config = WorkoutStyleConfig.Pyramid(
            direction = WorkoutStyleConfig.PyramidDirection.DOWN,
            startReps = 2,
            stepReps = 2,
            peakReps = 6,
        )
        assertEquals(listOf(6, 4, 2), config.repScheme())
    }

    @Test
    fun `step loading computes weight per set from step`() {
        val config = WorkoutStyleConfig.StepLoading(
            startWeightKg = 40.0,
            stepWeightKg = 10.0,
            setCount = 5,
            repsPerSet = 5,
        )
        assertEquals(40.0, config.weightForSet(0), 0.0)
        assertEquals(70.0, config.weightForSet(3), 0.0)
    }

    @Test
    fun `grease the groove daily target multiplies reps by sets`() {
        val config = WorkoutStyleConfig.GreaseTheGroove(
            exerciseName = "Pull-Up",
            repsPerSet = 5,
            targetSetsPerDay = 10,
        )
        assertEquals(50, config.dailyTargetReps)
    }

    @Test
    fun `emom default config uses sane defaults`() {
        val config = WorkoutStyleConfig.Emom()
        assertEquals(10, config.repGoal)
        assertEquals(3, config.rounds)
        assertEquals(60, config.restBetweenRoundsSeconds)
    }
}
