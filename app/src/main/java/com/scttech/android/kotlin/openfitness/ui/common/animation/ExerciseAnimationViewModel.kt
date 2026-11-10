package com.scttech.android.kotlin.openfitness.ui.common.animation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.scttech.android.kotlin.openfitness.data.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Holds the library's exercise -> clip lookup for the screens that run a workout. */
@HiltViewModel
class ExerciseAnimationViewModel @Inject constructor(
    exerciseRepository: ExerciseRepository,
) : ViewModel() {

    val clips: StateFlow<ExerciseClips> = exerciseRepository.observeExercises()
        .map { ExerciseClips(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExerciseClips.Empty)
}

/**
 * Shows the animation for an exercise in a workout or program, or nothing when the library has no clip
 * for it. [exerciseId] is the library link when there is one; [exerciseName] covers template workouts that have none.
 */
@Composable
fun ExerciseAnimationFor(
    exerciseId: Long?,
    exerciseName: String,
    modifier: Modifier,
    viewModel: ExerciseAnimationViewModel = hiltViewModel(),
) {
    val clips by viewModel.clips.collectAsStateWithLifecycle()
    val clip = clips.resolve(exerciseId, exerciseName) ?: return
    ExerciseAnimationView(
        animationName = clip,
        contentDescription = "Animated demonstration of $exerciseName",
        modifier = modifier,
    )
}
