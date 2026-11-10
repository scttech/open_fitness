package com.scttech.android.kotlin.openfitness.ui.common.animation

import com.scttech.android.kotlin.openfitness.domain.model.Exercise
import com.scttech.android.kotlin.openfitness.domain.model.ExerciseCategory
import com.scttech.android.kotlin.openfitness.domain.model.ExerciseEquipment
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExerciseClipsTest {

    private fun exercise(id: Long, name: String, clip: String?, custom: Boolean = false) = Exercise(
        id = id,
        name = name,
        category = ExerciseCategory.LEGS,
        equipment = ExerciseEquipment.BODYWEIGHT,
        isCustom = custom,
        animationName = clip,
        createdAt = Instant.fromEpochMilliseconds(0),
    )

    private val clips = ExerciseClips(
        listOf(
            exercise(1, "Air Squat", "Squat"),
            exercise(2, "Back Squat", null),
            exercise(3, "My Squat", null, custom = true),
        ),
    )

    @Test
    fun linkedExerciseUsesItsOwnClip() {
        assertEquals("Squat", clips.resolve(1, "Air Squat"))
    }

    @Test
    fun linkedExerciseWithoutAClipDoesNotBorrowOneByName() {
        // The id is authoritative: this workout points at Back Squat, which has no animation.
        assertNull(clips.resolve(2, "Air Squat"))
    }

    @Test
    fun unlinkedExerciseMatchesByNameIgnoringCaseAndSpaces() {
        assertEquals("Squat", clips.resolve(null, "  air squat "))
    }

    @Test
    fun deletedLinkFallsBackToName() {
        assertEquals("Squat", clips.resolve(999, "Air Squat"))
    }

    @Test
    fun unknownExerciseHasNoClip() {
        assertNull(clips.resolve(null, "Farmer's Carry"))
        assertNull(ExerciseClips.Empty.resolve(null, "Air Squat"))
    }
}
