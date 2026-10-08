package com.scttech.android.kotlin.openfitness.ui.workout.builder

import com.scttech.android.kotlin.openfitness.domain.model.WorkoutStyleConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class StyleFormFieldsTest {

    @Test
    fun `tabata fields convert to config using entered values`() {
        val fields = StyleFormFields.TabataFields(
            workSeconds = "30",
            restSeconds = "15",
            roundsPerCycle = "6",
            cycles = "2",
            restBetweenCyclesSeconds = "90",
        )
        val config = fields.toStyleConfig() as WorkoutStyleConfig.Tabata
        assertEquals(30, config.workSeconds)
        assertEquals(15, config.restSeconds)
        assertEquals(6, config.roundsPerCycle)
        assertEquals(2, config.cycles)
        assertEquals(90, config.restBetweenCyclesSeconds)
    }

    @Test
    fun `blank numeric fields fall back to sane defaults instead of crashing`() {
        val fields = StyleFormFields.TabataFields(
            workSeconds = "",
            restSeconds = "not a number",
            roundsPerCycle = "8",
            cycles = "1",
            restBetweenCyclesSeconds = "60",
        )
        val config = fields.toStyleConfig() as WorkoutStyleConfig.Tabata
        assertEquals(20, config.workSeconds)
        assertEquals(10, config.restSeconds)
    }

    @Test
    fun `step loading fields produce first exercise with target weight and reps`() {
        val fields = StyleFormFields.StepLoadingFields(
            exerciseName = "Back Squat",
            startWeightKg = "40",
            stepWeightKg = "10",
            setCount = "5",
            repsPerSet = "5",
            deloadEverySessions = "",
            restSeconds = "60",
        )
        val exercises = fields.toExercises(emptyList())
        assertEquals(1, exercises.size)
        assertEquals("Back Squat", exercises.first().name)
        assertEquals(5, exercises.first().targetReps)
        assertEquals(40.0, exercises.first().targetWeightKg)
    }

    @Test
    fun `emom fields convert to config using entered values`() {
        val fields = StyleFormFields.EmomFields(
            repGoal = "15",
            rounds = "5",
            restBetweenRoundsSeconds = "45",
        )
        val config = fields.toStyleConfig() as WorkoutStyleConfig.Emom
        assertEquals(15, config.repGoal)
        assertEquals(5, config.rounds)
        assertEquals(45, config.restBetweenRoundsSeconds)
    }

    @Test
    fun `emom fields produce a named exercise per circuit entry`() {
        val fields = StyleFormFields.EmomFields(repGoal = "10", rounds = "3", restBetweenRoundsSeconds = "60")
        val exercises = fields.toExercises(listOf(CircuitExerciseField("Kettlebell Swing"), CircuitExerciseField("Push-Up")))
        assertEquals(2, exercises.size)
        assertEquals("Kettlebell Swing", exercises[0].name)
        assertEquals("Push-Up", exercises[1].name)
    }

    @Test
    fun `follow along fields convert to config using entered values`() {
        val fields = StyleFormFields.FollowAlongFields(defaultRestSeconds = "20")
        val config = fields.toStyleConfig() as WorkoutStyleConfig.FollowAlong
        assertEquals(20, config.defaultRestSeconds)
    }

    @Test
    fun `follow along steps convert to rest and exercise WorkoutExercise entries, falling back to sane defaults on blank seconds`() {
        val steps = listOf(
            FollowAlongStepField(isRest = false, name = "Push-Up", exerciseId = 1L, seconds = "40"),
            FollowAlongStepField(isRest = true, name = "Rest", seconds = ""),
            FollowAlongStepField(isRest = false, name = "", seconds = "30"),
            FollowAlongStepField(isRest = false, name = "Air Squat", seconds = "not a number"),
        )
        val exercises = steps.toWorkoutExercises()

        assertEquals(3, exercises.size)
        assertEquals("Push-Up", exercises[0].name)
        assertEquals(1L, exercises[0].exerciseId)
        assertEquals(40, exercises[0].targetDurationSeconds)
        assertEquals(false, exercises[0].isRest)

        assertEquals("Rest", exercises[1].name)
        assertEquals(true, exercises[1].isRest)
        assertEquals(15, exercises[1].targetDurationSeconds)

        assertEquals("Air Squat", exercises[2].name)
        assertEquals(false, exercises[2].isRest)
        assertEquals(30, exercises[2].targetDurationSeconds)
    }
}
