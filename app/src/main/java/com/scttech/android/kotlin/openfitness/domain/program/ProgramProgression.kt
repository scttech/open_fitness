package com.scttech.android.kotlin.openfitness.domain.program

import com.scttech.android.kotlin.openfitness.domain.model.ProgramConfig
import com.scttech.android.kotlin.openfitness.domain.model.ProgramPrescription
import com.scttech.android.kotlin.openfitness.domain.model.ProgramSet
import com.scttech.android.kotlin.openfitness.domain.model.RepStrategyConfig
import kotlin.math.roundToInt

/**
 * Turns a max-effort test result into the next training block's prescription: a training max
 * (the test result discounted by [ProgramConfig.trainingMaxFactor] as a safety buffer), then a
 * list of sets shaped by [ProgramConfig.repStrategyConfig].
 */
object ProgramProgression {

    fun recalculate(config: ProgramConfig, testResult: Int): ProgramPrescription {
        val trainingMax = (testResult * config.trainingMaxFactor).roundToInt()
        val sets = when (val strategyConfig = config.repStrategyConfig) {
            is RepStrategyConfig.PercentageLadder -> strategyConfig.percentages.mapIndexed { index, percentage ->
                ProgramSet(setIndex = index, targetValue = valueAt(trainingMax, percentage))
            }

            is RepStrategyConfig.StraightSets -> List(strategyConfig.setCount) { index ->
                ProgramSet(setIndex = index, targetValue = valueAt(trainingMax, strategyConfig.percentage))
            }

            is RepStrategyConfig.AmrapFinisher -> {
                val fixedSets = List(strategyConfig.fixedSetCount) { index ->
                    ProgramSet(setIndex = index, targetValue = valueAt(trainingMax, strategyConfig.fixedPercentage))
                }
                fixedSets + ProgramSet(
                    setIndex = strategyConfig.fixedSetCount,
                    targetValue = valueAt(trainingMax, strategyConfig.fixedPercentage),
                    isAmrap = true,
                )
            }

            is RepStrategyConfig.CustomManual -> strategyConfig.setTargets.mapIndexed { index, target ->
                ProgramSet(setIndex = index, targetValue = target.coerceAtLeast(1))
            }
        }
        return ProgramPrescription(
            basedOnTestResult = testResult,
            trainingMax = trainingMax,
            sets = sets,
        )
    }

    private fun valueAt(trainingMax: Int, percentage: Double): Int =
        (trainingMax * percentage).roundToInt().coerceAtLeast(1)
}
