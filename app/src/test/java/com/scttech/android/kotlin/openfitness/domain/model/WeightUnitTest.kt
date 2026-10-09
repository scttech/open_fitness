package com.scttech.android.kotlin.openfitness.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class WeightUnitTest {

    @Test
    fun `kilograms are shown as stored`() {
        assertEquals("84.2 kg", WeightUnit.KG.format(84.2))
        assertEquals("40 kg", WeightUnit.KG.format(40.0))
    }

    @Test
    fun `pounds convert from kilograms and round to one decimal`() {
        assertEquals("185.6 lb", WeightUnit.LBS.format(84.2))
        assertEquals("88.2 lb", WeightUnit.LBS.format(40.0))
        assertEquals("135 lb", WeightUnit.LBS.format(61.23495))
    }

    @Test
    fun `toKg inverts fromKg`() {
        assertEquals(185.0, WeightUnit.LBS.fromKg(WeightUnit.LBS.toKg(185.0)), 1e-9)
        assertEquals(61.235, WeightUnit.LBS.toKg(135.0), 0.001)
    }

    @Test
    fun `unknown stored names fall back to kilograms`() {
        assertEquals(WeightUnit.LBS, WeightUnit.fromName("LBS"))
        assertEquals(WeightUnit.KG, WeightUnit.fromName("STONE"))
        assertEquals(WeightUnit.KG, WeightUnit.fromName(null))
    }
}
