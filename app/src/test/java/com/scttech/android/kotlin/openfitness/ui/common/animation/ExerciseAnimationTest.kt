package com.scttech.android.kotlin.openfitness.ui.common.animation

import com.scttech.android.kotlin.openfitness.domain.template.StarterExercises
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ExerciseAnimationTest {

    /** The JSON chunk of the bundled GLB: 12-byte header, then a chunk of [length, type, data]. */
    private fun glbJson(): JsonObject {
        val bytes = File("src/main/assets/$EXERCISE_ANIMATION_ASSET").readBytes()
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val length = buffer.getInt(12)
        return Json.parseToJsonElement(String(bytes, 20, length, Charsets.UTF_8)).jsonObject
    }

    private fun clipNames(): Set<String> =
        glbJson().getValue("animations").jsonArray.map { it.jsonObject.getValue("name").jsonPrimitive.content }.toSet()

    @Test
    fun everyMappedClipExistsInTheBundledFile() {
        val clips = clipNames()
        val missing = StarterExercises.ANIMATIONS.filterValues { it !in clips }
        assertTrue("Exercises mapped to clips the GLB does not contain: $missing", missing.isEmpty())
    }

    @Test
    fun everyMappedExerciseIsInTheStarterLibrary() {
        val names = StarterExercises.all().map { it.name }.toSet()
        val unknown = StarterExercises.ANIMATIONS.keys - names
        assertTrue("Animations mapped to exercises that do not exist: $unknown", unknown.isEmpty())
    }

    @Test
    fun starterExercisesCarryTheirClip() {
        val byName = StarterExercises.all().filter { it.name == "Goblet Squat" }
        assertEquals(2, byName.size)
        assertTrue(byName.all { it.animationName == "GobletSquat" })
        assertEquals("Pushup", StarterExercises.all().first { it.name == "Push-Up" }.animationName)
    }

    @Test
    fun propsFollowTheEquipmentInTheClipName() {
        assertEquals(setOf(AnimationProp.KETTLEBELL), propsFor("Kbswing"))
        assertEquals(setOf(AnimationProp.KETTLEBELL), propsFor("GobletSquat"))
        assertEquals(setOf(AnimationProp.KETTLEBELL), propsFor("KettlebellHalo"))
        assertEquals(setOf(AnimationProp.MACE), propsFor("Mace10to2"))
        assertEquals(setOf(AnimationProp.PULLUP_BAR), propsFor("Chinup"))
        assertEquals(setOf(AnimationProp.PULLUP_BAR), propsFor("HangingLegRaise"))
        assertEquals(emptySet<AnimationProp>(), propsFor("Pushup"))
    }

    @Test
    fun theGlbHasAMeshForEveryProp() {
        val meshes = glbJson().getValue("meshes").jsonArray.map { it.jsonObject.getValue("name").jsonPrimitive.content }
        AnimationProp.entries.forEach { assertTrue("${it.meshName} missing from GLB", it.meshName in meshes) }
    }

    @Test
    fun loopSkipsTheLeadInAndNeverReachesTheSeamFrame() {
        val duration = 2f
        assertEquals(CLIP_LEAD_IN_SECONDS, loopTime(0f, duration), 1e-6f)
        val period = duration - CLIP_LEAD_IN_SECONDS
        assertEquals(CLIP_LEAD_IN_SECONDS, loopTime(period, duration), 1e-4f)
        assertTrue(loopTime(period - 0.001f, duration) < duration)
        assertEquals(CLIP_LEAD_IN_SECONDS + 0.5f, loopTime(3 * period + 0.5f, duration), 1e-3f)
    }

    @Test
    fun aDegenerateClipDoesNotDivideByZero() {
        assertEquals(0f, loopTime(5f, CLIP_LEAD_IN_SECONDS), 0f)
    }
}
