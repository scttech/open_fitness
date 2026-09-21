package com.scttech.android.kotlin.openfitness.domain.model

import kotlinx.datetime.Instant

enum class ExerciseCategory(val displayName: String) {
    CHEST("Chest"),
    BACK("Back"),
    SHOULDERS("Shoulders"),
    ARMS("Arms"),
    LEGS("Legs"),
    CORE("Core"),
    FULL_BODY("Full Body"),
    CARDIO("Cardio"),
    MOBILITY("Mobility"),
}

enum class ExerciseEquipment(val displayName: String) {
    BODYWEIGHT("Bodyweight"),
    BARBELL("Barbell"),
    DUMBBELL("Dumbbell"),
    KETTLEBELL("Kettlebell"),
    MACE("Steel Mace"),
    MACHINE("Machine"),
    BAND("Band"),
    CARDIO_MACHINE("Cardio Machine"),
    OTHER("Other"),
}

/**
 * A reference-library movement. Global (no [Profile]) and shared across profiles - workouts and
 * programs link to one by [id] but always keep a denormalized copy of the name, so nothing breaks
 * if the linked exercise is later renamed or deleted.
 */
data class Exercise(
    val id: Long = 0L,
    val name: String,
    val category: ExerciseCategory,
    val equipment: ExerciseEquipment,
    val formNotes: String = "",
    val isCustom: Boolean = false,
    val createdAt: Instant,
)
