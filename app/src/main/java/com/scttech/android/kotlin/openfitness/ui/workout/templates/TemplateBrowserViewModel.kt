package com.scttech.android.kotlin.openfitness.ui.workout.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scttech.android.kotlin.openfitness.data.repository.ProfileRepository
import com.scttech.android.kotlin.openfitness.data.repository.WorkoutRepository
import com.scttech.android.kotlin.openfitness.domain.model.TemplateCategory
import com.scttech.android.kotlin.openfitness.domain.model.Workout
import com.scttech.android.kotlin.openfitness.domain.template.StarterTemplates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface TemplateBrowserUiState {
    data object Loading : TemplateBrowserUiState
    data class Success(
        val query: String,
        /** `null` means "All". */
        val selectedCategory: TemplateCategory?,
        /** Categories that have at least one template, in display order. */
        val availableCategories: List<TemplateCategory>,
        /** Templates matching the filters, grouped by category in display order. */
        val groups: List<TemplateGroup>,
    ) : TemplateBrowserUiState {
        val isEmpty: Boolean get() = groups.isEmpty()
    }
}

data class TemplateGroup(val category: TemplateCategory, val templates: List<Workout>)

private fun matchesQuery(template: Workout, query: String): Boolean =
    query.isBlank() ||
        template.name.contains(query, ignoreCase = true) ||
        template.notes.contains(query, ignoreCase = true) ||
        template.style.displayName.contains(query, ignoreCase = true) ||
        template.exercises.any { it.name.contains(query, ignoreCase = true) }

@HiltViewModel
class TemplateBrowserViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow<TemplateCategory?>(null)

    val uiState: StateFlow<TemplateBrowserUiState> =
        combine(workoutRepository.observeTemplates(), query, selectedCategory) { templates, query, selected ->
            val byCategory = templates.groupBy { StarterTemplates.categoryOf(it.name) }
            val groups = TemplateCategory.entries
                .filter { selected == null || it == selected }
                .mapNotNull { category ->
                    val matching = byCategory[category].orEmpty().filter { matchesQuery(it, query) }
                    matching.takeIf { it.isNotEmpty() }?.let { TemplateGroup(category, it) }
                }
            TemplateBrowserUiState.Success(
                query = query,
                selectedCategory = selected,
                availableCategories = TemplateCategory.entries.filter { it in byCategory },
                groups = groups,
            ) as TemplateBrowserUiState
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TemplateBrowserUiState.Loading,
        )

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onCategorySelected(category: TemplateCategory?) {
        selectedCategory.value = category
    }

    private val _workoutAdded = MutableSharedFlow<Long>()
    val workoutAdded: SharedFlow<Long> = _workoutAdded

    fun useTemplate(template: Workout) {
        viewModelScope.launch {
            val profileId = profileRepository.currentProfileId.filterNotNull().first()
            val newId = workoutRepository.copyTemplateToProfile(template, profileId)
            _workoutAdded.emit(newId)
        }
    }
}
