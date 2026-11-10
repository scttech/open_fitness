package com.scttech.android.kotlin.openfitness.ui.common.animation

import com.scttech.android.kotlin.openfitness.domain.model.Exercise

/**
 * Finds the animation clip for a movement named in a workout or program. Those carry an `exerciseId`
 * only when the exercise was picked from the library; starter templates carry just the name, so the
 * name is the fallback (the same key the library is seeded by).
 */
class ExerciseClips(exercises: List<Exercise>) {

    private val clipById: Map<Long, String?> = exercises.associate { it.id to it.animationName }
    private val clipByName: Map<String, String> = buildMap {
        exercises.forEach { e -> e.animationName?.let { putIfAbsent(e.name.trim().lowercase(), it) } }
    }

    /** The clip for a linked exercise, which wins even when it has none; otherwise the clip whose exercise shares [name]. */
    fun resolve(exerciseId: Long?, name: String): String? =
        if (exerciseId != null && exerciseId in clipById) clipById[exerciseId] else clipByName[name.trim().lowercase()]

    companion object {
        val Empty = ExerciseClips(emptyList())
    }
}
