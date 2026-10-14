package com.scttech.android.kotlin.openfitness.domain.template

import com.scttech.android.kotlin.openfitness.domain.model.TemplateCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StarterTemplatesTest {

    private val templates = StarterTemplates.all()

    @Test
    fun templateNamesAreUnique() {
        val duplicates = templates.groupingBy { it.name }.eachCount().filterValues { it > 1 }.keys
        assertTrue("Duplicate template names: $duplicates", duplicates.isEmpty())
    }

    @Test
    fun everyExerciseIsInTheStarterLibrary() {
        val library = StarterExercises.all().map { it.name.lowercase() }.toSet()
        val missing = templates.flatMap { template ->
            template.exercises.filter { !it.isRest && it.name.lowercase() !in library }.map { "${template.name}: ${it.name}" }
        }
        assertTrue("Exercises missing from library: $missing", missing.isEmpty())
    }

    @Test
    fun styleMatchesConfig() {
        templates.forEach { assertEquals(it.name, it.style, it.styleConfig.style) }
    }

    @Test
    fun everyTemplateHasExercises() {
        templates.forEach { assertTrue("${it.name} has no exercises", it.exercises.isNotEmpty()) }
    }

    @Test
    fun everyCategoryHasTemplates() {
        val used = templates.map { StarterTemplates.categoryOf(it.name) }.toSet()
        assertEquals(TemplateCategory.entries.toSet(), used)
    }
}
