package com.scttech.android.kotlin.openfitness.ui.common.animation

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.MotionEvent
import android.view.TextureView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import kotlin.math.abs

private const val TAG = "ExerciseAnimation"
internal const val EXERCISE_ANIMATION_ASSET = "exercises/exercise_animations.glb"

/**
 * A looping 3D demonstration of one exercise. The caller decides the size (the figure is framed to
 * fit any shape from wide to square, so it only needs a bounded width and height). Drag sideways to look around the figure, tap to pause.
 * Switching [animationName] reuses the loaded model, so a timer can change clips without a reload.
 * Draws nothing if the clip is missing from the bundled file.
 */
@SuppressLint("ClickableViewAccessibility")
@Composable
fun ExerciseAnimationView(
    animationName: String,
    modifier: Modifier,
    contentDescription: String = "Animated demonstration of the exercise",
) {
    val context = LocalContext.current
    val model by produceState<ByteBuffer?>(initialValue = null) { value = loadModel(context) }
    var player by remember { mutableStateOf<ExerciseAnimationPlayer?>(null) }
    var failed by remember { mutableStateOf(false) }

    DisposableEffect(model) {
        val buffer = model
        val created = if (buffer == null) {
            null
        } else {
            runCatching { ExerciseAnimationPlayer(buffer.duplicate()) }
                .onFailure {
                    Log.e(TAG, "Could not start the exercise animation player", it)
                    failed = true
                }
                .getOrNull()
        }
        player = created
        onDispose {
            created?.destroy()
            player = null
        }
    }

    val current = player
    val skinToneViewModel: SkinToneViewModel = hiltViewModel()
    val skinTone by skinToneViewModel.skinTone.collectAsStateWithLifecycle()
    LaunchedEffect(current, skinTone) { current?.setSkinTone(skinTone) }
    var clipFound by remember { mutableStateOf(true) }
    var rendered by remember(current) { mutableStateOf(false) }
    DisposableEffect(current) {
        current?.onFirstFrame = { rendered = true }
        onDispose { current?.onFirstFrame = null }
    }
    LaunchedEffect(current, animationName) {
        if (current != null) clipFound = current.play(animationName)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(current, lifecycleOwner) {
        if (current == null) return@DisposableEffect onDispose {}
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> current.resume()
                Lifecycle.Event.ON_STOP -> current.pause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) current.resume()
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            current.pause()
        }
    }

    if (failed || !clipFound) return

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        if (current != null) {
            AndroidView(
                modifier = Modifier.matchParentSize(),
                factory = { ctx ->
                    TextureView(ctx).also { textureView ->
                        current.attachTo(textureView)
                        textureView.setOnTouchListener(OrbitTouchListener(current))
                    }
                },
                onRelease = { current.detach() },
            )
        }
        if (!rendered) CircularProgressIndicator()
    }
}

/** Horizontal drags orbit the figure and claim the gesture from a scrolling parent; a tap with no drag pauses. */
private class OrbitTouchListener(private val player: ExerciseAnimationPlayer) : android.view.View.OnTouchListener {
    private var lastX = 0f
    private var downX = 0f
    private var downY = 0f
    private var dragged = false

    override fun onTouch(v: android.view.View, event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                downX = event.x
                downY = event.y
                dragged = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (!dragged && abs(event.x - downX) > v.touchSlop() && abs(event.x - downX) > abs(event.y - downY)) {
                    dragged = true
                    v.parent?.requestDisallowInterceptTouchEvent(true)
                }
                if (dragged) {
                    player.orbitBy(-(event.x - lastX) * DEGREES_PER_PIXEL)
                    lastX = event.x
                }
            }
            MotionEvent.ACTION_UP -> {
                if (!dragged) {
                    player.togglePaused()
                    v.performClick()
                }
            }
        }
        return true
    }

    private fun android.view.View.touchSlop() = android.view.ViewConfiguration.get(context).scaledTouchSlop

    private companion object {
        const val DEGREES_PER_PIXEL = 0.4f
    }
}

/** Reads the bundled GLB into a direct buffer, which is what Filament's parser needs. */
private suspend fun loadModel(context: Context): ByteBuffer? = withContext(Dispatchers.IO) {
    runCatching {
        val bytes = context.assets.open(EXERCISE_ANIMATION_ASSET).use { it.readBytes() }
        ByteBuffer.allocateDirect(bytes.size).apply {
            put(bytes)
            rewind()
        }
    }.getOrNull()
}
