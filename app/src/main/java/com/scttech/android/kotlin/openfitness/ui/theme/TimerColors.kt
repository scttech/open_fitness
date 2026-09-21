package com.scttech.android.kotlin.openfitness.ui.theme

import androidx.compose.ui.graphics.Color

/** A curated set of distinguishable colors the user can assign to timer work/rest phases. */
val TimerPaletteColors = listOf(
    Color(0xFFB3541E), // orange
    Color(0xFFC62828), // red
    Color(0xFF9A3E5B), // rose
    Color(0xFF7A5AC2), // purple
    Color(0xFF1565C0), // blue
    Color(0xFF3A6373), // teal
    Color(0xFF1E6B4E), // green
    Color(0xFF4C6358), // sage
)

val DefaultTimerWorkColor = TimerPaletteColors[0]
val DefaultTimerRestColor = TimerPaletteColors[4]
