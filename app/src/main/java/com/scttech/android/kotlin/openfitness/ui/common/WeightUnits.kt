package com.scttech.android.kotlin.openfitness.ui.common

import androidx.compose.runtime.compositionLocalOf
import com.scttech.android.kotlin.openfitness.domain.model.WeightUnit

/** The active profile's preferred [WeightUnit], provided once at the app root. */
val LocalWeightUnit = compositionLocalOf { WeightUnit.KG }
