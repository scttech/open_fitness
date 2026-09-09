package com.scttech.android.kotlin.openfitness.domain.program

import com.scttech.android.kotlin.openfitness.domain.model.ProgramConfig
import com.scttech.android.kotlin.openfitness.domain.model.ProgramPrescription
import com.scttech.android.kotlin.openfitness.domain.model.ProgramSet
import kotlin.math.roundToInt

/**
 * Turns a max-effort test result into the next training block's prescription: a training max
 * (the test result discounted by [ProgramConfig.trainingMaxFactor] as a safety buffer), then one
 * set per entry in [ProgramConfig.percentageLadder] at that percentage of the training max.
 */
object ProgramProgression {

    fun recalculate(config: ProgramConfig, testResult: Double): ProgramPrescription {
        val trainingMax = testResult * config.trainingMaxFactor
        val sets = config.percentageLadder.mapIndexed { index, percentage ->
            ProgramSet(setIndex = index, targetValue = (trainingMax * percentage).roundToInt().coerceAtLeast(1))
        }
        return ProgramPrescription(
            basedOnTestResult = testResult,
            trainingMax = trainingMax,
            sets = sets,
        )
    }
}
