package com.scttech.android.kotlin.openfitness.domain.model

import kotlin.math.roundToLong

/**
 * A profile's preferred unit for displaying and entering weights. Weights are always *stored* in
 * kilograms (`weightKg` fields) - this only converts at the display/input edge, so switching units
 * never rewrites data.
 */
enum class WeightUnit(val label: String, val displayName: String, private val unitsPerKg: Double) {
    KG("kg", "Kilograms", 1.0),
    LBS("lb", "Pounds", 2.20462262185),
    ;

    fun fromKg(kg: Double): Double = kg * unitsPerKg

    fun toKg(value: Double): Double = value / unitsPerKg

    /** [kg] in this unit, to one decimal place with a trailing ".0" dropped - e.g. "84.2", "40". */
    fun formatValue(kg: Double): String {
        val rounded = (fromKg(kg) * 10).roundToLong() / 10.0
        return if (rounded == rounded.toLong().toDouble()) rounded.toLong().toString() else rounded.toString()
    }

    /** [kg] in this unit with its label - e.g. "84.2 kg", "185.6 lb". */
    fun format(kg: Double): String = "${formatValue(kg)} $label"

    companion object {
        fun fromName(name: String?): WeightUnit = entries.firstOrNull { it.name == name } ?: KG
    }
}
