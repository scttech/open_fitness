package com.scttech.android.kotlin.openfitness.domain.program

import com.scttech.android.kotlin.openfitness.domain.model.ProgramConfig
import com.scttech.android.kotlin.openfitness.domain.model.RepStrategyConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgramProgressionTest {

    @Test
    fun `percentage ladder produces one set per percentage`() {
        val config = ProgramConfig(
            repStrategyConfig = RepStrategyConfig.PercentageLadder(percentages = listOf(0.5, 0.7, 0.9)),
            trainingMaxFactor = 1.0,
        )
        val prescription = ProgramProgression.recalculate(config, testResult = 100)

        assertEquals(100, prescription.trainingMax)
        assertEquals(listOf(50, 70, 90), prescription.sets.map { it.targetValue })
        assertTrue(prescription.sets.none { it.isAmrap })
    }

    @Test
    fun `straight sets repeats the same target across the configured set count`() {
        val config = ProgramConfig(
            repStrategyConfig = RepStrategyConfig.StraightSets(setCount = 4, percentage = 0.7),
            trainingMaxFactor = 1.0,
        )
        val prescription = ProgramProgression.recalculate(config, testResult = 100)

        assertEquals(listOf(70, 70, 70, 70), prescription.sets.map { it.targetValue })
    }

    @Test
    fun `amrap finisher appends a flagged set after the fixed sets`() {
        val config = ProgramConfig(
            repStrategyConfig = RepStrategyConfig.AmrapFinisher(fixedSetCount = 2, fixedPercentage = 0.8),
            trainingMaxFactor = 1.0,
        )
        val prescription = ProgramProgression.recalculate(config, testResult = 100)

        assertEquals(3, prescription.sets.size)
        assertEquals(listOf(false, false, true), prescription.sets.map { it.isAmrap })
        assertEquals(80, prescription.sets.last().targetValue)
    }

    @Test
    fun `custom manual ignores the test result and uses the configured targets`() {
        val config = ProgramConfig(
            repStrategyConfig = RepStrategyConfig.CustomManual(setTargets = listOf(12, 10, 8)),
            trainingMaxFactor = 1.0,
        )
        val prescription = ProgramProgression.recalculate(config, testResult = 999)

        assertEquals(listOf(12, 10, 8), prescription.sets.map { it.targetValue })
    }
}
