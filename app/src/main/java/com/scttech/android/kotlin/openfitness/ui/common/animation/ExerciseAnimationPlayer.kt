package com.scttech.android.kotlin.openfitness.ui.common.animation

import android.os.SystemClock
import android.util.Log
import android.view.Choreographer
import android.view.Surface
import android.view.TextureView
import com.google.android.filament.Camera
import com.google.android.filament.Engine
import com.google.android.filament.EntityManager
import com.google.android.filament.Filament
import com.google.android.filament.IndirectLight
import com.google.android.filament.LightManager
import com.google.android.filament.Renderer
import com.google.android.filament.Scene
import com.google.android.filament.SwapChain
import com.google.android.filament.View
import com.google.android.filament.Viewport
import com.google.android.filament.android.UiHelper
import com.google.android.filament.gltfio.AssetLoader
import com.google.android.filament.gltfio.FilamentAsset
import com.google.android.filament.gltfio.Gltfio
import com.google.android.filament.gltfio.ResourceLoader
import com.google.android.filament.gltfio.UbershaderProvider
import java.nio.ByteBuffer
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Renders the bundled exercise-animation GLB with Filament: one rigged figure, three props, and a
 * clip per exercise. Owns every native Filament object, so [destroy] must be called exactly once,
 * and every method must run on the main thread.
 */
internal class ExerciseAnimationPlayer(model: ByteBuffer) {

    private val engine: Engine = Engine.create()
    private val renderer: Renderer = engine.createRenderer()
    private val scene: Scene = engine.createScene()
    private val view: View = engine.createView()
    private val cameraEntity = EntityManager.get().create()
    private val camera: Camera = engine.createCamera(cameraEntity)
    private val sun = EntityManager.get().create()
    private val indirectLight: IndirectLight

    private val materialProvider = UbershaderProvider(engine)
    private val assetLoader = AssetLoader(engine, materialProvider, EntityManager.get())
    private val resourceLoader = ResourceLoader(engine)
    private val asset: FilamentAsset

    private val uiHelper = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK)
    private var swapChain: SwapChain? = null
    private var textureView: TextureView? = null
    private var viewportWidth = 0
    private var viewportHeight = 0

    private val choreographer = Choreographer.getInstance()
    private var running = false
    private var paused = false
    private var destroyed = false

    private var clipIndex = -1
    private var clipDuration = 0f
    private var clockStartNanos = 0L
    private var pausedAtSeconds = 0f
    private val propEntities: Map<AnimationProp, IntArray>

    private var azimuthDegrees = DEFAULT_AZIMUTH_DEGREES

    /** Bounding sphere of the current clip's whole motion; the camera orbits its centre and backs off to fit it. */
    private var focus = floatArrayOf(0f, 1f, 0f)
    private var focusRadius = 1.2f

    /** Called once, on the main thread, when the first frames have been rendered. */
    var onFirstFrame: (() -> Unit)? = null
    private var firstFrameReported = false
    private var framesSubmitted = 0

    init {
        val startedAt = SystemClock.elapsedRealtime()
        asset = checkNotNull(assetLoader.createAsset(model)) { "Exercise animation file could not be parsed" }
        resourceLoader.loadResources(asset)
        asset.releaseSourceData()
        scene.addEntities(asset.entities)

        propEntities = AnimationProp.entries.associateWith { prop ->
            // The prop bones share their mesh's name, so keep only the entity that actually draws.
            asset.getEntitiesByName(prop.meshName).filter { engine.renderableManager.hasComponent(it) }.toIntArray()
        }

        // A flat, shadowless rig: one sun for shape, a uniform ambient term so the underside is not black.
        LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(1f, 1f, 1f)
            .intensity(SUN_LUX)
            .direction(-0.4f, -0.7f, -0.6f)
            .castShadows(false)
            .build(engine, sun)
        scene.addEntity(sun)
        indirectLight = IndirectLight.Builder()
            .irradiance(1, floatArrayOf(1f, 1f, 1f))
            .intensity(AMBIENT_LUX)
            .build(engine)
        scene.indirectLight = indirectLight

        view.scene = scene
        view.camera = camera
        // Transparent, so whatever the screen paints behind the view (the themed card, light or dark) is the backdrop.
        view.blendMode = View.BlendMode.TRANSLUCENT
        renderer.clearOptions = renderer.clearOptions.apply {
            clear = true
            clearColor = doubleArrayOf(0.0, 0.0, 0.0, 0.0)
        }

        uiHelper.isOpaque = false
        uiHelper.renderCallback = object : UiHelper.RendererCallback {
            override fun onNativeWindowChanged(surface: Surface) {
                swapChain?.let { engine.destroySwapChain(it) }
                swapChain = engine.createSwapChain(surface, uiHelper.swapChainFlags)
            }

            override fun onDetachedFromSurface() {
                swapChain?.let {
                    engine.destroySwapChain(it)
                    engine.flushAndWait()
                }
                swapChain = null
            }

            override fun onResized(width: Int, height: Int) {
                viewportWidth = width
                viewportHeight = height
                view.viewport = Viewport(0, 0, width, height)
                updateCamera()
            }
        }
        updateCamera()
        Log.d(TAG, "Exercise animation model ready in ${SystemClock.elapsedRealtime() - startedAt} ms")
    }

    private fun animator() = asset.instance.animator

    fun attachTo(textureView: TextureView) {
        this.textureView = textureView
        uiHelper.attachTo(textureView)
    }

    fun detach() {
        uiHelper.detach()
        textureView = null
    }

    /** Switches to [animationName] and shows exactly the props it uses. Returns false if the file has no such clip. */
    fun play(animationName: String): Boolean {
        val animator = animator() ?: return false
        val index = (0 until animator.animationCount).firstOrNull { animator.getAnimationName(it) == animationName }
        if (index == null) {
            clipIndex = -1
            return false
        }
        clipIndex = index
        clipDuration = animator.getAnimationDuration(index)
        clockStartNanos = System.nanoTime()
        pausedAtSeconds = 0f
        val wanted = propsFor(animationName)
        propEntities.forEach { (prop, entities) ->
            entities.forEach { if (prop in wanted) scene.addEntity(it) else scene.remove(it) }
        }
        frameClip(animator, index, wanted)
        return true
    }

    /**
     * Frames the camera on everything the clip does: a squat stays near the standing height, a plank
     * or crunch sits near the floor, a pull-up reaches up to the bar. Samples the motion across the
     * loop and takes the extent of the skeleton's joints (the mesh is skinned, so it has no bounds of its own).
     */
    private fun frameClip(animator: com.google.android.filament.gltfio.Animator, index: Int, wanted: Set<AnimationProp>) {
        val hiddenPropNames = (AnimationProp.entries - wanted).map { it.meshName }.toSet()
        val joints = asset.instance.getJointsAt(0).filter { asset.getName(it) !in hiddenPropNames }
        val transforms = engine.transformManager
        val matrix = FloatArray(16)
        val min = floatArrayOf(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE)
        val max = floatArrayOf(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE)
        for (sample in 0 until FRAMING_SAMPLES) {
            val fraction = sample / (FRAMING_SAMPLES - 1f)
            animator.applyAnimation(index, CLIP_LEAD_IN_SECONDS + fraction * (clipDuration - 2 * CLIP_LEAD_IN_SECONDS).coerceAtLeast(0f))
            for (joint in joints) {
                transforms.getWorldTransform(transforms.getInstance(joint), matrix)
                for (axis in 0..2) {
                    min[axis] = minOf(min[axis], matrix[12 + axis])
                    max[axis] = maxOf(max[axis], matrix[12 + axis])
                }
            }
        }
        if (min[0] > max[0]) return // no joints: keep the previous framing
        focus = FloatArray(3) { (min[it] + max[it]) / 2 }
        val halfDiagonal = sqrt((0..2).sumOf { ((max[it] - min[it]) / 2.0).pow(2) }).toFloat()
        // Joints are bone origins, not skin: pad for the head above the neck, the hand beyond the wrist, and a prop's far end.
        focusRadius = maxOf(halfDiagonal + FRAMING_PADDING_METERS, MIN_FRAMING_RADIUS_METERS)
        // The figure faces +Z. Work that runs along the floor (planks, crunches, bridges) is foreshortened from the
        // front, so view it from the side; everything else reads best from a front three-quarter angle.
        azimuthDegrees = if ((max[2] - min[2]) > FLOOR_WORK_RATIO * (max[0] - min[0])) SIDE_AZIMUTH_DEGREES else DEFAULT_AZIMUTH_DEGREES
        updateCamera()
    }

    fun togglePaused() {
        if (paused) {
            clockStartNanos = System.nanoTime() - (pausedAtSeconds * 1e9f).toLong()
        } else {
            pausedAtSeconds = elapsedSeconds()
        }
        paused = !paused
    }

    /** Drag to look around the figure; only the horizontal axis, so it can never end up upside down. */
    fun orbitBy(degrees: Float) {
        azimuthDegrees = (azimuthDegrees + degrees) % 360f
        updateCamera()
    }

    fun resume() {
        if (running || destroyed) return
        running = true
        choreographer.postFrameCallback(frameCallback)
    }

    fun pause() {
        running = false
        choreographer.removeFrameCallback(frameCallback)
    }

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!running) return
            choreographer.postFrameCallback(this)
            renderFrame(frameTimeNanos)
        }
    }

    private fun renderFrame(frameTimeNanos: Long) {
        val chain = swapChain ?: return
        if (!uiHelper.isReadyToRender) return
        val animator = animator()
        if (animator != null && clipIndex >= 0) {
            val seconds = if (paused) pausedAtSeconds else elapsedSeconds()
            animator.applyAnimation(clipIndex, loopTime(seconds, clipDuration))
            animator.updateBoneMatrices()
        }
        if (renderer.beginFrame(chain, frameTimeNanos)) {
            renderer.render(view)
            renderer.endFrame()
            // Frames are queued to a GPU thread that compiles shaders on first use, so submitting one is not the
            // same as showing one. Wait for a handful so the spinner is not dropped over a blank view.
            if (!firstFrameReported && ++framesSubmitted >= FRAMES_BEFORE_REVEAL) {
                firstFrameReported = true
                onFirstFrame?.invoke()
            }
        }
    }

    private fun elapsedSeconds() = (System.nanoTime() - clockStartNanos) / 1e9f

    private fun updateCamera() {
        val aspect = if (viewportHeight > 0) viewportWidth.toDouble() / viewportHeight else DEFAULT_ASPECT
        camera.setProjection(FOV_DEGREES, aspect, 0.1, 50.0, Camera.Fov.VERTICAL)
        // The sphere around the clip must fit the narrower of the two fields of view.
        val halfVertical = Math.toRadians(FOV_DEGREES / 2)
        val halfHorizontal = atan(tan(halfVertical) * aspect)
        val distance = focusRadius / sin(minOf(halfVertical, halfHorizontal))
        val azimuth = Math.toRadians(azimuthDegrees.toDouble())
        val elevation = Math.toRadians(CAMERA_ELEVATION_DEGREES)
        camera.lookAt(
            focus[0] + sin(azimuth) * cos(elevation) * distance,
            focus[1] + sin(elevation) * distance,
            focus[2] + cos(azimuth) * cos(elevation) * distance,
            focus[0].toDouble(), focus[1].toDouble(), focus[2].toDouble(),
            0.0, 1.0, 0.0,
        )
    }

    fun destroy() {
        if (destroyed) return
        destroyed = true
        pause()
        uiHelper.detach()
        swapChain?.let { engine.destroySwapChain(it) }
        swapChain = null
        assetLoader.destroyAsset(asset)
        assetLoader.destroy()
        resourceLoader.destroy()
        materialProvider.destroyMaterials()
        materialProvider.destroy()
        engine.destroyEntity(sun)
        engine.lightManager.destroy(sun)
        engine.destroyIndirectLight(indirectLight)
        engine.destroyCameraComponent(cameraEntity)
        EntityManager.get().destroy(sun)
        EntityManager.get().destroy(cameraEntity)
        engine.destroyView(view)
        engine.destroyScene(scene)
        engine.destroyRenderer(renderer)
        engine.destroy()
    }

    private companion object {
        init {
            // Must run before the first Filament call, and property initializers (Engine.create) come before any init block.
            Filament.init()
            Gltfio.init()
        }

        const val TAG = "ExerciseAnimation"
        const val DEFAULT_AZIMUTH_DEGREES = 25f
        const val SIDE_AZIMUTH_DEGREES = 70f

        /** A clip is floor work when it is this many times longer front-to-back than it is wide. */
        const val FLOOR_WORK_RATIO = 1.4f
        const val DEFAULT_ASPECT = 4.0 / 3.0
        const val FOV_DEGREES = 30.0

        const val CAMERA_ELEVATION_DEGREES = 8.0
        const val FRAMES_BEFORE_REVEAL = 12
        const val FRAMING_SAMPLES = 24
        const val FRAMING_PADDING_METERS = 0.2f
        const val MIN_FRAMING_RADIUS_METERS = 0.9f

        const val SUN_LUX = 90_000f
        const val AMBIENT_LUX = 25_000f

    }
}
