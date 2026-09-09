package com.scttech.android.kotlin.openfitness.domain.model

/** The supported training protocols. Each has a distinct [WorkoutStyleConfig] shape. */
enum class WorkoutStyle(val displayName: String, val shortDescription: String) {
    TABATA(
        displayName = "Tabata",
        shortDescription = "Short, high-intensity work/rest intervals repeated for several rounds.",
    ),
    GREASE_THE_GROOVE(
        displayName = "Grease the Groove",
        shortDescription = "Frequent, submaximal sets of one movement spread across the day.",
    ),
    PYRAMID(
        displayName = "Pyramid Protocol",
        shortDescription = "Reps climb up, come back down, or both, set after set.",
    ),
    DENSITY(
        displayName = "Density Training",
        shortDescription = "Pack as much quality volume as possible into a fixed time window.",
    ),
    STEP_LOADING(
        displayName = "Step-Loading",
        shortDescription = "Load increases in steps across sets or successive sessions.",
    ),
}
